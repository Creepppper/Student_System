-- 同一年级、班级最多一个班主任；不同年级同名班级互不影响。
-- 依赖 011；MariaDB 11.8 / MySQL 8；需要 ALTER 权限。
-- 执行前检查：SELECT grade,class_name,COUNT(*) FROM stu_class_teacher GROUP BY grade,class_name HAVING COUNT(*)>1;
-- 如存在重复，先由管理员确认并取消多余分配；不自动删除任何记录。
-- 执行：sudo mariadb student_system < database/019_unique_class_teacher.sql
-- 非幂等；执行前确认不存在 uk_stu_class_teacher_grade_class 索引。
-- 回滚：ALTER TABLE stu_class_teacher DROP INDEX uk_stu_class_teacher_grade_class;
ALTER TABLE stu_class_teacher ADD UNIQUE KEY uk_stu_class_teacher_grade_class (grade, class_name);
