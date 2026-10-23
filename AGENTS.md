# 项目协作规则

1. 修改代码前先阅读 `README.md`、`docs/design/` 和当前需求，不把尚未确认的学生业务模块写成已实现功能。
2. 不随意升级 Spring Boot、Vue、Vite、Element Plus、Pinia 或其他核心版本；升级必须说明动机、兼容风险和验证结果。
3. 新前端页面使用 Vue 3 Composition API、Element Plus 和 Pinia，不引入 Vue 2、Element UI 或 Vetur 专用写法。
4. 不破坏若依现有的登录、JWT、角色、菜单、数据权限和按钮权限机制。
5. 业务模块优先复用若依的权限、菜单、字典和代码生成功能；生成代码必须经过人工审查。
6. 数据库结构变更必须同步保存到 `database/`，说明执行顺序、回滚方式和兼容范围。新增业务表使用 `id INT AUTO_INCREMENT` 主键，除非设计评审明确批准例外。
7. 功能修改应包含相称的自动化测试或可复现验证；未执行的检查不得标为通过。
8. 修改 `ruoyi-common`、`ruoyi-framework` 或共享前端设施前，先评估所有 Maven 模块和页面的影响。
9. 不提交真实数据库密码、Token 密钥、个人信息、私有配置、日志、构建产物或缓存。提交前进行敏感信息扫描。
10. 保持 Fedora 与 Windows 的可复现启动说明；共享配置不得包含个人绝对路径。

补充约束：不删除上游许可证；不在 `frontend/` 创建嵌套 Git 仓库；不擅自迁移到 Spring Boot 4、RuoYi-Cloud 或衍生框架；不要提前生成学生、教师、课程、班级和成绩模块。
