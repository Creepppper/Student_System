# 04 现有若依 API 核实与复用清单

> 依据：真实 Controller/注解/`SecurityConfig`/`PermissionService`/`AjaxResult`/`TableDataInfo`/前端 `request.js`。  
> **后端未运行**：8080 连接失败；以下为源码级核实，不是运行验证。

## 1. 统计口径核实

源码扫描结果：

| 项 | 数量 |
| --- | ---: |
| Controller 类 | **25** |
| Mapping 注解 | **146** |
| 其中 BaseController | 1（0 个 Mapping，抽象基类） |
| TestController | 1（5 个 Mapping，演示用） |
| 运行时验证 | 0（服务未启动） |

- 25 个 Controller 中包含 `BaseController`（无映射）和 `TestController`（演示）；
- 146 是 `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping` 注解数量；
- 本轮未发现需要删重后改变 146 计数的重复映射；多个 Controller 可共享根路径（如 `/`），但没有把 `/dev-api` 当后端路径；
- **/dev-api 是 Vite 开发代理前缀**，实际 Spring 路由见下。

## 2. Controller 分组

| Controller | 前缀 | Mapping 数 | 主要功能 | 权限 |
| --- | --- | ---: | --- | --- |
| `CaptchaController` | `/captchaImage` | 1 | 验证码 | 匿名 |
| `SysLoginController` | `/` | 3 | `/login`、`getInfo`、`getRouters` | 登录 |
| `SysRegisterController` | `/` | 1 | 注册 | 匿名/配置 |
| `SysUserController` | `/system/user` | 13 | 用户、授权角色、部门树 | `system:user:*` |
| `SysRoleController` | `/system/role` | 15 | 角色、数据范围、授权 | `system:role:*` |
| `SysMenuController` | `/system/menu` | 8 | 菜单/权限树 | `system:menu:*` |
| `SysDeptController` | `/system/dept` | 7 | 部门树 | `system:dept:*` |
| `SysPostController` | `/system/post` | 7 | 岗位 | `system:post:*` |
| `SysDictTypeController` | `/system/dict/type` | 8 | 字典类型 | `system:dict:*` |
| `SysDictDataController` | `/system/dict/data` | 7 | 字典数据 | `system:dict:*` |
| `SysConfigController` | `/system/config` | 8 | 参数 | `system:config:*` |
| `SysNoticeController` | `/system/notice` | 9 | 通知 | `system:notice:*` |
| `SysProfileController` | `/system/user/profile` | 4 | 个人资料/头像/密码 | 登录本人 |
| `SysIndexController` | `/` | 1 | 锁屏解锁 | 登录 |
| `SysLogininforController` | `/monitor/logininfor` | 5 | 登录日志/解锁 | `monitor:logininfor:*` |
| `SysOperlogController` | `/monitor/operlog` | 4 | 操作日志 | `monitor:operlog:*` |
| `SysUserOnlineController` | `/monitor/online` | 2 | 在线用户 | `monitor:online:*` |
| `CacheController` | `/monitor/cache` | 7 | 缓存 | `monitor:cache:list` |
| `ServerController` | `/monitor/server` | 1 | 服务器 | `monitor:server:list` |
| `SysJobController` | `/monitor/job` | 8 | 定时任务 | `monitor:job:*` |
| `SysJobLogController` | `/monitor/jobLog` | 5 | 任务日志 | `monitor:job:*` |
| `GenController` | `/tool/gen` | 13 | 代码生成 | `tool:gen:*` |
| `CommonController` | `/common` | 4 | 上传/下载 | 登录 |
| `TestController` | `/test/user` | 5 | 演示 | 演示 |
| `BaseController` | — | 0 | 抽象基类 | — |

## 3. 关键现有接口

| 方法 | 实际路径 | Controller/方法 | 请求/响应 | 权限 |
| --- | --- | --- | --- | --- |
| GET | `/captchaImage` | `CaptchaController#getCode` | 验证码 JSON | 匿名 |
| POST | `/login` | `SysLoginController#login` | `LoginBody` → `AjaxResult` | 匿名 |
| GET | `/getInfo` | `SysLoginController#getInfo` | → `AjaxResult` | 登录 |
| GET | `/getRouters` | `SysLoginController#getRouters` | → `AjaxResult` | 登录 |
| POST | `/logout` | Spring Security LogoutFilter | → 退出结果 | 登录 |
| GET | `/system/user/list` | `SysUserController#list` | 分页 → `TableDataInfo` | `system:user:list` |
| GET | `/system/user/{userId}` | `SysUserController#getInfo` | → `AjaxResult` | `system:user:query` |
| POST/PUT/DELETE | `/system/user`、`/system/user/{ids}` | `SysUserController` | CRUD | `system:user:*` |
| GET | `/system/role/list` | `SysRoleController#list` | `TableDataInfo` | `system:role:list` |
| GET | `/system/menu/list` | `SysMenuController#list` | `AjaxResult` | `system:menu:list` |
| GET | `/system/dept/list` | `SysDeptController#list` | `AjaxResult` | `system:dept:list` |
| GET | `/system/dict/data/type/{dictType}` | `SysDictDataController` | 字典 | 登录 |
| GET | `/tool/gen/list` | `GenController#list` | 代码生成表 | `tool:gen:list` |

> `getInfo`/`getRouters` 方法上未写前导 `/`，Spring 解析后的实际路径仍是 `/getInfo`、`/getRouters`；不要写成 `/system/getInfo`。

## 4. 认证/安全事实

- `SecurityConfig`：CSRF 禁用、STATELESS、JWT filter、`/login`、`/register`、`/captchaImage`、Swagger、Druid 放行；
- `JwtAuthenticationTokenFilter`：从 TokenService 取 LoginUser，写入 SecurityContext；
- `@PreAuthorize("@ss.hasPermi('...')")` 做功能权限；
- `@DataScope` 做若依部门数据范围；
- **admin 在 `SysPermissionService` 被加入 `ALL_PERMISSION`**，不能只靠 `@PreAuthorize` 阻断成绩；
- `AjaxResult`：`code/msg/data`；`TableDataInfo`：`total/rows/code/msg`；
- `GlobalExceptionHandler`：403 返回“没有权限”；其他业务异常通常 `AjaxResult.error`；
- 前端 `request.js` 读 Cookie `Admin-Token`，加 `Authorization: Bearer`，处理 401/500/601。

## 5. 复用清单

| 能力 | 现有接口 | 复用方式 | 不能做什么 |
| --- | --- | --- | --- |
| 登录/验证码/登出 | `/captchaImage`、`/login`、`/logout` | 直接复用 | 不另建登录 |
| 当前用户/角色/权限 | `/getInfo` | 直接复用 | 不把角色当学生身份 |
| 动态菜单/路由 | `/getRouters` | 直接复用；业务菜单写 `sys_menu` | 不硬编码业务菜单 |
| 用户/角色/菜单/部门/岗位 | `/system/*` | 直接复用 RBAC/组织辅助 | 用户不是学生档案；部门不是班级 |
| 字典/参数/通知 | `/system/dict`、`/system/config`、`/system/notice` | 复用显示/配置 | 不替代业务状态机 |
| 日志/在线/缓存 | `/monitor/*` | 系统监控 | 不替代业务审计 |
| 代码生成 | `/tool/gen/*` | 后续生成 CRUD 骨架 | 不直接生成状态机/权限/审计 |
| 文件上传下载 | `/common/*` | P1 导入导出可复用 | 不传敏感成绩到公共目录 |

## 6. 与业务 API 的边界

- 现有接口均不是学生业务 API；`/system/user` 是账号管理，不是学生档案；
- 新业务统一 `/business/**`，不复用 `/system/**` 命名；
- 新 API 的权限统一 `business:*`；
- 现有 25/146 数量不包含 68 个 P0 规划 API和11 个 P1 规划 API；
- 运行验证待后端启动后进行；本文件不把源码扫描称为运行时 Swagger 验证。 
