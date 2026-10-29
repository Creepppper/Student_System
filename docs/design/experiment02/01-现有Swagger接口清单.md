# 01 现有 Swagger/API 清单

> 结论：**后端未运行，未取得 `/v3/api-docs` 实际响应**。  
> 本清单来自当前源码的 Controller 注解（Spring MVC），不是对运行中 Swagger 的截图/响应；接口存在性有源码依据，运行行为仍需启动后验证。

## 1. API 文档配置

| 项 | 配置 |
| --- | --- |
| 实现 | Springdoc OpenAPI **2.9.0**（非 Swagger2） |
| API JSON | `/v3/api-docs` |
| UI | `/swagger-ui.html` |
| 分组 | `default`，扫描 `com.ruoyi.web.controller`、`com.ruoyi.generator.controller`、`com.ruoyi.quartz.controller` |
| 认证放行 | `SecurityConfig` 允许 `/swagger-ui.html`、`/v3/api-docs/**`、`/swagger-ui/**` |
| 当前 8080 | **未运行**，`curl` 连接失败，未取得 JSON |

## 2. Controller 总览

源码扫描：**25 个 Controller，146 个 Mapping 注解**（含 1 个演示用 `TestController`、1 个无 Mapping 的 `BaseController`）。

| Controller | 前缀 | Mapping 数 | 认证/权限 |
| --- | --- | ---: | --- |
| `CaptchaController` | `/captchaImage` | 1 | 匿名 |
| `SysLoginController` | `/` | 3 | `/login` 匿名；`getInfo/getRouters` 需登录 |
| `SysRegisterController` | `/` | 1 | 匿名（若依配置开关） |
| `SysUserController` | `/system/user` | 13 | `system:user:*` |
| `SysRoleController` | `/system/role` | 15 | `system:role:*` |
| `SysMenuController` | `/system/menu` | 8 | `system:menu:*`，部分树查询需登录 |
| `SysDeptController` | `/system/dept` | 7 | `system:dept:*` |
| `SysPostController` | `/system/post` | 7 | `system:post:*` |
| `SysDictTypeController` | `/system/dict/type` | 8 | `system:dict:*`，部分查询登录即可 |
| `SysDictDataController` | `/system/dict/data` | 7 | `system:dict:*`，按类型查询登录即可 |
| `SysConfigController` | `/system/config` | 8 | `system:config:*`，按 key 查询登录即可 |
| `SysNoticeController` | `/system/notice` | 9 | `system:notice:*`，部分登录即可 |
| `SysProfileController` | `/system/user/profile` | 4 | 登录用户本人 |
| `SysIndexController` | `/` | 1 | 登录（锁屏解锁） |
| `SysLogininforController` | `/monitor/logininfor` | 5 | `monitor:logininfor:*` |
| `SysOperlogController` | `/monitor/operlog` | 4 | `monitor:operlog:*` |
| `SysUserOnlineController` | `/monitor/online` | 2 | `monitor:online:*` |
| `CacheController` | `/monitor/cache` | 7 | `monitor:cache:list` |
| `ServerController` | `/monitor/server` | 1 | `monitor:server:list` |
| `SysJobController` | `/monitor/job` | 8 | `monitor:job:*` |
| `SysJobLogController` | `/monitor/jobLog` | 5 | `monitor:job:*` |
| `GenController` | `/tool/gen` | 13 | `tool:gen:*` |
| `CommonController` | `/common` | 4 | 登录/上传下载 |
| `TestController` | `/test/user` | 5 | 仅演示，不应作为业务接口 |
| `BaseController` | — | 0 | 抽象基类 |

## 3. 关键接口清单

### 3.1 认证/初始化

| 方法 | 路径 | Controller | 功能 | 认证/权限 |
| --- | --- | --- | --- | --- |
| GET | `/captchaImage` | `CaptchaController` | 获取验证码 | 匿名 |
| POST | `/login` | `SysLoginController` | 登录，返回 Token | 匿名 |
| GET | `/getInfo` | `SysLoginController` | 用户/角色/权限 | 登录 |
| GET | `/getRouters` | `SysLoginController` | 动态路由菜单 | 登录 |
| POST | `/logout` | Spring Security LogoutFilter | 退出 | 登录 |
| POST | `/register` | `SysRegisterController` | 注册（若开启） | 匿名/配置 |

### 3.2 用户管理

| 方法 | 路径 | 权限 | 功能 |
| --- | --- | --- | --- |
| GET | `/system/user/list` | `system:user:list` | 分页用户列表 |
| GET | `/system/user/{userId}` | `system:user:query` | 用户详情 |
| POST | `/system/user` | `system:user:add` | 新增用户 |
| PUT | `/system/user` | `system:user:edit` | 修改用户 |
| DELETE | `/system/user/{userIds}` | `system:user:remove` | 删除用户 |
| PUT | `/system/user/resetPwd` | `system:user:resetPwd` | 重置密码 |
| PUT | `/system/user/changeStatus` | `system:user:edit` | 启停 |
| GET | `/system/user/authRole/{userId}` | `system:user:query` | 查询授权角色 |
| PUT | `/system/user/authRole` | `system:user:edit` | 保存授权角色 |
| GET | `/system/user/deptTree` | `system:user:list` | 部门树 |

### 3.3 角色/菜单/部门/岗位

| 方法 | 路径 | 权限 | 功能 |
| --- | --- | --- | --- |
| GET | `/system/role/list` | `system:role:list` | 角色列表 |
| PUT | `/system/role/dataScope` | `system:role:edit` | 数据范围 |
| GET | `/system/menu/list` | `system:menu:list` | 菜单树 |
| GET | `/system/menu/treeselect` | 登录 | 菜单树下拉 |
| GET | `/system/dept/list` | `system:dept:list` | 部门列表 |
| GET | `/system/post/list` | `system:post:list` | 岗位列表 |

### 3.4 字典/参数/通知

| 方法 | 路径 | 权限 | 功能 |
| --- | --- | --- | --- |
| GET | `/system/dict/type/list` | `system:dict:list` | 字典类型分页 |
| GET | `/system/dict/data/type/{dictType}` | 登录 | 按类型查字典 |
| GET | `/system/config/list` | `system:config:list` | 参数分页 |
| GET | `/system/config/configKey/{configKey}` | 登录 | 按 key 查参数 |
| GET | `/system/notice/list` | `system:notice:list` | 通知分页 |

### 3.5 监控/工具/通用

| 方法 | 路径 | 权限 | 功能 |
| --- | --- | --- | --- |
| GET | `/monitor/server` | `monitor:server:list` | 服务器信息 |
| GET | `/monitor/cache` | `monitor:cache:list` | 缓存信息 |
| GET | `/monitor/online/list` | `monitor:online:list` | 在线用户 |
| GET | `/monitor/job/list` | `monitor:job:list` | 定时任务 |
| GET | `/monitor/operlog/list` | `monitor:operlog:list` | 操作日志 |
| GET | `/tool/gen/list` | `tool:gen:list` | 代码生成表 |
| POST | `/tool/gen/importTable` | `tool:gen:import` | 导入表 |
| POST | `/common/upload` | 登录 | 文件上传 |

## 4. 未实现的学生业务 API

以下接口**尚未存在**，只作为下一阶段概要设计的计划命名，不能写入“现有 Swagger”：

```text
/business/org/**          # 学年、学期、年级、班级
/business/students/**     # 学生档案/归属/账号
/business/head-teachers/**# 班主任/任职
/business/subjects/**     # 科目
/business/exams/**        # 考试/范围/科目
/business/score-tasks/**  # 任务/成绩/提交
/business/publications/** # 审核/发布/撤回/更正
/business/my-scores/**    # 学生本人查询
/business/stats/**        # 统计
/business/audit/**        # 业务变更记录
/business/reviews/**      # P1 复核
```

这些路径没有 Controller/Service/Mapper 源码依据，不得说已实现。

## 5. 待运行验证

服务未运行，因此以下未能验证：

- `/v3/api-docs` 实际 JSON；
- `/swagger-ui.html` 可访问性；
- `/captchaImage` 返回结构；
- 认证放行/401/403 的实际响应；
- 接口分页参数、统一响应结构在运行时的行为。

启动方式见 `README.md`/`docs/deployment`；本阶段不启动长期后台服务、不修改系统配置。
