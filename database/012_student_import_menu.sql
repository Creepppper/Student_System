-- ============================================================
-- 阶段七：学生 Excel 导入的按钮权限
-- 目标数据库：student_system
--
-- 内容：在“学生信息”菜单下新增按钮权限 system:student:import（导入、下载模板）。
-- 说明：
--   - 可重复执行：按 perms 判断，已存在则跳过；
--   - 不修改表结构；超级管理员默认拥有该权限，其他角色需在角色管理中授权（班主任不授予）；
--   - 依赖 002（学生信息菜单）。
--
-- 执行：sudo mariadb student_system < database/012_student_import_menu.sql
-- 回滚：见文件末尾。
-- ============================================================

SET NAMES utf8mb4;

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生导入', p.menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'system:student:import', '#', 'admin', sysdate(), '', null, 'Excel 批量导入学生'
from (select menu_id from sys_menu where perms = 'system:student:list' limit 1) p
where not exists (select 1 from sys_menu where perms = 'system:student:import');

-- ============================================================
-- 回滚（需要时取消注释执行）
-- ============================================================
-- delete from sys_role_menu where menu_id in (select menu_id from sys_menu where perms = 'system:student:import');
-- delete from sys_menu where perms = 'system:student:import';

-- ============================================================
-- 验证
-- ============================================================
-- select menu_id, menu_name, parent_id, perms from sys_menu where perms = 'system:student:import';
