-- 菜单 SQL，由 ruoyi-generator 生成后人工调整
-- 生成器原始上级菜单是 3（系统工具），这里改为 1（系统管理）。
-- 只执行一次；重复执行会重复插入菜单和按钮。
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请', '1', '1', 'review', 'system/review/index', 1, 0, 'C', '0', '0', 'system:review:list', '#', 'admin', sysdate(), '', null, '查卷申请菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:review:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:review:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:review:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:review:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('查卷申请导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:review:export',       '#', 'admin', sysdate(), '', null, '');