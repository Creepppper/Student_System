-- 清理若依默认普通角色；在 010 之后执行，可重复执行。
-- 仅处理原始 role_id=2、role_key=common，且没有任何账号关联时才软删除。
-- 保留角色的菜单和部门授权以便回滚；不修改超级管理员或业务角色。
-- 执行：mariadb student_system < database/013_remove_default_common_role.sql
-- 执行后检查下方验证查询；若仍有普通角色，先检查其账号关联及角色标识。
-- 回滚：update sys_role set del_flag='0', update_by='', update_time=null
--       where role_id=2 and role_key='common' and del_flag='2'
--         and update_by='role_cleanup_013';
-- 兼容范围：若依原始角色 ID/标识及 MariaDB、MySQL；不改表结构。

UPDATE sys_role
SET del_flag = '2', update_by = 'role_cleanup_013', update_time = CURRENT_TIMESTAMP
WHERE role_id = 2 AND role_key = 'common' AND del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role WHERE role_id = 2);

-- 验证：select role_id, role_name, role_key, status from sys_role where del_flag='0';
-- 检查关联：select user_id from sys_user_role where role_id=2;
