-- ============================================================
-- 设计草稿，尚未执行，尚未部署，需评审后使用。
-- P1 扩展表（复核/导入），不属于 P0 退出条件；状态码待 P1 评审。
-- 不包含 DROP、数据变更、用户/权限变更。
-- ============================================================
SET NAMES utf8mb4;

-- stu_review_application：P1 成绩复核申请（设计占位）；模块 N09；DR: DR-REVIEW-001
 -- 设计说明：P1 表，不属于 P0；状态代码和时限待 P1 评审，不在本阶段冻结。
CREATE TABLE `stu_review_application` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` INT NOT NULL COMMENT '学生 ID',
  `exam_id` INT NOT NULL COMMENT '考试 ID',
  `subject_id` INT NOT NULL COMMENT '科目 ID',
  `score_id` INT NOT NULL COMMENT '成绩记录 ID（申请时快照/逻辑关联）',
  `publication_version_id` INT NOT NULL COMMENT '申请时的发布版本 ID',
  `reason` VARCHAR(500) NOT NULL COMMENT '申请理由',
  `deadline_at` DATETIME NOT NULL COMMENT '申请截止时间（P1 规则待评审）',
  `review_status` VARCHAR(20) NULL COMMENT 'P1 状态待评审，不在本阶段固化',
  `handled_by` BIGINT NULL COMMENT '处理人 sys_user.user_id',
  `result_comment` VARCHAR(500) NULL COMMENT '处理结论',
  `closed_at` DATETIME NULL COMMENT '关闭时间',
  `active_key` VARCHAR(80) GENERATED ALWAYS AS (CASE WHEN closed_at IS NULL THEN CONCAT(student_id,':',exam_id,':',subject_id) ELSE NULL END) STORED COMMENT '活跃申请唯一槽位',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` BIGINT NULL COMMENT '创建人',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` BIGINT NULL COMMENT '更新人',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_review_active` (`active_key`) COMMENT '同一学生+考试+科目最多一个活跃申请',
  KEY `idx_stu_review_student` (`student_id`, `review_status`) COMMENT '学生复核',
  KEY `idx_stu_review_deadline` (`deadline_at`) COMMENT '时限'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='P1 成绩复核申请（设计占位）';

-- stu_review_action：P1 复核处理动作（设计占位）；模块 N09；DR: P1 扩展
 -- 设计说明：P1 表，不属于 P0。
CREATE TABLE `stu_review_action` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id` INT NOT NULL COMMENT '复核申请 ID',
  `action_type` VARCHAR(32) NOT NULL COMMENT 'P1 动作类型待评审',
  `actor_user_id` BIGINT NOT NULL COMMENT '处理人 sys_user.user_id',
  `actor_role` VARCHAR(32) NOT NULL COMMENT '处理角色',
  `comment` VARCHAR(500) NULL COMMENT '处理意见',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stu_review_action` (`review_id`, `created_at`) COMMENT '处理历史'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='P1 复核处理动作（设计占位）';

-- stu_import_batch：P1 成绩导入批次（设计占位）；模块 N09；DR: P1 扩展
 -- 设计说明：P1 表；趋势/对比为查询视图，不建表。
CREATE TABLE `stu_import_batch` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` INT NOT NULL COMMENT '成绩任务 ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '导入文件名',
  `imported_by` BIGINT NOT NULL COMMENT '导入人 sys_user.user_id',
  `total_rows` INT NOT NULL DEFAULT 0 COMMENT '总行数',
  `success_rows` INT NOT NULL DEFAULT 0 COMMENT '成功行数',
  `failed_rows` INT NOT NULL DEFAULT 0 COMMENT '失败行数',
  `batch_status` VARCHAR(20) NULL COMMENT 'P1 状态待评审',
  `error_summary` LONGTEXT NULL COMMENT '错误摘要（不含敏感信息）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stu_import_task` (`task_id`, `created_at`) COMMENT '任务导入历史'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='P1 成绩导入批次（设计占位）';


-- 外键：业务内部稳定父子关系使用物理 FK；sys_user 绑定使用逻辑关联；审计多态不建 FK。
ALTER TABLE `stu_review_application` ADD CONSTRAINT `fk_stu_review_student` FOREIGN KEY (`student_id`) REFERENCES `stu_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 学生
ALTER TABLE `stu_review_application` ADD CONSTRAINT `fk_stu_review_exam` FOREIGN KEY (`exam_id`) REFERENCES `stu_exam` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 考试
ALTER TABLE `stu_review_application` ADD CONSTRAINT `fk_stu_review_subject` FOREIGN KEY (`subject_id`) REFERENCES `stu_subject` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 科目
ALTER TABLE `stu_review_application` ADD CONSTRAINT `fk_stu_review_score` FOREIGN KEY (`score_id`) REFERENCES `stu_score` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 成绩
ALTER TABLE `stu_review_application` ADD CONSTRAINT `fk_stu_review_version` FOREIGN KEY (`publication_version_id`) REFERENCES `stu_publication_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 发布版本
ALTER TABLE `stu_review_action` ADD CONSTRAINT `fk_stu_review_action_review` FOREIGN KEY (`review_id`) REFERENCES `stu_review_application` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 复核申请
ALTER TABLE `stu_import_batch` ADD CONSTRAINT `fk_stu_import_task` FOREIGN KEY (`task_id`) REFERENCES `stu_score_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 成绩任务
