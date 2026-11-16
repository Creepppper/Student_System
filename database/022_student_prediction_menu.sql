-- 在 010 和 017 之后执行；兼容 MariaDB/MySQL，可重复执行。
-- 回滚：先删除 perms='student:prediction:my' 菜单的 sys_role_menu 授权，再删除该菜单。
SET NAMES utf8mb4;
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,update_by,update_time,remark)
SELECT '高考预估', p.menu_id, 3, 'prediction', 'student/prediction/index', 'StudentPrediction', 1, 0, 'C', '0', '0', 'student:prediction:my', 'chart', 'admin', sysdate(), '', NULL, '学生查看本人高考预估'
FROM (SELECT menu_id FROM sys_menu WHERE menu_name='学生端' AND parent_id=0 AND menu_type='M' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='student:prediction:my');
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT r.role_id,m.menu_id FROM sys_role r JOIN sys_menu m ON m.perms='student:prediction:my'
WHERE r.role_key='student' AND r.del_flag='0'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=r.role_id AND x.menu_id=m.menu_id);
