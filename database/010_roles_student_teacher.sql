-- ============================================================
-- 阶段四、五：角色（班主任、学生）、学生端菜单、角色菜单授权
-- 目标数据库：student_system
--
-- 内容：
--   1. 新增角色：班主任（teacher）、学生（student）；
--   2. 新增“学生端”目录，以及“我的成绩”“我的查卷”“提交查卷”按钮；
--   3. 按角色授予菜单：班主任可管理本班学生、成绩、查卷，并查看统计；
--      学生只能访问学生端菜单。
--
-- 说明：
--   - 可重复执行：角色按 role_key、菜单按 perms/名称判重，已存在则跳过；
--   - 授权按 (role_id, menu_id) 判重；
--   - 不修改表结构，不改动已有数据；
--   - 依赖 001（学生表）、003/004/005（已生成的菜单）、007（查卷处理按钮）、008（成绩统计菜单）；
--     菜单缺失时对应授权会被跳过，请先执行前面的脚本；
--   - 班主任与学生角色的“数据范围”由后端代码控制（班级 / 本人），这里 data_scope 设为 5（仅本人），
--     与若依的 @DataScope 不冲突，因为本项目的业务数据过滤不使用 sys_dept。
--
-- 执行：sudo mariadb student_system < database/010_roles_student_teacher.sql
-- 回滚：见文件末尾。
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 角色
-- ------------------------------------------------------------
insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '班主任', 'teacher', 3, '5', 1, 1, '0', '0', 'admin', sysdate(), '', null, '按负责班级管理学生、成绩和查卷'
from dual
where not exists (select 1 from sys_role where role_key = 'teacher');

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '学生', 'student', 4, '5', 1, 1, '0', '0', 'admin', sysdate(), '', null, '查看本人成绩，提交查卷申请'
from dual
where not exists (select 1 from sys_role where role_key = 'student');

-- ------------------------------------------------------------
-- 2. 学生端菜单
-- ------------------------------------------------------------
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生端', 0, 4, 'student', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '学生端目录'
from dual
where not exists (select 1 from sys_menu where menu_name = '学生端' and parent_id = 0 and menu_type = 'M');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '我的成绩', p.menu_id, 1, 'myscore', 'student/score/index', 1, 0, 'C', '0', '0', 'student:score:my', 'list', 'admin', sysdate(), '', null, '学生查看本人成绩单'
from (select menu_id from sys_menu where menu_name = '学生端' and parent_id = 0 and menu_type = 'M' limit 1) p
where not exists (select 1 from sys_menu where perms = 'student:score:my');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '我的查卷', p.menu_id, 2, 'myreview', 'student/review/index', 1, 0, 'C', '0', '0', 'student:review:my', 'documentation', 'admin', sysdate(), '', null, '学生查看本人查卷申请'
from (select menu_id from sys_menu where menu_name = '学生端' and parent_id = 0 and menu_type = 'M' limit 1) p
where not exists (select 1 from sys_menu where perms = 'student:review:my');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交查卷', p.menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'student:review:apply', '#', 'admin', sysdate(), '', null, ''
from (select menu_id from sys_menu where perms = 'student:review:my' limit 1) p
where not exists (select 1 from sys_menu where perms = 'student:review:apply');

-- ------------------------------------------------------------
-- 3. 角色菜单授权
-- ------------------------------------------------------------
-- 班主任：系统管理目录 + 学生信息/学生成绩/查卷申请/成绩统计（含按钮）+ 考试信息（只读，用于下拉选择）
insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.menu_name = '系统管理' and m.parent_id = 0
where r.role_key = 'teacher'
  and not exists (select 1 from sys_role_menu x where x.role_id = r.role_id and x.menu_id = m.menu_id);

insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.perms in (
  'system:student:list', 'system:student:query',
  'system:score:list', 'system:score:query', 'system:score:add', 'system:score:edit', 'system:score:remove', 'system:score:export', 'system:score:stat',
  'system:review:list', 'system:review:query', 'system:review:handle', 'system:review:export',
  'system:exam:list', 'system:exam:query'
)
where r.role_key = 'teacher'
  and not exists (select 1 from sys_role_menu x where x.role_id = r.role_id and x.menu_id = m.menu_id);

-- 学生：学生端目录 + 我的成绩 + 我的查卷 + 提交查卷
insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.menu_name = '学生端' and m.parent_id = 0 and m.menu_type = 'M'
where r.role_key = 'student'
  and not exists (select 1 from sys_role_menu x where x.role_id = r.role_id and x.menu_id = m.menu_id);

insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.perms in ('student:score:my', 'student:review:my', 'student:review:apply')
where r.role_key = 'student'
  and not exists (select 1 from sys_role_menu x where x.role_id = r.role_id and x.menu_id = m.menu_id);

-- ============================================================
-- 回滚（需要时取消注释执行；会移除这两个角色的授权和学生端菜单，不删除业务数据）
-- ============================================================
-- delete from sys_role_menu where role_id in (select role_id from sys_role where role_key in ('teacher', 'student'));
-- delete from sys_user_role where role_id in (select role_id from sys_role where role_key in ('teacher', 'student'));
-- delete from sys_menu where perms in ('student:score:my', 'student:review:my', 'student:review:apply');
-- delete from sys_menu where menu_name = '学生端' and parent_id = 0 and menu_type = 'M';
-- delete from sys_role where role_key in ('teacher', 'student');

-- ============================================================
-- 验证
-- ============================================================
-- select role_id, role_name, role_key from sys_role where role_key in ('teacher', 'student');
-- select m.menu_name, m.perms from sys_role_menu rm join sys_menu m on m.menu_id = rm.menu_id join sys_role r on r.role_id = rm.role_id where r.role_key = 'teacher';
