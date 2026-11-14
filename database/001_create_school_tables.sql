-- ============================================================
-- 学生管理系统 - 高中理科班成绩管理业务表
-- 目标数据库：student_system
-- 适用环境：MariaDB 11.8 / MySQL 8.x
--
-- 说明：
--   1. 本脚本只创建 4 张业务表，不插入任何测试数据。
--   2. 不包含 DROP TABLE，不修改、清空已有数据。
--   3. 使用 CREATE TABLE IF NOT EXISTS，重复执行不会覆盖已有表；
--      已存在表的字段差异不会被自动修正，需人工检查后另写迁移脚本。
--   4. 建表需要 DDL 权限，请使用数据库管理员账号执行；
--      应用账号 student_app 只有 SELECT/INSERT/UPDATE/DELETE 权限。
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 学生信息表
-- ------------------------------------------------------------
create table if not exists stu_student (
  id           int         not null auto_increment comment '学生ID',
  student_no   varchar(32) not null                comment '学号',
  student_name varchar(50) not null                comment '学生姓名',
  grade        varchar(20) not null default ''     comment '年级（如高一、高二、高三）',
  class_name   varchar(50) not null default ''     comment '班级名称（如1班、2班）',
  user_id      bigint      default null            comment '关联若依用户ID（sys_user.user_id），NULL表示未绑定',
  primary key (id),
  unique key uk_stu_student_no (student_no),
  unique key uk_stu_student_user_id (user_id),
  key idx_stu_student_name (student_name),
  key idx_stu_student_class (class_name),
  key idx_stu_student_grade_class (grade, class_name)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='学生信息表';

-- ------------------------------------------------------------
-- 2. 考试信息表
-- ------------------------------------------------------------
create table if not exists stu_exam (
  id        int          not null auto_increment comment '考试ID',
  exam_name varchar(100) not null                comment '考试名称（如月考、期中、期末）',
  exam_time date         not null                comment '考试日期',
  primary key (id),
  key idx_stu_exam_name (exam_name),
  key idx_stu_exam_time (exam_time)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='考试信息表';

-- ------------------------------------------------------------
-- 3. 学生成绩表
--    一个学生在一次考试中只有一行，六科成绩横向存放。
--    总分和排名不落库，查询时用 SQL 计算。
-- ------------------------------------------------------------
create table if not exists stu_score (
  id              int          not null auto_increment comment '成绩ID',
  student_id      int          not null                comment '学生ID（stu_student.id）',
  exam_id         int          not null                comment '考试ID（stu_exam.id）',
  chinese         decimal(5,2) default null            comment '语文成绩（满分150；NULL未录入，缺考为0）',
  math            decimal(5,2) default null            comment '数学成绩（满分150；NULL未录入，缺考为0）',
  english         decimal(5,2) default null            comment '英语成绩（满分150；NULL未录入，缺考为0）',
  physics         decimal(5,2) default null            comment '物理成绩（满分100；NULL未录入，缺考为0）',
  chemistry       decimal(5,2) default null            comment '化学成绩（满分100；NULL未录入，缺考为0）',
  biology         decimal(5,2) default null            comment '生物成绩（满分100；NULL未录入，缺考为0）',
  absent_subjects varchar(100) not null default ''     comment '缺考科目代码，多个用英文逗号分隔（chinese,math,english,physics,chemistry,biology）',
  primary key (id),
  unique key uk_stu_score_student_exam (student_id, exam_id),
  key idx_stu_score_exam_id (exam_id)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='学生成绩表';

-- ------------------------------------------------------------
-- 4. 查卷申请表
-- ------------------------------------------------------------
create table if not exists stu_review (
  id          int          not null auto_increment comment '查卷申请ID',
  student_id  int          not null                comment '申请学生ID（stu_student.id）',
  exam_id     int          not null                comment '考试ID（stu_exam.id）',
  subject     varchar(20)  not null                comment '查卷科目代码（chinese/math/english/physics/chemistry/biology）',
  reason      varchar(500) not null                comment '申请理由',
  status      char(1)      not null default '0'    comment '状态（0待处理 1已受理 2已驳回 3已完成）',
  reply       varchar(500) default null            comment '班主任处理意见',
  create_time datetime     not null default current_timestamp comment '创建时间',
  update_time datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (id),
  key idx_stu_review_student_exam (student_id, exam_id),
  key idx_stu_review_exam_id (exam_id),
  key idx_stu_review_status (status)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='查卷申请表';

-- 建表结束：不插入测试数据。
