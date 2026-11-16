-- 开启若依的自助注册开关；依赖 010 中的学生、班主任角色。
-- 适用于 MariaDB / MySQL；可重复执行，不改变表结构。
-- 执行：mariadb student_system < database/020_enable_self_registration.sql
-- 回滚：UPDATE sys_config SET config_value='false' WHERE config_key='sys.account.registerUser';
-- 修改数据库后需重启后端以刷新参数缓存。
UPDATE sys_config SET config_value='true' WHERE config_key='sys.account.registerUser';
