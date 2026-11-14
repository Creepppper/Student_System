-- 菜单 SQL，由 ruoyi-generator 生成后人工调整
-- 生成器原始上级菜单是 3（系统工具），这里改为 1（系统管理）。
-- 如果项目已有“教学管理”目录，请先在菜单管理中查到它的 menu_id，
-- 再把下面的 parent_id 改成该 ID；不要直接执行后不管菜单位置。
-- 只执行一次；重复执行会重复插入菜单和按钮。
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息', '1', '1', 'student', 'system/student/index', 1, 0, 'C', '0', '0', 'system:student:list', '#', 'admin', sysdate(), '', null, '学生信息菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:student:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:student:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:student:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:student:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('学生信息导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:student:export',       '#', 'admin', sysdate(), '', null, '');