-- 为成绩管理业务菜单设置现有 SVG 图标；在 002 至 005、008、011 之后执行。
-- 只替换未设置的图标（#、空或 NULL），保留已自定义的图标；可重复执行。
-- 仅修改菜单显示，不改变表结构、菜单权限或路由。适用于 MariaDB / MySQL。
-- 执行：mariadb student_system < database/016_business_menu_icons.sql
-- 如需回滚，仅在确认以下图标由本脚本设置后执行：
-- UPDATE sys_menu SET icon = '#' WHERE menu_type = 'C' AND (
--   (perms = 'system:student:list' AND icon = 'education') OR
--   (perms = 'system:exam:list' AND icon = 'date') OR
--   (perms = 'system:score:list' AND icon = 'table') OR
--   (perms = 'system:review:list' AND icon = 'documentation') OR
--   (perms = 'system:score:stat' AND icon = 'chart') OR
--   (perms = 'system:classteacher:list' AND icon = 'people'));
SET NAMES utf8mb4;
UPDATE sys_menu
SET icon = CASE perms
  WHEN 'system:student:list' THEN 'education'
  WHEN 'system:exam:list' THEN 'date'
  WHEN 'system:score:list' THEN 'table'
  WHEN 'system:review:list' THEN 'documentation'
  WHEN 'system:score:stat' THEN 'chart'
  WHEN 'system:classteacher:list' THEN 'people'
END
WHERE menu_type = 'C' AND (icon IS NULL OR icon IN ('', '#'))
  AND perms IN ('system:student:list', 'system:exam:list', 'system:score:list',
                'system:review:list', 'system:score:stat', 'system:classteacher:list');
