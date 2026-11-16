-- 年级、班级标准约束；依次执行 001、011 后执行，适用于 MariaDB 11.8 / MySQL 8.0.16+。
-- 执行前检查两表是否存在非标准数据（含空格），由管理员人工处理，不自动改写：
-- SELECT id, grade, class_name FROM stu_student WHERE BINARY grade NOT IN (BINARY '高一',BINARY '高二',BINARY '高三') OR BINARY class_name NOT IN (BINARY '1班',BINARY '2班',BINARY '3班',BINARY '4班',BINARY '5班',BINARY '6班',BINARY '7班',BINARY '8班',BINARY '9班',BINARY '10班',BINARY '11班',BINARY '12班');
-- 对 stu_class_teacher 执行同样的检查；两项均为空才执行。约束不改变已有数据。
-- 执行：sudo mariadb student_system < database/015_standard_grade_class.sql
-- DDL 自动提交；非幂等。若第二条失败，先检查第一条是否已执行，修复后仅执行缺失项。
-- MariaDB 回滚：ALTER TABLE stu_student DROP CONSTRAINT chk_student_grade, DROP CONSTRAINT chk_student_class;
-- ALTER TABLE stu_class_teacher DROP CONSTRAINT chk_teacher_grade, DROP CONSTRAINT chk_teacher_class;
-- MySQL 8 回滚：将上述 DROP CONSTRAINT 替换为 DROP CHECK。
SET NAMES utf8mb4;
ALTER TABLE stu_student
  ADD CONSTRAINT chk_student_grade CHECK (BINARY grade IN (BINARY '高一', BINARY '高二', BINARY '高三')),
  ADD CONSTRAINT chk_student_class CHECK (BINARY class_name IN (BINARY '1班', BINARY '2班', BINARY '3班', BINARY '4班', BINARY '5班', BINARY '6班', BINARY '7班', BINARY '8班', BINARY '9班', BINARY '10班', BINARY '11班', BINARY '12班'));
ALTER TABLE stu_class_teacher
  ADD CONSTRAINT chk_teacher_grade CHECK (BINARY grade IN (BINARY '高一', BINARY '高二', BINARY '高三')),
  ADD CONSTRAINT chk_teacher_class CHECK (BINARY class_name IN (BINARY '1班', BINARY '2班', BINARY '3班', BINARY '4班', BINARY '5班', BINARY '6班', BINARY '7班', BINARY '8班', BINARY '9班', BINARY '10班', BINARY '11班', BINARY '12班'));
