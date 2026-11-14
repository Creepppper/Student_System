-- ============================================================
-- 阶段三：成绩统计（考试排名、学生成绩单）
-- 目标数据库：student_system
--
-- 内容：在“系统管理”下新增“成绩统计”菜单，权限 system:score:stat。
-- 排名和成绩单都是查询时计算，不新增表、不改表结构。
--
-- 说明：
--   - 可重复执行：按 perms 判断，已存在则跳过。
--   - 依赖 001 建表；不依赖 005/007。
--   - 执行后需在“角色管理”中把 system:score:stat 授权给需要查看统计的角色；
--     admin 默认拥有全部权限。
--
-- 执行：sudo mariadb student_system < database/008_score_stat_menu.sql
-- 回滚：见文件末尾。
-- ============================================================

SET NAMES utf8mb4;

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '成绩统计', 1, 6, 'scorestat', 'system/score/stat', 1, 0, 'C', '0', '0', 'system:score:stat', '#', 'admin', sysdate(), '', null, '考试排名与学生成绩单'
from dual
where not exists (select 1 from sys_menu where perms = 'system:score:stat');

-- ============================================================
-- 回滚（需要时取消注释执行）
-- ============================================================
-- delete from sys_role_menu where menu_id in (select menu_id from sys_menu where perms = 'system:score:stat');
-- delete from sys_menu where perms = 'system:score:stat';

-- ============================================================
-- 验证
-- ============================================================
-- select menu_id, menu_name, parent_id, path, component, perms from sys_menu where perms = 'system:score:stat';
