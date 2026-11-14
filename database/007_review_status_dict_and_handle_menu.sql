-- ============================================================
-- 阶段二：查卷申请状态流转
-- 目标数据库：student_system
--
-- 内容：
--   1. 新增字典 stu_review_status（待处理/已受理/已驳回/已完成），供列表显示和筛选使用；
--   2. 在“查卷申请”菜单下新增按钮权限 system:review:handle（受理/驳回/完成）。
--
-- 说明：
--   - 本脚本可重复执行：字典和菜单按 dict_type / perms 判断是否已存在，已存在则跳过。
--   - 不修改 stu_review 表结构，不改动已有数据。
--   - 执行后需在“角色管理”中把 system:review:handle 授权给班主任角色；
--     admin 账号默认拥有全部权限，无需额外授权。
--
-- 执行：sudo mariadb student_system < database/007_review_status_dict_and_handle_menu.sql
-- 回滚：见文件末尾的注释块。
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 字典类型
-- ------------------------------------------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
select '查卷申请状态', 'stu_review_status', '0', 'admin', sysdate(), '', null, '查卷申请处理状态'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'stu_review_status');

-- ------------------------------------------------------------
-- 2. 字典数据（值与 stu_review.status 一致）
-- ------------------------------------------------------------
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 1, '待处理', '0', 'stu_review_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '新提交，等待班主任处理'
from dual
where not exists (select 1 from sys_dict_data where dict_type = 'stu_review_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 2, '已受理', '1', 'stu_review_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '班主任已受理，正在核查'
from dual
where not exists (select 1 from sys_dict_data where dict_type = 'stu_review_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 3, '已驳回', '2', 'stu_review_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '申请被驳回，已填写处理意见'
from dual
where not exists (select 1 from sys_dict_data where dict_type = 'stu_review_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
select 4, '已完成', '3', 'stu_review_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '核查完成，已填写处理意见'
from dual
where not exists (select 1 from sys_dict_data where dict_type = 'stu_review_status' and dict_value = '3');

-- ------------------------------------------------------------
-- 3. 处理按钮权限，挂在“查卷申请”菜单（perms = system:review:list）下
--    若该菜单不存在（未执行 005），子查询为空，插入 0 行，需先执行 005。
-- ------------------------------------------------------------
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '查卷申请处理', m.menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'system:review:handle', '#', 'admin', sysdate(), '', null, '受理/驳回/完成'
from (select menu_id from sys_menu where perms = 'system:review:list' limit 1) m
where not exists (select 1 from sys_menu where perms = 'system:review:handle');

-- ============================================================
-- 回滚（需要时取消注释执行；会删除按钮权限和字典，请先确认没有角色仍依赖它们）
-- ============================================================
-- delete from sys_role_menu where menu_id in (select menu_id from sys_menu where perms = 'system:review:handle');
-- delete from sys_menu where perms = 'system:review:handle';
-- delete from sys_dict_data where dict_type = 'stu_review_status';
-- delete from sys_dict_type where dict_type = 'stu_review_status';

-- ============================================================
-- 验证
-- ============================================================
-- select dict_value, dict_label from sys_dict_data where dict_type = 'stu_review_status' order by dict_sort;
-- select menu_id, menu_name, perms from sys_menu where perms = 'system:review:handle';
