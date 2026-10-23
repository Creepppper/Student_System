# 学生管理系统

本仓库是电子科技大学软件工程课程“学生管理系统”的开发底座。当前阶段只完成 RuoYi-Vue Spring Boot 3 后端、官方 RuoYi-Vue3 前端、数据库初始化方案和实验二概要设计材料，不包含尚未评审的学生、教师、课程、班级或成绩业务模块。

## 技术栈与已选版本

| 层次 | 技术 | 版本或来源 |
| --- | --- | --- |
| 后端 | RuoYi-Vue / Spring Boot | RuoYi 3.9.2，Spring Boot 3.5.16，`springboot3` 分支 |
| Java | OpenJDK / Maven | 源码目标 Java 17；本机待以 JDK 25.0.4.1 实测；Maven 3.9.11 |
| 前端 | Vue / Vite / Element Plus / Pinia | Vue 3.5.26、Vite 6.4.1、Element Plus 2.13.1、Pinia 3.0.4 |
| 数据库 | MariaDB | 本机 11.8.8，应用使用 MySQL Connector/J |
| 缓存 | Valkey | 本机 9.0.6，Spring Data Redis + Lettuce，使用 Redis 协议 |
| 开发工具 | VS Code | Java Extension Pack、Spring Boot、Maven、Vue - Official |

后端上游为 <https://github.com/yangzongzhuan/RuoYi-Vue>，导入提交为 `a51a838b71b446ea27256900efe7ed2faa2a02fd`。前端上游为 <https://github.com/yangzongzhuan/RuoYi-Vue3>，导入提交为 `838965c5a18d2c61b73ec30c6e288057aaa08b63`。许可证分别保留在根目录 `LICENSE` 和 `frontend/LICENSE`。完整来源记录见 `docs/reference/upstream.md`。

## 目录结构

```text
Student_System/
├── ruoyi-admin/       # 启动模块、Controller、配置
├── ruoyi-common/      # 通用常量、工具、领域基类和 Redis 封装
├── ruoyi-framework/   # Spring Security、JWT、Druid、MyBatis 等框架配置
├── ruoyi-generator/   # Velocity 代码生成器，含 Vue 3 JS/TS 模板
├── ruoyi-quartz/      # Quartz 定时任务
├── ruoyi-system/      # 用户、角色、菜单、部门等系统服务
├── sql/               # 若依官方基础 SQL 和 Quartz SQL
├── frontend/          # 官方 RuoYi-Vue3
├── database/          # 本项目数据库初始化辅助脚本
├── docs/              # 部署、设计、测试和课程材料记录
└── .vscode/           # 可共享的任务、调试和扩展建议
```

## 首次配置

复制 `.env.example` 为 Git 已忽略的 `.env.local`，填写本机值并限制文件权限。项目启动脚本会读取该文件；直接运行 JAR 时则需在当前终端导出同名变量。`.env.local` 使用 shell 兼容的 `KEY='value'` 格式，禁止提交。

```bash
cp .env.example .env.local
chmod 600 .env.local
# 使用编辑器填写 DB_PASSWORD 和 TOKEN_SECRET，不要在命令行参数中传密码
```

数据库初始化脚本通过 Fedora 的 `sudo mariadb`/unix_socket 管理员认证，只创建 `student_system` 和仅有该库 CRUD 权限的 `student_app` 本地账号，不修改 root 认证。首次运行会导入上游 SQL；同名数据库及两个应用账号都已存在时只验证连接和表结构，不会重新导入。若只存在部分对象，脚本会停止并要求人工检查，避免覆盖数据。

```bash
./database/init_database.sh
```

脚本按 `sql/ry_20260417.sql`、`sql/quartz.sql` 的顺序导入。数据库默认使用 `utf8mb4_unicode_ci`；若已有同名数据库，先人工确认归属，不要删除或重建。

Valkey 默认连接 `127.0.0.1:6379`、数据库 0。可先执行：

```bash
valkey-cli -h 127.0.0.1 -p 6379 ping
```

预期响应为 `PONG`。项目不要求安装第二套 Redis。

## 构建与启动

后端源码以 Java 17 为目标。本机可以先用现有 JDK 25 构建；只有实际出现编译或运行不兼容时，才为本项目单独配置 JDK 17。

Maven 统一使用默认的用户仓库 `~/.m2/repository/`，项目命令、脚本和 VS Code 任务均不再设置 `maven.repo.local`。不要在项目中创建第二份 Maven 依赖缓存。

推荐使用脚本，它们通过脚本自身位置定位项目根目录。后端启动脚本会读取 `.env.local`，将日志和上传目录分别定位到项目 `logs/`、`data/uploadPath/`，检查必要变量及 8080 端口，然后以前台进程启动；不会使用 sudo 或自动终止其他进程。

```bash
./scripts/check-env.sh
./scripts/build-backend.sh
./scripts/start-backend.sh

# 等价的直接构建命令
mvn test
mvn clean package
```

前端上游没有锁文件，本项目首次安装后已生成并保留唯一的 `package-lock.json`；此后所有成员统一使用 `npm ci`，只有明确调整依赖时才使用 `npm install` 并审查锁文件差异。

```bash
cd frontend && npm ci
cd ..
./scripts/start-frontend.sh
cd frontend && npm run build:prod
```

Windows 使用 `scripts\build-backend.cmd`、`scripts\start-backend.cmd` 和 `scripts\start-frontend.cmd`。密码通过当前 PowerShell/CMD 会话的环境变量提供；Windows 脚本不会读取 Bash 格式的 `.env.local`。

默认开发地址：

- 前端：`http://localhost:3000`（可由 `VITE_DEV_PORT` 或命令行覆盖）
- 后端：`http://localhost:8080`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- Druid 控制台：默认关闭；确需使用时通过环境变量启用并设置独立口令

若 3000 或 8080 已占用，先使用 `ss -lntp` 定位进程，不要直接终止其他项目。可通过 `VITE_DEV_PORT` 或 `./scripts/start-frontend.sh --port <端口>` 选择其他端口，并记录实际地址。

## VS Code

推荐扩展见 `.vscode/extensions.json`。Java 扩展宿主 JDK、Maven 构建 JDK和后端运行 JDK是三个不同概念：Java 扩展可使用较新的 JDK 运行语言服务器；Maven 和 Spring Boot 进程则以启动它们的终端 `JAVA_HOME` 为准。检查命令如下：

```bash
java -version
mvn -version
code --list-extensions --show-versions
```

本仓库不写入个人 JDK 绝对路径。需要 Java 17 时，在当前终端或 VS Code 工作区会话中设置 `JAVA_HOME`，不要修改系统全局默认版本。

## 代码生成器

`ruoyi-generator` 已参与 Maven 聚合构建。当前源码通过 `tplWebType=element-plus` 选择 `vm/vue/v3/` 模板，可生成 Vue 3 + Element Plus 的 JavaScript 页面；还存在独立的 `element-plus-typescript` 模板，但本项目不主动切换 TypeScript。

后续新增业务表时先完成数据库评审，并按老师要求使用名为 `id` 的 `INT AUTO_INCREMENT` 主键。生成前检查字段注释、Java 类型、查询方式、表单控件和权限前缀；生成结果应先下载审查，再分别放入后端业务模块和 `frontend/src`。不得把 Vue 2 模板输出直接并入 Vue 3 前端。代码生成器是 Velocity 模板工具，不是 AI。

## Windows + MySQL + Redis 注意事项

- 使用 JDK 17，并在 PowerShell 会话内设置项目所需的 `JAVA_HOME`；不要覆盖其他项目的 JDK。
- Fedora 专用的 `database/init_database.sh` 使用 `sudo mariadb`，Windows 成员不要执行。应使用本机 MySQL 管理工具创建独立的 `student_system` 数据库和最小权限账号，再按 `sql/ry_20260417.sql`、`sql/quartz.sql` 的顺序导入。
- MySQL 8 可使用相同表结构；通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 指向各自环境。若启用了 TLS，应调整 JDBC 参数并配置本机证书，不要把证书和密码提交仓库。
- Redis 与 Valkey使用相同协议；通过 `VALKEY_HOST`、`VALKEY_PORT`、`VALKEY_DATABASE`、`VALKEY_PASSWORD` 配置，不要求组员安装 Valkey。
- PowerShell 在当前会话设置 `$env:DB_URL`、`$env:DB_USERNAME`、`$env:DB_PASSWORD`、`$env:TOKEN_SECRET` 及 Redis 变量，然后使用 `scripts\build-backend.cmd`、`scripts\start-backend.cmd`。不要把真实值写入共享脚本。
- 所有项目路径由脚本相对位置推导；`LOG_PATH` 和 `RUOYI_PROFILE` 未设置时分别使用项目内的 `logs`、`data\uploadPath`，不要求与 Fedora 使用相同目录。
- SQL 文件使用 UTF-8。导入前确认目标数据库为 `student_system`，不得对其他数据库运行脚本。
- Windows 组合尚未在本轮实机验证，验收状态应保持“待后续验证”。

## GitHub 协作注意事项

- 必须提交源码、`pom.xml`、`frontend/package.json`、`frontend/package-lock.json`、SQL 初始化脚本、README、设计文档、启动脚本、许可证和共享 `.vscode` 配置。
- 不提交 `.env.local`、数据库密码、Token 密钥、日志、上传文件、数据库导出、Maven/npm 缓存、`node_modules`、`target` 或 `dist`。
- 老师原始课件保留在本机 `docs/reference/materials/`，仓库只提交已整理的 `docs/reference/实验二要求提取.md`。旧版 `doc/若依环境使用手册.docx` 含过时的固定路径，也不提交。
- 克隆后先复制 `.env.example` 并建立自己的私有配置；Linux/Fedora 使用 `.env.local`，Windows 使用当前 PowerShell/CMD 环境变量。
- `main` 分支保持可构建；成员从功能分支开发并通过 Pull Request 合并，不直接提交构建产物或真实业务数据。

## 上游更新策略

本项目最终只保留一个 Git 仓库。后端官方仓库登记为 `upstream`；前端不保留嵌套 `.git`，更新时在临时目录拉取 RuoYi-Vue3，记录新旧提交，审查差异后再复制到 `frontend/`。不要把小组仓库的 `origin` 指向官方仓库。

实验二设计初稿见 `docs/design/概要设计说明书.md`，环境与测试证据分别见 `docs/deployment/环境与部署记录.md` 和 `docs/testing/验收记录.md`。
