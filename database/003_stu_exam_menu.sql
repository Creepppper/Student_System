-- 菜单 SQL，由 ruoyi-generator 生成后人工调整
-- 生成器原始上级菜单是 3（系统工具），这里改为 1（系统管理）。
-- 只执行一次；重复执行会重复插入菜单和按钮。
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息', '1', '1', 'exam', 'system/exam/index', 1, 0, 'C', '0', '0', 'system:exam:list', '#', 'admin', sysdate(), '', null, '考试信息菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:exam:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:exam:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:exam:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:exam:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('考试信息导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:exam:export',       '#', 'admin', sysdate(), '', null, '');