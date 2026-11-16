-- 成绩管理菜单名称；在 004 后执行，可重复执行。
-- 兼容 MariaDB / MySQL，不修改权限、路由或表结构。
-- 执行：mariadb student_system < database/018_score_management_menu.sql
-- 回滚：UPDATE sys_menu SET menu_name='学生成绩' WHERE perms='system:score:list' AND menu_type='C' AND menu_name='成绩管理';
UPDATE sys_menu SET menu_name='成绩管理'
WHERE perms='system:score:list' AND menu_type='C' AND menu_name='学生成绩';
