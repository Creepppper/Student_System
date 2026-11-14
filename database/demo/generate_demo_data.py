#!/usr/bin/env python3
"""
生成演示用的学生与成绩数据（虚构姓名，随机但可复现）。

用法（在项目根目录执行）：
    python3 database/demo/generate_demo_data.py > database/009_demo_students_scores.sql

规则：
- 3 个年级（高一、高二、高三），每个年级 3 个班（1班、2班、3班）；
- 每个班随机 5–8 名学生，学号形如 20261101（2026 + 年级序号 + 班级序号 + 两位序号）；
- 两场演示考试：演示月考、演示期中考试；每个学生每场都有成绩；
- 约 8% 的记录有一科未录入（NULL），约 8% 的记录有一科缺考（记 0 分并写入缺考代码）；
- 分数围绕学生个人水平随机波动，并限制在满分范围内。

随机种子固定，重复运行输出完全相同。
"""
import random

SEED = 20261114
GRADES = ["高一", "高二", "高三"]
CLASSES = ["1班", "2班", "3班"]
EXAMS = [("演示月考", "2026-09-20"), ("演示期中考试", "2026-10-25")]
SUBJECTS = [("chinese", 150), ("math", 150), ("english", 150),
            ("physics", 100), ("chemistry", 100), ("biology", 100)]
SURNAMES = list("赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜")
GIVEN = list("伟芳娜敏静丽强磊军洋勇艳杰涛明超秀英华慧建文亮俊玲桂萍鹏悦宁晨瑶欣怡博")


def sql_str(value):
    return "NULL" if value is None else "'" + value.replace("'", "''") + "'"


def sql_num(value):
    return "NULL" if value is None else str(value)


def main():
    rng = random.Random(SEED)

    students = []  # (student_no, name, grade, class_name)
    for gi, grade in enumerate(GRADES, start=1):
        for ci, cls in enumerate(CLASSES, start=1):
            count = rng.randint(5, 8)
            for seq in range(1, count + 1):
                name = rng.choice(SURNAMES) + rng.choice(GIVEN) + rng.choice(GIVEN)
                student_no = f"2026{gi}{ci}{seq:02d}"
                students.append((student_no, name, grade, cls))

    # 每个学生一个基础水平，每场考试再加随机波动
    ability = {no: rng.gauss(0.72, 0.12) for no, *_ in students}

    scores = {exam: [] for exam, _ in EXAMS}  # exam -> list of row dicts
    for exam, _ in EXAMS:
        for student_no, *_ in students:
            row = {"student_no": student_no, "absent": "", "values": {}}
            for key, full in SUBJECTS:
                ratio = ability[student_no] + rng.gauss(0, 0.06)
                ratio = min(1.0, max(0.25, ratio))
                row["values"][key] = int(round(ratio * full))
            roll = rng.random()
            if roll < 0.08:
                key = rng.choice([k for k, _ in SUBJECTS])
                row["values"][key] = None  # 未录入
            elif roll < 0.16:
                key = rng.choice([k for k, _ in SUBJECTS])
                row["values"][key] = 0  # 缺考
                row["absent"] = key
            scores[exam].append(row)

    out = []
    out.append("-- ============================================================")
    out.append("-- 演示数据：虚构学生、两场演示考试及随机成绩")
    out.append("-- 由 database/demo/generate_demo_data.py 生成（随机种子固定，可复现）")
    out.append("-- 目标数据库：student_system")
    out.append("--")
    out.append("-- 说明：")
    out.append("--   1. 姓名为随机组合，不对应任何真实人员。")
    out.append("--   2. 可重复执行：学生按学号、成绩按(学生,考试)判重，已存在则不修改。")
    out.append("--   3. 演示数据会与已有的同名学生、同名考试并存，请勿在正式数据上使用。")
    out.append("-- 执行：sudo mariadb student_system < database/009_demo_students_scores.sql")
    out.append("-- ============================================================")
    out.append("")
    out.append("SET NAMES utf8mb4;")
    out.append("")
    out.append("-- 1. 学生")
    out.append("INSERT INTO stu_student (student_no, student_name, grade, class_name, user_id) VALUES")
    lines = [f"  ({sql_str(no)}, {sql_str(name)}, {sql_str(grade)}, {sql_str(cls)}, NULL)"
             for no, name, grade, cls in students]
    out.append(",\n".join(lines))
    out.append("ON DUPLICATE KEY UPDATE student_no = student_no;")
    out.append("")
    out.append("-- 2. 考试")
    for exam, date in EXAMS:
        out.append(f"INSERT INTO stu_exam (exam_name, exam_time)")
        out.append(f"SELECT {sql_str(exam)}, '{date}' FROM dual")
        out.append(f"WHERE NOT EXISTS (SELECT 1 FROM stu_exam WHERE exam_name = {sql_str(exam)} AND exam_time = '{date}');")
    out.append("")
    out.append("-- 3. 成绩（每场考试一条语句，通过学号和考试名称关联）")
    for exam, date in EXAMS:
        out.append(f"INSERT INTO stu_score (student_id, exam_id, chinese, math, english, physics, chemistry, biology, absent_subjects)")
        out.append("SELECT st.id, ex.id, v.chinese, v.math, v.english, v.physics, v.chemistry, v.biology, v.absent_subjects FROM (")
        rows = []
        for row in scores[exam]:
            vals = row["values"]
            rows.append(
                f"  SELECT {sql_str(row['student_no'])} AS student_no, "
                + ", ".join(f"{sql_num(vals[k])} AS {k}" for k, _ in SUBJECTS)
                + f", {sql_str(row['absent'])} AS absent_subjects"
            )
        out.append("\nUNION ALL\n".join(rows))
        out.append(") v")
        out.append("JOIN stu_student st ON st.student_no = v.student_no")
        out.append(f"JOIN stu_exam ex ON ex.exam_name = {sql_str(exam)} AND ex.exam_time = '{date}'")
        out.append("ON DUPLICATE KEY UPDATE student_id = student_id;")
        out.append("")
    out.append("-- ============================================================")
    out.append("-- 回滚（需要时取消注释执行；只删除演示数据，不影响其他记录）")
    out.append("-- ============================================================")
    out.append("-- DELETE FROM stu_score WHERE exam_id IN (SELECT id FROM (SELECT id FROM stu_exam WHERE exam_name IN ('演示月考', '演示期中考试')) e);")
    out.append("-- DELETE FROM stu_exam WHERE exam_name IN ('演示月考', '演示期中考试');")
    out.append("-- DELETE FROM stu_student WHERE student_no REGEXP '^2026[123][123][0-9]{2}$' AND user_id IS NULL;")
    return "\n".join(out) + "\n"


if __name__ == "__main__":
    print(main(), end="")
