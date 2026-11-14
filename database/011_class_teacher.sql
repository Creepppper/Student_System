-- ============================================================
-- 阶段六：班主任负责班级（数据权限映射）
-- 目标数据库：student_system
--
-- 内容：
--   1. 新增表 stu_class_teacher：班主任账号 与 负责的年级、班级；
--   2. 在“系统管理”下新增“班主任班级”菜单，权限 system:classteacher:*。
--
-- 说明：
--   - 主键按课程要求使用 id INT AUTO_INCREMENT；不使用物理外键，沿用项目约定，由业务代码校验；
--   - 同一班主任不能重复负责同一班级（唯一索引）；
--   - 使用 CREATE TABLE IF NOT EXISTS，重复执行不会覆盖已有数据；
--   - 菜单按 perms 判重，可重复执行。
--
-- 执行：sudo mariadb student_system < database/011_class_teacher.sql
-- 回滚：见文件末尾。
-- ============================================================

SET NAMES utf8mb4;

create table if not exists stu_class_teacher (
  id          int          not null auto_increment comment '主键',
  user_id     bigint       not null                comment '班主任账号（sys_user.user_id），需拥有班主任角色',
  grade       varchar(20)  not null                comment '负责年级（与 stu_student.grade 一致）',
  class_name  varchar(50)  not null                comment '负责班级（与 stu_student.class_name 一致）',
  create_time datetime     not null default current_timestamp comment '创建时间',
  primary key (id),
  unique key uk_stu_class_teacher_user_class (user_id, grade, class_name),
  key idx_stu_class_teacher_grade_class (grade, class_name)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='班主任负责班级表';

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '班主任班级', 1, 7, 'classteacher', 'system/classteacher/index', 1, 0, 'C', '0', '0', 'system:classteacher:list', '#', 'admin', sysdate(), '', null, '分配班主任负责的年级班级'
from dual
where not exists (select 1 from sys_menu where perms = 'system:classteacher:list');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '班主任班级新增', p.menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'system:classteacher:add', '#', 'admin', sysdate(), '', null, ''
from (select menu_id from sys_menu where perms = 'system:classteacher:list' limit 1) p
where not exists (select 1 from sys_menu where perms = 'system:classteacher:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '班主任班级删除', p.menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'system:classteacher:remove', '#', 'admin', sysdate(), '', null, ''
from (select menu_id from sys_menu where perms = 'system:classteacher:list' limit 1) p
where not exists (select 1 from sys_menu where perms = 'system:classteacher:remove');

-- ============================================================
-- 回滚（需要时取消注释执行；会删除所有班主任分配记录）
-- ============================================================
-- delete from sys_menu where perms like 'system:classteacher:%';
-- drop table if exists stu_class_teacher;

-- ============================================================
-- 验证
-- ============================================================
-- select menu_id, menu_name, perms from sys_menu where perms like 'system:classteacher:%';
