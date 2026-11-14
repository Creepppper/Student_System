-- ============================================================
-- 演示数据：虚构学生、两场演示考试及随机成绩
-- 由 database/demo/generate_demo_data.py 生成（随机种子固定，可复现）
-- 目标数据库：student_system
--
-- 说明：
--   1. 姓名为随机组合，不对应任何真实人员。
--   2. 可重复执行：学生按学号、成绩按(学生,考试)判重，已存在则不修改。
--   3. 演示数据会与已有的同名学生、同名考试并存，请勿在正式数据上使用。
-- 执行：sudo mariadb student_system < database/009_demo_students_scores.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1. 学生
INSERT INTO stu_student (student_no, student_name, grade, class_name, user_id) VALUES
  ('20261101', '杨晨欣', '高一', '1班', NULL),
  ('20261102', '冯瑶芳', '高一', '1班', NULL),
  ('20261103', '金超鹏', '高一', '1班', NULL),
  ('20261104', '陈芳萍', '高一', '1班', NULL),
  ('20261105', '孔桂英', '高一', '1班', NULL),
  ('20261201', '杨宁洋', '高一', '2班', NULL),
  ('20261202', '沈俊杰', '高一', '2班', NULL),
  ('20261203', '曹晨华', '高一', '2班', NULL),
  ('20261204', '王艳亮', '高一', '2班', NULL),
  ('20261205', '卫宁萍', '高一', '2班', NULL),
  ('20261206', '陈静明', '高一', '2班', NULL),
  ('20261301', '王俊怡', '高一', '3班', NULL),
  ('20261302', '卫文军', '高一', '3班', NULL),
  ('20261303', '金华艳', '高一', '3班', NULL),
  ('20261304', '陈玲秀', '高一', '3班', NULL),
  ('20261305', '陈明艳', '高一', '3班', NULL),
  ('20261306', '李洋鹏', '高一', '3班', NULL),
  ('20262101', '华亮桂', '高二', '1班', NULL),
  ('20262102', '陈文亮', '高二', '1班', NULL),
  ('20262103', '张怡涛', '高二', '1班', NULL),
  ('20262104', '姜瑶洋', '高二', '1班', NULL),
  ('20262105', '何瑶磊', '高二', '1班', NULL),
  ('20262106', '魏超悦', '高二', '1班', NULL),
  ('20262107', '张秀涛', '高二', '1班', NULL),
  ('20262108', '姜宁杰', '高二', '1班', NULL),
  ('20262201', '孔建杰', '高二', '2班', NULL),
  ('20262202', '华鹏洋', '高二', '2班', NULL),
  ('20262203', '朱博亮', '高二', '2班', NULL),
  ('20262204', '张艳伟', '高二', '2班', NULL),
  ('20262205', '王桂艳', '高二', '2班', NULL),
  ('20262301', '华磊磊', '高二', '3班', NULL),
  ('20262302', '杨悦洋', '高二', '3班', NULL),
  ('20262303', '沈勇欣', '高二', '3班', NULL),
  ('20262304', '秦英悦', '高二', '3班', NULL),
  ('20262305', '周敏芳', '高二', '3班', NULL),
  ('20262306', '尤俊强', '高二', '3班', NULL),
  ('20262307', '金建洋', '高二', '3班', NULL),
  ('20262308', '李洋英', '高二', '3班', NULL),
  ('20263101', '郑瑶悦', '高三', '1班', NULL),
  ('20263102', '许晨华', '高三', '1班', NULL),
  ('20263103', '吴鹏杰', '高三', '1班', NULL),
  ('20263104', '张华俊', '高三', '1班', NULL),
  ('20263105', '孙慧伟', '高三', '1班', NULL),
  ('20263201', '尤勇艳', '高三', '2班', NULL),
  ('20263202', '卫玲艳', '高三', '2班', NULL),
  ('20263203', '朱超磊', '高三', '2班', NULL),
  ('20263204', '曹芳芳', '高三', '2班', NULL),
  ('20263205', '朱涛慧', '高三', '2班', NULL),
  ('20263206', '孙秀英', '高三', '2班', NULL),
  ('20263207', '华怡娜', '高三', '2班', NULL),
  ('20263301', '李俊俊', '高三', '3班', NULL),
  ('20263302', '严英慧', '高三', '3班', NULL),
  ('20263303', '朱桂桂', '高三', '3班', NULL),
  ('20263304', '张勇慧', '高三', '3班', NULL),
  ('20263305', '陶怡欣', '高三', '3班', NULL),
  ('20263306', '魏桂勇', '高三', '3班', NULL)
ON DUPLICATE KEY UPDATE student_no = student_no;

-- 2. 考试
INSERT INTO stu_exam (exam_name, exam_time)
SELECT '演示月考', '2026-09-20' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM stu_exam WHERE exam_name = '演示月考' AND exam_time = '2026-09-20');
INSERT INTO stu_exam (exam_name, exam_time)
SELECT '演示期中考试', '2026-10-25' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM stu_exam WHERE exam_name = '演示期中考试' AND exam_time = '2026-10-25');

-- 3. 成绩（每场考试一条语句，通过学号和考试名称关联）
INSERT INTO stu_score (student_id, exam_id, chinese, math, english, physics, chemistry, biology, absent_subjects)
SELECT st.id, ex.id, v.chinese, v.math, v.english, v.physics, v.chemistry, v.biology, v.absent_subjects FROM (
  SELECT '20261101' AS student_no, 117 AS chinese, 127 AS math, 128 AS english, 85 AS physics, 93 AS chemistry, 89 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261102' AS student_no, 99 AS chinese, 87 AS math, 75 AS english, 68 AS physics, 56 AS chemistry, 58 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261103' AS student_no, 99 AS chinese, 109 AS math, 121 AS english, 63 AS physics, 70 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261104' AS student_no, 116 AS chinese, 120 AS math, 117 AS english, 70 AS physics, 78 AS chemistry, 71 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261105' AS student_no, 116 AS chinese, 125 AS math, 113 AS english, 71 AS physics, 76 AS chemistry, 73 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261201' AS student_no, 150 AS chinese, 119 AS math, 134 AS english, NULL AS physics, 100 AS chemistry, 100 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261202' AS student_no, 85 AS chinese, 78 AS math, 98 AS english, 62 AS physics, 59 AS chemistry, 56 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261203' AS student_no, 84 AS chinese, 90 AS math, 72 AS english, 39 AS physics, 63 AS chemistry, 57 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261204' AS student_no, 78 AS chinese, 85 AS math, 85 AS english, 52 AS physics, 51 AS chemistry, 60 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261205' AS student_no, 114 AS chinese, 115 AS math, 128 AS english, 81 AS physics, 82 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261206' AS student_no, 86 AS chinese, 79 AS math, 101 AS english, 68 AS physics, 62 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261301' AS student_no, 71 AS chinese, 87 AS math, 78 AS english, 69 AS physics, 59 AS chemistry, 57 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261302' AS student_no, 102 AS chinese, 83 AS math, 77 AS english, 57 AS physics, 61 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261303' AS student_no, 107 AS chinese, 101 AS math, 118 AS english, 61 AS physics, 69 AS chemistry, 83 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261304' AS student_no, 113 AS chinese, 132 AS math, 137 AS english, 90 AS physics, 91 AS chemistry, 89 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261305' AS student_no, 114 AS chinese, 111 AS math, 102 AS english, 71 AS physics, 74 AS chemistry, 75 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261306' AS student_no, 87 AS chinese, 78 AS math, 106 AS english, 50 AS physics, 48 AS chemistry, 60 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262101' AS student_no, 132 AS chinese, 125 AS math, 140 AS english, 92 AS physics, 78 AS chemistry, 77 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262102' AS student_no, 113 AS chinese, 117 AS math, 124 AS english, 74 AS physics, 72 AS chemistry, 84 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262103' AS student_no, 123 AS chinese, 117 AS math, 0 AS english, 80 AS physics, 76 AS chemistry, 77 AS biology, 'english' AS absent_subjects
UNION ALL
  SELECT '20262104' AS student_no, 101 AS chinese, 85 AS math, 88 AS english, 52 AS physics, 65 AS chemistry, 69 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262105' AS student_no, 107 AS chinese, 114 AS math, 124 AS english, 66 AS physics, 76 AS chemistry, 77 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262106' AS student_no, 98 AS chinese, 101 AS math, 89 AS english, 63 AS physics, 61 AS chemistry, 68 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262107' AS student_no, 109 AS chinese, 98 AS math, 99 AS english, 72 AS physics, 58 AS chemistry, 67 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262108' AS student_no, 101 AS chinese, 105 AS math, 110 AS english, 74 AS physics, 70 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262201' AS student_no, 80 AS chinese, 80 AS math, 114 AS english, 59 AS physics, 58 AS chemistry, 62 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262202' AS student_no, 116 AS chinese, 121 AS math, 116 AS english, 85 AS physics, 0 AS chemistry, 85 AS biology, 'chemistry' AS absent_subjects
UNION ALL
  SELECT '20262203' AS student_no, 115 AS chinese, 127 AS math, 107 AS english, 87 AS physics, 86 AS chemistry, 80 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262204' AS student_no, 145 AS chinese, 138 AS math, 133 AS english, 92 AS physics, 100 AS chemistry, 99 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262205' AS student_no, 117 AS chinese, 104 AS math, 121 AS english, 80 AS physics, 71 AS chemistry, 74 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262301' AS student_no, 115 AS chinese, 107 AS math, 93 AS english, 81 AS physics, 73 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262302' AS student_no, 145 AS chinese, 141 AS math, 141 AS english, 87 AS physics, 87 AS chemistry, 100 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262303' AS student_no, 129 AS chinese, 124 AS math, 109 AS english, 93 AS physics, 73 AS chemistry, 88 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262304' AS student_no, 104 AS chinese, 104 AS math, 126 AS english, 67 AS physics, 79 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262305' AS student_no, 90 AS chinese, 64 AS math, 68 AS english, 54 AS physics, 49 AS chemistry, NULL AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262306' AS student_no, 150 AS chinese, 150 AS math, 142 AS english, 84 AS physics, 100 AS chemistry, 89 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262307' AS student_no, 88 AS chinese, 103 AS math, 112 AS english, 66 AS physics, 69 AS chemistry, 71 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262308' AS student_no, 146 AS chinese, 122 AS math, 126 AS english, 93 AS physics, 85 AS chemistry, 98 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263101' AS student_no, 119 AS chinese, 131 AS math, 113 AS english, 84 AS physics, 80 AS chemistry, 85 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263102' AS student_no, 102 AS chinese, 86 AS math, 107 AS english, 73 AS physics, 70 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263103' AS student_no, 117 AS chinese, 103 AS math, NULL AS english, 78 AS physics, 70 AS chemistry, 74 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263104' AS student_no, 129 AS chinese, 109 AS math, 114 AS english, 72 AS physics, 70 AS chemistry, 65 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263105' AS student_no, 97 AS chinese, 102 AS math, 116 AS english, 67 AS physics, 68 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263201' AS student_no, 116 AS chinese, 117 AS math, 115 AS english, 82 AS physics, 78 AS chemistry, 64 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263202' AS student_no, 112 AS chinese, 96 AS math, 111 AS english, 72 AS physics, 72 AS chemistry, 64 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263203' AS student_no, 100 AS chinese, 83 AS math, 103 AS english, 62 AS physics, 58 AS chemistry, 51 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263204' AS student_no, 134 AS chinese, 137 AS math, 137 AS english, 87 AS physics, 94 AS chemistry, 100 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263205' AS student_no, 111 AS chinese, 115 AS math, 122 AS english, 82 AS physics, 73 AS chemistry, 81 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263206' AS student_no, 118 AS chinese, 131 AS math, 123 AS english, 79 AS physics, 84 AS chemistry, 87 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263207' AS student_no, 86 AS chinese, 94 AS math, 81 AS english, 57 AS physics, 53 AS chemistry, 54 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263301' AS student_no, 120 AS chinese, 120 AS math, 111 AS english, 93 AS physics, 80 AS chemistry, 80 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263302' AS student_no, 114 AS chinese, 127 AS math, 121 AS english, 73 AS physics, 67 AS chemistry, 78 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263303' AS student_no, 83 AS chinese, 102 AS math, 96 AS english, 60 AS physics, 70 AS chemistry, 54 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263304' AS student_no, 0 AS chinese, 150 AS math, 135 AS english, 82 AS physics, 100 AS chemistry, 95 AS biology, 'chinese' AS absent_subjects
UNION ALL
  SELECT '20263305' AS student_no, 103 AS chinese, 105 AS math, 91 AS english, 66 AS physics, 49 AS chemistry, 56 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263306' AS student_no, 105 AS chinese, 81 AS math, 99 AS english, 66 AS physics, 51 AS chemistry, 0 AS biology, 'biology' AS absent_subjects
) v
JOIN stu_student st ON st.student_no = v.student_no
JOIN stu_exam ex ON ex.exam_name = '演示月考' AND ex.exam_time = '2026-09-20'
ON DUPLICATE KEY UPDATE student_id = student_id;

INSERT INTO stu_score (student_id, exam_id, chinese, math, english, physics, chemistry, biology, absent_subjects)
SELECT st.id, ex.id, v.chinese, v.math, v.english, v.physics, v.chemistry, v.biology, v.absent_subjects FROM (
  SELECT '20261101' AS student_no, 118 AS chinese, 127 AS math, 136 AS english, 81 AS physics, 87 AS chemistry, 80 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261102' AS student_no, 86 AS chinese, 102 AS math, 85 AS english, 62 AS physics, 66 AS chemistry, 74 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261103' AS student_no, 103 AS chinese, 87 AS math, 0 AS english, 66 AS physics, 62 AS chemistry, 64 AS biology, 'english' AS absent_subjects
UNION ALL
  SELECT '20261104' AS student_no, 122 AS chinese, 114 AS math, 105 AS english, 85 AS physics, 78 AS chemistry, 82 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261105' AS student_no, 112 AS chinese, 126 AS math, 117 AS english, 79 AS physics, NULL AS chemistry, 86 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261201' AS student_no, 129 AS chinese, 131 AS math, 144 AS english, 95 AS physics, 100 AS chemistry, 82 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261202' AS student_no, 89 AS chinese, 89 AS math, 82 AS english, 53 AS physics, 49 AS chemistry, 64 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261203' AS student_no, NULL AS chinese, 81 AS math, 82 AS english, 55 AS physics, 44 AS chemistry, 57 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261204' AS student_no, 89 AS chinese, 99 AS math, 86 AS english, 55 AS physics, 54 AS chemistry, 52 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261205' AS student_no, 103 AS chinese, 129 AS math, 140 AS english, 80 AS physics, 76 AS chemistry, 79 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261206' AS student_no, 89 AS chinese, 84 AS math, 90 AS english, 53 AS physics, 54 AS chemistry, 55 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261301' AS student_no, 105 AS chinese, 76 AS math, 99 AS english, 59 AS physics, 55 AS chemistry, 59 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261302' AS student_no, 81 AS chinese, 107 AS math, 92 AS english, 50 AS physics, 63 AS chemistry, 61 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261303' AS student_no, 115 AS chinese, 120 AS math, 110 AS english, 0 AS physics, 65 AS chemistry, 75 AS biology, 'physics' AS absent_subjects
UNION ALL
  SELECT '20261304' AS student_no, 137 AS chinese, 118 AS math, 122 AS english, 80 AS physics, 76 AS chemistry, 92 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261305' AS student_no, 95 AS chinese, 92 AS math, 99 AS english, 67 AS physics, 72 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20261306' AS student_no, 99 AS chinese, 96 AS math, 97 AS english, 53 AS physics, 67 AS chemistry, 66 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262101' AS student_no, 116 AS chinese, 127 AS math, 132 AS english, 79 AS physics, 94 AS chemistry, 95 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262102' AS student_no, 94 AS chinese, 121 AS math, 109 AS english, 0 AS physics, 79 AS chemistry, 77 AS biology, 'physics' AS absent_subjects
UNION ALL
  SELECT '20262103' AS student_no, 115 AS chinese, 114 AS math, 105 AS english, 67 AS physics, 85 AS chemistry, 68 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262104' AS student_no, 92 AS chinese, 97 AS math, 111 AS english, 75 AS physics, 64 AS chemistry, 62 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262105' AS student_no, 104 AS chinese, 115 AS math, 106 AS english, 74 AS physics, 69 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262106' AS student_no, 98 AS chinese, 82 AS math, 86 AS english, 73 AS physics, 73 AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262107' AS student_no, 84 AS chinese, 96 AS math, 95 AS english, 78 AS physics, 56 AS chemistry, 62 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262108' AS student_no, 87 AS chinese, 118 AS math, 107 AS english, 76 AS physics, 64 AS chemistry, 74 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262201' AS student_no, 90 AS chinese, 85 AS math, 96 AS english, 70 AS physics, 73 AS chemistry, 52 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262202' AS student_no, 125 AS chinese, 130 AS math, 124 AS english, 86 AS physics, 89 AS chemistry, 87 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262203' AS student_no, 138 AS chinese, 122 AS math, 114 AS english, 83 AS physics, 81 AS chemistry, 82 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262204' AS student_no, 0 AS chinese, 150 AS math, 150 AS english, 99 AS physics, 95 AS chemistry, 95 AS biology, 'chinese' AS absent_subjects
UNION ALL
  SELECT '20262205' AS student_no, 95 AS chinese, 97 AS math, 99 AS english, 82 AS physics, 73 AS chemistry, 75 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262301' AS student_no, 98 AS chinese, 97 AS math, 107 AS english, 68 AS physics, 69 AS chemistry, 66 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262302' AS student_no, 147 AS chinese, 127 AS math, 149 AS english, 82 AS physics, 88 AS chemistry, 97 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262303' AS student_no, 121 AS chinese, 124 AS math, 119 AS english, 86 AS physics, 93 AS chemistry, 82 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262304' AS student_no, 124 AS chinese, 110 AS math, 124 AS english, 90 AS physics, 78 AS chemistry, 86 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262305' AS student_no, 82 AS chinese, 69 AS math, 79 AS english, 52 AS physics, 50 AS chemistry, 44 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262306' AS student_no, 140 AS chinese, 135 AS math, 143 AS english, 100 AS physics, 84 AS chemistry, 96 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262307' AS student_no, 110 AS chinese, 103 AS math, 98 AS english, 62 AS physics, 61 AS chemistry, 64 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20262308' AS student_no, 127 AS chinese, 0 AS math, 143 AS english, 97 AS physics, 85 AS chemistry, 85 AS biology, 'math' AS absent_subjects
UNION ALL
  SELECT '20263101' AS student_no, 125 AS chinese, 0 AS math, 119 AS english, 77 AS physics, 77 AS chemistry, 81 AS biology, 'math' AS absent_subjects
UNION ALL
  SELECT '20263102' AS student_no, 89 AS chinese, 92 AS math, 86 AS english, 58 AS physics, 64 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263103' AS student_no, 95 AS chinese, 0 AS math, 112 AS english, 73 AS physics, 81 AS chemistry, 78 AS biology, 'math' AS absent_subjects
UNION ALL
  SELECT '20263104' AS student_no, 104 AS chinese, 112 AS math, 122 AS english, 76 AS physics, NULL AS chemistry, 76 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263105' AS student_no, 124 AS chinese, 111 AS math, 116 AS english, 88 AS physics, 74 AS chemistry, 81 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263201' AS student_no, 102 AS chinese, 109 AS math, 115 AS english, 81 AS physics, 71 AS chemistry, 91 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263202' AS student_no, 96 AS chinese, 114 AS math, 104 AS english, 67 AS physics, 74 AS chemistry, 72 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263203' AS student_no, 111 AS chinese, 94 AS math, 81 AS english, 61 AS physics, 63 AS chemistry, 66 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263204' AS student_no, 145 AS chinese, 128 AS math, 147 AS english, 83 AS physics, 85 AS chemistry, 100 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263205' AS student_no, 123 AS chinese, 134 AS math, 130 AS english, 76 AS physics, 72 AS chemistry, 83 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263206' AS student_no, 137 AS chinese, 117 AS math, 135 AS english, 94 AS physics, 93 AS chemistry, 73 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263207' AS student_no, 77 AS chinese, 71 AS math, 61 AS english, 60 AS physics, 54 AS chemistry, 54 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263301' AS student_no, 117 AS chinese, 105 AS math, 120 AS english, 72 AS physics, 84 AS chemistry, 84 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263302' AS student_no, 110 AS chinese, 131 AS math, 110 AS english, 75 AS physics, 84 AS chemistry, 79 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263303' AS student_no, 112 AS chinese, 81 AS math, 101 AS english, 61 AS physics, 61 AS chemistry, 63 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263304' AS student_no, 134 AS chinese, 146 AS math, 150 AS english, 100 AS physics, 88 AS chemistry, 93 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263305' AS student_no, 95 AS chinese, 96 AS math, 102 AS english, 60 AS physics, 53 AS chemistry, 60 AS biology, '' AS absent_subjects
UNION ALL
  SELECT '20263306' AS student_no, 93 AS chinese, 0 AS math, 104 AS english, 60 AS physics, 73 AS chemistry, 60 AS biology, 'math' AS absent_subjects
) v
JOIN stu_student st ON st.student_no = v.student_no
JOIN stu_exam ex ON ex.exam_name = '演示期中考试' AND ex.exam_time = '2026-10-25'
ON DUPLICATE KEY UPDATE student_id = student_id;

-- ============================================================
-- 回滚（需要时取消注释执行；只删除演示数据，不影响其他记录）
-- ============================================================
-- DELETE FROM stu_score WHERE exam_id IN (SELECT id FROM (SELECT id FROM stu_exam WHERE exam_name IN ('演示月考', '演示期中考试')) e);
-- DELETE FROM stu_exam WHERE exam_name IN ('演示月考', '演示期中考试');
-- DELETE FROM stu_student WHERE student_no REGEXP '^2026[123][123][0-9]{2}$' AND user_id IS NULL;
