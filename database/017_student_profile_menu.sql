-- 学生基本信息菜单和学生角色授权；依赖 010，可重复执行。
-- 不改表结构；仅影响学生端菜单及权限。兼容 MariaDB / MySQL。
-- 执行：mariadb student_system < database/017_student_profile_menu.sql
-- 回滚：
-- DELETE rm FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id=rm.menu_id WHERE m.perms='student:profile:my';
-- DELETE FROM sys_menu WHERE perms='student:profile:my';
SET NAMES utf8mb4;
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '基本信息', p.menu_id, 0, 'profile', 'student/profile/index', 'StudentProfile', 1, 0, 'C', '0', '0', 'student:profile:my', 'user', 'admin', sysdate(), '', NULL, '学生查看本人基本信息'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name='学生端' AND parent_id=0 AND menu_type='M' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='student:profile:my');
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT r.role_id,m.menu_id FROM sys_role r JOIN sys_menu m ON m.perms='student:profile:my'
WHERE r.role_key='student' AND r.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=r.role_id AND x.menu_id=m.menu_id);
