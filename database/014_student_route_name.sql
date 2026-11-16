-- 在 010 之后执行；为学生信息菜单指定唯一的路由名称，避免与学生端目录冲突。
-- 不改变路径、菜单权限或表结构。适用于 MariaDB 和 MySQL，可重复执行。
-- 执行：mariadb student_system < database/014_student_route_name.sql
-- 回滚：update sys_menu set route_name='' where perms='system:student:list'
--       and path='student' and component='system/student/index' and route_name='StudentManagement';
-- 回滚会恢复原来的路由名冲突。
UPDATE sys_menu SET route_name = 'StudentManagement'
WHERE perms = 'system:student:list' AND path = 'student'
  AND component = 'system/student/index' AND (route_name IS NULL OR route_name = '');
