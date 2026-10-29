-- ============================================================
-- 设计草稿，尚未执行，尚未部署，需评审后使用。
-- 目标：学生信息与成绩管理系统 P0 业务表（MySQL 8 / MariaDB 11.8）。
-- 不包含 DROP、数据变更、用户/权限变更；本文件不应直接在生产库执行。
-- 主键：新建业务主表 id INT AUTO_INCREMENT。
-- 账号关联：sys_user.user_id 为 BIGINT，绑定表 user_id 同为 BIGINT，不修改原表。
-- ============================================================
SET NAMES utf8mb4;

-- stu_year：学年；模块 N01；DR: DR-ORG-001
CREATE TABLE `stu_year` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `year_code` VARCHAR(16) NOT NULL COMMENT '学年编码，如 2026-2027',
  `year_name` VARCHAR(32) NOT NULL COMMENT '学年名称',
  `start_date` DATE NOT NULL COMMENT '学年开始日期',
  `end_date` DATE NOT NULL COMMENT '学年结束日期',
  `is_current` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否当前学年：1 是，0 否',
  `current_slot` TINYINT GENERATED ALWAYS AS (CASE WHEN is_current=1 THEN 1 ELSE NULL END) STORED COMMENT '当前学年唯一槽位（生成列）',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_year_code` (`year_code`) COMMENT '学年编码唯一',
  UNIQUE KEY `uk_stu_year_name` (`year_name`) COMMENT '学年名称唯一',
  UNIQUE KEY `uk_stu_year_current` (`current_slot`) COMMENT '最多一个当前学年',
  KEY `idx_stu_year_status` (`status`) COMMENT '状态查询',
  CONSTRAINT `ck_stu_year_dates` CHECK (start_date <= end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学年';

-- stu_term：学期；模块 N01；DR: DR-ORG-002
CREATE TABLE `stu_term` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `year_id` INT NOT NULL COMMENT '学年 ID',
  `term_no` TINYINT NOT NULL COMMENT '学期序号 1/2',
  `term_name` VARCHAR(32) NOT NULL COMMENT '学期名称',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_term_no` (`year_id`, `term_no`) COMMENT '同一学年学期序号唯一',
  UNIQUE KEY `uk_stu_term_name` (`year_id`, `term_name`) COMMENT '同一学年学期名称唯一',
  KEY `idx_stu_term_year` (`year_id`) COMMENT '学年查询',
  CONSTRAINT `ck_stu_term_dates` CHECK (start_date <= end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期';

-- stu_grade：年级；模块 N01；DR: DR-ORG-003
CREATE TABLE `stu_grade` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `grade_code` VARCHAR(16) NOT NULL COMMENT '年级代码，如 G1/G2/G3',
  `grade_name` VARCHAR(32) NOT NULL COMMENT '年级名称，如 高一',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_grade_code` (`grade_code`) COMMENT '年级代码唯一',
  UNIQUE KEY `uk_stu_grade_name` (`grade_name`) COMMENT '年级名称唯一',
  KEY `idx_stu_grade_status` (`status`) COMMENT '状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年级';

-- stu_class：行政班（学年+年级）；模块 N01；DR: DR-ORG-004
 -- 设计说明：班级不是 sys_dept；有学生/任务/发布时禁止物理删除。
CREATE TABLE `stu_class` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `year_id` INT NOT NULL COMMENT '学年 ID',
  `grade_id` INT NOT NULL COMMENT '年级 ID',
  `class_code` VARCHAR(32) NOT NULL COMMENT '班级编码',
  `class_name` VARCHAR(64) NOT NULL COMMENT '班级名称',
  `roster_version` INT NOT NULL DEFAULT 0 COMMENT '花名册版本（任务生成快照）',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_class_code` (`year_id`, `class_code`) COMMENT '同一学年班级编码唯一',
  UNIQUE KEY `uk_stu_class_name` (`year_id`, `class_name`) COMMENT '同一学年班级名称唯一',
  KEY `idx_stu_class_grade` (`grade_id`) COMMENT '年级查询',
  KEY `idx_stu_class_status` (`status`) COMMENT '状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政班（学年+年级）';

-- stu_student：学生业务档案；模块 N02；DR: DR-STU-001
CREATE TABLE `stu_student` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键（内部 ID，不是学号）',
  `student_no` VARCHAR(32) NOT NULL COMMENT '学号，全校唯一',
  `student_name` VARCHAR(64) NOT NULL COMMENT '姓名',
  `gender` CHAR(1) NOT NULL DEFAULT 'U' COMMENT '性别 M/F/U',
  `enrollment_date` DATE NULL COMMENT '入学日期',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '学籍状态 ACTIVE/INACTIVE/GRADUATED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_student_no` (`student_no`) COMMENT '学号全校唯一',
  KEY `idx_stu_student_name` (`student_name`) COMMENT '姓名查询',
  KEY `idx_stu_student_status` (`status`) COMMENT '学籍状态查询',
  CONSTRAINT `ck_stu_student_gender` CHECK (gender IN ('M','F','U'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生业务档案';

-- stu_student_class：学生班级归属历史；模块 N02；DR: DR-STU-002
 -- 设计说明：当前归属唯一由生成列唯一键 + 事务锁共同保证；历史不覆盖。
CREATE TABLE `stu_student_class` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` INT NOT NULL COMMENT '学生 ID',
  `class_id` INT NOT NULL COMMENT '班级 ID',
  `effective_from` DATE NOT NULL COMMENT '生效日期',
  `effective_to` DATE NULL COMMENT '结束日期；空表示当前',
  `is_current` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否当前归属',
  `active_student_key` INT GENERATED ALWAYS AS (CASE WHEN is_current=1 THEN student_id ELSE NULL END) STORED COMMENT '当前归属唯一槽位（生成列）',
  `change_reason` VARCHAR(255) NULL COMMENT '转班/变更原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_student_class_current` (`active_student_key`) COMMENT '每名学生最多一条当前归属',
  UNIQUE KEY `uk_stu_student_class_hist` (`student_id`, `class_id`, `effective_from`) COMMENT '同一学生/班级/起始日唯一',
  KEY `idx_stu_student_class_student` (`student_id`, `is_current`) COMMENT '学生当前/历史查询',
  KEY `idx_stu_student_class_class` (`class_id`, `is_current`) COMMENT '班级当前学生查询',
  CONSTRAINT `ck_stu_student_class_dates` CHECK (effective_to IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生班级归属历史';

-- stu_head_teacher：班主任业务档案；模块 N03；DR: DR-TCH-001
 -- 设计说明：只表示班主任，不表示任课教师；不建任教关系。
CREATE TABLE `stu_head_teacher` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_no` VARCHAR(32) NOT NULL COMMENT '工号，全校唯一',
  `teacher_name` VARCHAR(64) NOT NULL COMMENT '姓名',
  `gender` CHAR(1) NOT NULL DEFAULT 'U' COMMENT '性别 M/F/U',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_head_teacher_no` (`teacher_no`) COMMENT '工号唯一',
  KEY `idx_stu_head_teacher_name` (`teacher_name`) COMMENT '姓名查询',
  KEY `idx_stu_head_teacher_status` (`status`) COMMENT '状态查询',
  CONSTRAINT `ck_stu_head_teacher_gender` CHECK (gender IN ('M','F','U'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班主任业务档案';

-- stu_head_teacher_assignment：班主任任职历史；模块 N03；DR: ENT-08
 -- 设计说明：同一班主任可负责多个班；同一班级最多一个当前班主任。时间区间不重叠由事务+服务层+索引共同保证，普通 UNIQUE 不能完整表达。
CREATE TABLE `stu_head_teacher_assignment` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `head_teacher_id` INT NOT NULL COMMENT '班主任 ID',
  `class_id` INT NOT NULL COMMENT '班级 ID',
  `effective_from` DATE NOT NULL COMMENT '任职开始日期',
  `effective_to` DATE NULL COMMENT '任职结束日期；空表示当前',
  `is_current` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否当前有效任职',
  `active_class_key` INT GENERATED ALWAYS AS (CASE WHEN is_current=1 THEN class_id ELSE NULL END) STORED COMMENT '当前班级唯一槽位（生成列）',
  `change_reason` VARCHAR(255) NULL COMMENT '指派/变更/结束原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_hta_current_class` (`active_class_key`) COMMENT '同一班级同一时刻最多一条有效任职',
  UNIQUE KEY `uk_stu_hta_hist` (`head_teacher_id`, `class_id`, `effective_from`) COMMENT '同一班主任/班级/起始日唯一',
  KEY `idx_stu_hta_teacher` (`head_teacher_id`, `is_current`) COMMENT '班主任当前/历史任职',
  KEY `idx_stu_hta_class` (`class_id`, `is_current`) COMMENT '班级当前班主任',
  CONSTRAINT `ck_stu_hta_dates` CHECK (effective_to IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班主任任职历史';

-- stu_subject：科目；模块 N03；DR: DR-TCH-002
CREATE TABLE `stu_subject` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `subject_code` VARCHAR(16) NOT NULL COMMENT '科目代码',
  `subject_name` VARCHAR(32) NOT NULL COMMENT '科目名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_subject_code` (`subject_code`) COMMENT '科目代码唯一',
  UNIQUE KEY `uk_stu_subject_name` (`subject_name`) COMMENT '科目名称唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目';

-- stu_exam：考试；模块 N04；DR: DR-EXAM-001
CREATE TABLE `stu_exam` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `year_id` INT NOT NULL COMMENT '学年 ID',
  `term_id` INT NOT NULL COMMENT '学期 ID',
  `exam_code` VARCHAR(32) NOT NULL COMMENT '考试编码',
  `exam_name` VARCHAR(128) NOT NULL COMMENT '考试名称',
  `exam_type` VARCHAR(32) NULL COMMENT '考试类型（月考/期中/期末等）',
  `exam_date` DATE NULL COMMENT '考试日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'EXAM_DRAFT' COMMENT 'EXAM_DRAFT/OPEN/CLOSED/ARCHIVED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_exam_code` (`exam_code`) COMMENT '考试编码唯一',
  UNIQUE KEY `uk_stu_exam_name` (`year_id`, `term_id`, `exam_name`) COMMENT '同一学年学期考试名称唯一',
  KEY `idx_stu_exam_term_status` (`year_id`, `term_id`, `status`) COMMENT '学期/状态查询',
  CONSTRAINT `ck_stu_exam_status` CHECK (status IN ('EXAM_DRAFT','EXAM_OPEN','EXAM_CLOSED','EXAM_ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试';

-- stu_exam_class：考试班级范围；模块 N04；DR: DR-EXAM-002
CREATE TABLE `stu_exam_class` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` INT NOT NULL COMMENT '考试 ID',
  `class_id` INT NOT NULL COMMENT '班级 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_exam_class` (`exam_id`, `class_id`) COMMENT '考试×班级唯一',
  KEY `idx_stu_exam_class_class` (`class_id`) COMMENT '班级考试查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试班级范围';

-- stu_exam_subject：考试科目/满分/线；模块 N04；DR: DR-EXAM-003
CREATE TABLE `stu_exam_subject` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` INT NOT NULL COMMENT '考试 ID',
  `subject_id` INT NOT NULL COMMENT '科目 ID',
  `full_mark` DECIMAL(6,1) NOT NULL COMMENT '满分（0.5 步长）',
  `pass_line` DECIMAL(6,1) NOT NULL COMMENT '及格线（0.5 步长）',
  `excellent_line` DECIMAL(6,1) NOT NULL COMMENT '优秀线（0.5 步长）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_exam_subject` (`exam_id`, `subject_id`) COMMENT '考试×科目唯一',
  KEY `idx_stu_exam_subject_subject` (`subject_id`) COMMENT '科目查询',
  CONSTRAINT `ck_stu_exam_subject_marks` CHECK (full_mark >= 0 AND pass_line >= 0 AND excellent_line >= 0 AND pass_line <= excellent_line AND excellent_line <= full_mark),
  CONSTRAINT `ck_stu_exam_subject_step` CHECK (full_mark*2 = FLOOR(full_mark*2) AND pass_line*2 = FLOOR(pass_line*2) AND excellent_line*2 = FLOOR(excellent_line*2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试科目/满分/线';

-- stu_score_task：成绩录入任务（考试×班级×科目）；模块 N05；DR: DR-SCORE-001
 -- 设计说明：第一责任人快照独立于后续任职变更；同事务写审计。
CREATE TABLE `stu_score_task` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` INT NOT NULL COMMENT '考试 ID',
  `class_id` INT NOT NULL COMMENT '班级 ID',
  `subject_id` INT NOT NULL COMMENT '科目 ID',
  `task_status` VARCHAR(20) NOT NULL DEFAULT 'TASK_DRAFT' COMMENT 'TASK_DRAFT/SUBMITTED/APPROVED/RETURNED',
  `first_owner_teacher_id` INT NOT NULL COMMENT '任务创建时第一责任人班主任快照',
  `roster_version` INT NOT NULL DEFAULT 0 COMMENT '花名册版本快照',
  `full_mark_snapshot` DECIMAL(6,1) NOT NULL COMMENT '满分快照',
  `pass_line_snapshot` DECIMAL(6,1) NOT NULL COMMENT '及格线快照',
  `excellent_line_snapshot` DECIMAL(6,1) NOT NULL COMMENT '优秀线快照',
  `submitted_by` BIGINT NULL COMMENT '提交人 sys_user.user_id',
  `submitted_at` DATETIME NULL COMMENT '提交时间',
  `reviewed_by` BIGINT NULL COMMENT '审核人 sys_user.user_id',
  `reviewed_at` DATETIME NULL COMMENT '审核时间',
  `return_reason` VARCHAR(500) NULL COMMENT '退回原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_score_task` (`exam_id`, `class_id`, `subject_id`) COMMENT '考试×班级×科目唯一',
  KEY `idx_stu_score_task_owner_status` (`first_owner_teacher_id`, `task_status`) COMMENT '班主任待办',
  KEY `idx_stu_score_task_class_status` (`class_id`, `task_status`) COMMENT '班级任务',
  CONSTRAINT `ck_stu_score_task_status` CHECK (task_status IN ('TASK_DRAFT','TASK_SUBMITTED','TASK_APPROVED','TASK_RETURNED')),
  CONSTRAINT `ck_stu_score_task_marks` CHECK (full_mark_snapshot >= 0 AND pass_line_snapshot >= 0 AND excellent_line_snapshot >= 0 AND pass_line_snapshot <= excellent_line_snapshot AND excellent_line_snapshot <= full_mark_snapshot AND full_mark_snapshot*2 = FLOOR(full_mark_snapshot*2) AND pass_line_snapshot*2 = FLOOR(pass_line_snapshot*2) AND excellent_line_snapshot*2 = FLOOR(excellent_line_snapshot*2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩录入任务（考试×班级×科目）';

-- stu_score：成绩记录（任务×学生）；模块 N05；DR: DR-SCORE-002
 -- 设计说明：真实 0 分是 SCORE_NORMAL + 0.0；缺考/未录入不得写 0。
CREATE TABLE `stu_score` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` INT NOT NULL COMMENT '成绩任务 ID',
  `student_id` INT NOT NULL COMMENT '学生 ID',
  `score_status` VARCHAR(20) NOT NULL DEFAULT 'SCORE_NOT_ENTERED' COMMENT 'SCORE_NORMAL/ABSENT/NOT_ENTERED',
  `score_value` DECIMAL(6,1) NULL COMMENT '分数；NORMAL 必填，ABSENT/NOT_ENTERED 必须为空',
  `entered_by` BIGINT NULL COMMENT '最后录入人 sys_user.user_id',
  `entered_at` DATETIME NULL COMMENT '最后录入时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_score` (`task_id`, `student_id`) COMMENT '任务×学生唯一',
  KEY `idx_stu_score_task_status` (`task_id`, `score_status`) COMMENT '任务录入/缺考统计',
  KEY `idx_stu_score_student` (`student_id`) COMMENT '学生成绩',
  CONSTRAINT `ck_stu_score_status` CHECK (score_status IN ('SCORE_NORMAL','SCORE_ABSENT','SCORE_NOT_ENTERED')),
  CONSTRAINT `ck_stu_score_value` CHECK ((score_status='SCORE_NORMAL' AND score_value IS NOT NULL AND score_value >= 0 AND score_value <= 300 AND score_value*2 = FLOOR(score_value*2)) OR (score_status IN ('SCORE_ABSENT','SCORE_NOT_ENTERED') AND score_value IS NULL)),
  CONSTRAINT `ck_stu_score_value_max` CHECK (score_value IS NULL OR score_value <= 300)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩记录（任务×学生）';

-- stu_publication：班级成绩发布当前状态（考试×班级）；模块 N06；DR: DR-PUB-001
 -- 设计说明：当前状态唯一；历史版本不可变。
CREATE TABLE `stu_publication` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` INT NOT NULL COMMENT '考试 ID',
  `class_id` INT NOT NULL COMMENT '班级 ID',
  `publication_status` VARCHAR(20) NOT NULL DEFAULT 'PUB_UNPUBLISHED' COMMENT 'PUB_UNPUBLISHED/PUBLISHED/WITHDRAWN',
  `current_version_id` INT NULL COMMENT '当前发布版本 ID（逻辑关联，循环依赖用 ALTER 加物理 FK）',
  `published_at` DATETIME NULL COMMENT '当前版本发布时间',
  `withdrawn_at` DATETIME NULL COMMENT '撤回时间',
  `withdraw_reason` VARCHAR(500) NULL COMMENT '撤回原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_publication` (`exam_id`, `class_id`) COMMENT '考试×班级唯一',
  KEY `idx_stu_publication_status` (`publication_status`) COMMENT '发布状态查询',
  CONSTRAINT `ck_stu_publication_status` CHECK (publication_status IN ('PUB_UNPUBLISHED','PUB_PUBLISHED','PUB_WITHDRAWN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级成绩发布当前状态（考试×班级）';

-- stu_publication_version：不可变发布版本头；模块 N06；DR: DR-PUB-001
 -- 设计说明：只插入，不更新/删除；当前版本由 stu_publication.current_version_id 指向。
CREATE TABLE `stu_publication_version` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `publication_id` INT NOT NULL COMMENT '发布 ID',
  `version_no` INT NOT NULL COMMENT '版本号，从 1 递增',
  `based_on_version_id` INT NULL COMMENT '更正/重发所基于的版本',
  `snapshot_at` DATETIME NOT NULL COMMENT '快照时间',
  `published_by` BIGINT NOT NULL COMMENT '发布操作人 sys_user.user_id',
  `change_reason` VARCHAR(500) NULL COMMENT '发布/更正原因',
  `snapshot_hash` CHAR(64) NULL COMMENT '快照 SHA-256（应用计算）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_pub_version` (`publication_id`, `version_no`) COMMENT '发布×版本号唯一',
  KEY `idx_stu_pub_version_pub` (`publication_id`, `version_no`) COMMENT '版本查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='不可变发布版本头';

-- stu_publication_version_detail：不可变发布版本成绩明细；模块 N06；DR: DR-PUB-001
 -- 设计说明：不引用可变 stu_score，避免后续更正污染历史；只插入不更新。
CREATE TABLE `stu_publication_version_detail` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `publication_version_id` INT NOT NULL COMMENT '发布版本 ID',
  `student_id` INT NOT NULL COMMENT '学生 ID',
  `subject_id` INT NOT NULL COMMENT '科目 ID',
  `score_status` VARCHAR(20) NOT NULL COMMENT '快照三态',
  `score_value` DECIMAL(6,1) NULL COMMENT '快照分数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_pub_detail` (`publication_version_id`, `student_id`, `subject_id`) COMMENT '版本×学生×科目唯一',
  KEY `idx_stu_pub_detail_student` (`student_id`, `subject_id`) COMMENT '学生历史成绩',
  CONSTRAINT `ck_stu_pub_detail_status` CHECK (score_status IN ('SCORE_NORMAL','SCORE_ABSENT','SCORE_NOT_ENTERED')),
  CONSTRAINT `ck_stu_pub_detail_value` CHECK ((score_status='SCORE_NORMAL' AND score_value IS NOT NULL AND score_value >= 0 AND score_value*2 = FLOOR(score_value*2)) OR (score_status IN ('SCORE_ABSENT','SCORE_NOT_ENTERED') AND score_value IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='不可变发布版本成绩明细';

-- stu_audit_log：业务变更审计（只插入）；模块 N08；DR: DR-AUDIT-001
 -- 设计说明：不建物理外键，支持多态对象；不依赖 sys_oper_log。
CREATE TABLE `stu_audit_log` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_type` VARCHAR(40) NOT NULL COMMENT '事件类型（如 SCORE_UPDATE/PUBLISH/CORRECTION）',
  `object_type` VARCHAR(40) NOT NULL COMMENT '对象类型（TASK/SCORE/PUBLICATION等）',
  `object_id` BIGINT NOT NULL COMMENT '对象 ID，多态引用',
  `object_version` INT NULL COMMENT '对象版本',
  `before_data` LONGTEXT NULL COMMENT '变更前 JSON（不存密码/Token）',
  `after_data` LONGTEXT NULL COMMENT '变更后 JSON',
  `reason` VARCHAR(500) NULL COMMENT '原因',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人 sys_user.user_id（逻辑关联）',
  `operator_role` VARCHAR(32) NOT NULL COMMENT '操作角色',
  `occurred_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  `source` VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '来源 WEB/IMPORT/SYSTEM',
  `trace_id` VARCHAR(64) NULL COMMENT '链路追踪 ID',
  PRIMARY KEY (`id`),
  KEY `idx_stu_audit_object` (`object_type`, `object_id`, `occurred_at`) COMMENT '对象审计',
  KEY `idx_stu_audit_operator` (`operator_user_id`, `occurred_at`) COMMENT '操作人审计',
  KEY `idx_stu_audit_event` (`event_type`, `occurred_at`) COMMENT '事件审计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务变更审计（只插入）';

-- stu_account_binding：账号—学生/班主任档案绑定；模块 N02；DR: DR-AUTH-001
 -- 设计说明：user_id 不建物理 FK（sys_user 为若依原生表，软删除/迁移风险），但字段类型必须 BIGINT；服务层校验账号存在且未被停用。
CREATE TABLE `stu_account_binding` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '若依账号 ID，与 sys_user.user_id 同类型',
  `profile_type` VARCHAR(20) NOT NULL COMMENT 'STUDENT/HEAD_TEACHER',
  `student_id` INT NULL COMMENT '学生档案 ID；STUDENT 时必填',
  `head_teacher_id` INT NULL COMMENT '班主任档案 ID；HEAD_TEACHER 时必填',
  `binding_status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REVOKED',
  `effective_from` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `effective_to` DATETIME NULL COMMENT '失效时间',
  `active_user_key` VARCHAR(80) GENERATED ALWAYS AS (CASE WHEN binding_status='ACTIVE' THEN CAST(user_id AS CHAR) ELSE NULL END) STORED COMMENT '账号唯一槽位（生成列；不论业务类型）',
  `active_profile_key` VARCHAR(80) GENERATED ALWAYS AS (CASE WHEN binding_status='ACTIVE' THEN CONCAT(profile_type,':',COALESCE(student_id,head_teacher_id)) ELSE NULL END) STORED COMMENT '档案唯一槽位（生成列）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人 sys_user.user_id',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人 sys_user.user_id',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_binding_active_user` (`active_user_key`) COMMENT '一个账号最多一个有效绑定（不论类型）',
  UNIQUE KEY `uk_stu_binding_active_profile` (`active_profile_key`) COMMENT '一个档案最多一个有效账号',
  KEY `idx_stu_binding_user` (`user_id`, `binding_status`) COMMENT '账号绑定查询',
  KEY `idx_stu_binding_student` (`student_id`, `binding_status`) COMMENT '学生绑定查询',
  KEY `idx_stu_binding_teacher` (`head_teacher_id`, `binding_status`) COMMENT '班主任绑定查询',
  CONSTRAINT `ck_stu_binding_profile` CHECK ((profile_type='STUDENT' AND student_id IS NOT NULL AND head_teacher_id IS NULL) OR (profile_type='HEAD_TEACHER' AND head_teacher_id IS NOT NULL AND student_id IS NULL)),
  CONSTRAINT `ck_stu_binding_status` CHECK (binding_status IN ('ACTIVE','REVOKED')),
  CONSTRAINT `ck_stu_binding_time` CHECK (effective_to IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号—学生/班主任档案绑定';


-- 外键：业务内部稳定父子关系使用物理 FK；sys_user 绑定使用逻辑关联；审计多态不建 FK。
ALTER TABLE `stu_term` ADD CONSTRAINT `fk_stu_term_year` FOREIGN KEY (`year_id`) REFERENCES `stu_year` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学年
ALTER TABLE `stu_class` ADD CONSTRAINT `fk_stu_class_year` FOREIGN KEY (`year_id`) REFERENCES `stu_year` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学年
ALTER TABLE `stu_class` ADD CONSTRAINT `fk_stu_class_grade` FOREIGN KEY (`grade_id`) REFERENCES `stu_grade` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 年级
ALTER TABLE `stu_student_class` ADD CONSTRAINT `fk_stu_student_class_student` FOREIGN KEY (`student_id`) REFERENCES `stu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学生
ALTER TABLE `stu_student_class` ADD CONSTRAINT `fk_stu_student_class_class` FOREIGN KEY (`class_id`) REFERENCES `stu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班级
ALTER TABLE `stu_head_teacher_assignment` ADD CONSTRAINT `fk_stu_hta_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `stu_head_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班主任
ALTER TABLE `stu_head_teacher_assignment` ADD CONSTRAINT `fk_stu_hta_class` FOREIGN KEY (`class_id`) REFERENCES `stu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班级
ALTER TABLE `stu_exam` ADD CONSTRAINT `fk_stu_exam_year` FOREIGN KEY (`year_id`) REFERENCES `stu_year` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学年
ALTER TABLE `stu_exam` ADD CONSTRAINT `fk_stu_exam_term` FOREIGN KEY (`term_id`) REFERENCES `stu_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学期
ALTER TABLE `stu_exam_class` ADD CONSTRAINT `fk_stu_exam_class_exam` FOREIGN KEY (`exam_id`) REFERENCES `stu_exam` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 考试
ALTER TABLE `stu_exam_class` ADD CONSTRAINT `fk_stu_exam_class_class` FOREIGN KEY (`class_id`) REFERENCES `stu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班级
ALTER TABLE `stu_exam_subject` ADD CONSTRAINT `fk_stu_exam_subject_exam` FOREIGN KEY (`exam_id`) REFERENCES `stu_exam` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 考试
ALTER TABLE `stu_exam_subject` ADD CONSTRAINT `fk_stu_exam_subject_subject` FOREIGN KEY (`subject_id`) REFERENCES `stu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 科目
ALTER TABLE `stu_score_task` ADD CONSTRAINT `fk_stu_score_task_exam` FOREIGN KEY (`exam_id`) REFERENCES `stu_exam` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 考试
ALTER TABLE `stu_score_task` ADD CONSTRAINT `fk_stu_score_task_class` FOREIGN KEY (`class_id`) REFERENCES `stu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班级
ALTER TABLE `stu_score_task` ADD CONSTRAINT `fk_stu_score_task_subject` FOREIGN KEY (`subject_id`) REFERENCES `stu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 科目
ALTER TABLE `stu_score_task` ADD CONSTRAINT `fk_stu_score_task_owner` FOREIGN KEY (`first_owner_teacher_id`) REFERENCES `stu_head_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 第一责任人
ALTER TABLE `stu_score` ADD CONSTRAINT `fk_stu_score_task` FOREIGN KEY (`task_id`) REFERENCES `stu_score_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 成绩任务
ALTER TABLE `stu_score` ADD CONSTRAINT `fk_stu_score_student` FOREIGN KEY (`student_id`) REFERENCES `stu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学生
ALTER TABLE `stu_publication` ADD CONSTRAINT `fk_stu_publication_exam` FOREIGN KEY (`exam_id`) REFERENCES `stu_exam` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 考试
ALTER TABLE `stu_publication` ADD CONSTRAINT `fk_stu_publication_class` FOREIGN KEY (`class_id`) REFERENCES `stu_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班级
ALTER TABLE `stu_publication_version` ADD CONSTRAINT `fk_stu_pub_version_publication` FOREIGN KEY (`publication_id`) REFERENCES `stu_publication` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 发布
ALTER TABLE `stu_publication_version` ADD CONSTRAINT `fk_stu_pub_version_based` FOREIGN KEY (`based_on_version_id`) REFERENCES `stu_publication_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 基于版本
ALTER TABLE `stu_publication_version_detail` ADD CONSTRAINT `fk_stu_pub_detail_version` FOREIGN KEY (`publication_version_id`) REFERENCES `stu_publication_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 版本
ALTER TABLE `stu_publication_version_detail` ADD CONSTRAINT `fk_stu_pub_detail_student` FOREIGN KEY (`student_id`) REFERENCES `stu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学生
ALTER TABLE `stu_publication_version_detail` ADD CONSTRAINT `fk_stu_pub_detail_subject` FOREIGN KEY (`subject_id`) REFERENCES `stu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 科目
ALTER TABLE `stu_account_binding` ADD CONSTRAINT `fk_stu_binding_student` FOREIGN KEY (`student_id`) REFERENCES `stu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学生档案
ALTER TABLE `stu_account_binding` ADD CONSTRAINT `fk_stu_binding_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `stu_head_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 班主任档案
ALTER TABLE `stu_publication` ADD CONSTRAINT `fk_stu_publication_current_version` FOREIGN KEY (`current_version_id`) REFERENCES `stu_publication_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 循环依赖：表创建后补加
