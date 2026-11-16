# 高中理科班成绩管理数据库说明

本目录存放学生管理系统的数据库脚本。当前新增的是高中理科班成绩管理模块的 **4 张业务表**，只负责数据库结构与说明，不包含 Java、Vue 代码，也不包含测试数据。

## 1. 文件

| 文件 | 作用 |
| --- | --- |
| [001_create_school_tables.sql](001_create_school_tables.sql) | 创建 `stu_student`、`stu_exam`、`stu_score`、`stu_review` 四张表 |
| [002_stu_student_menu.sql](002_stu_student_menu.sql) | 代码生成器输出的学生菜单和按钮 SQL；执行前确认上级菜单和角色授权 |
| [003_stu_exam_menu.sql](003_stu_exam_menu.sql) | 考试信息菜单和按钮 SQL |
| [004_stu_score_menu.sql](004_stu_score_menu.sql) | 学生成绩菜单和按钮 SQL |
| [005_stu_review_menu.sql](005_stu_review_menu.sql) | 查卷申请菜单和按钮 SQL |
| [007_review_status_dict_and_handle_menu.sql](007_review_status_dict_and_handle_menu.sql) | 阶段二：新增查卷状态字典 `stu_review_status` 和“查卷申请处理”按钮权限 `system:review:handle`；依赖 005，可重复执行 |
| [008_score_stat_menu.sql](008_score_stat_menu.sql) | 阶段三：新增“成绩统计”菜单（权限 `system:score:stat`），排名与成绩单为查询计算，不改表；可重复执行 |
| [009_demo_students_scores.sql](009_demo_students_scores.sql) | 演示数据：56 名虚构学生（高一至高三各 3 个班）和两场演示考试的随机成绩；由 `demo/generate_demo_data.py` 生成，可重复执行，仅用于演示和测试 |
| [010_roles_student_teacher.sql](010_roles_student_teacher.sql) | 阶段四、五：新增“班主任”“学生”角色，新增学生端菜单（我的成绩、我的查卷、提交查卷），并按角色授权；可重复执行 |
| [011_class_teacher.sql](011_class_teacher.sql) | 阶段六：新增 `stu_class_teacher` 表（班主任负责班级）和“班主任班级”菜单；可重复执行 |
| [012_student_import_menu.sql](012_student_import_menu.sql) | 阶段七：学生 Excel 导入的按钮权限 `system:student:import`；可重复执行 |
| [013_remove_default_common_role.sql](013_remove_default_common_role.sql) | 在 010 之后执行：无账号关联时软删除若依默认普通角色；保留授权数据，可按脚本注释回滚 |
| [014_student_route_name.sql](014_student_route_name.sql) | 在 010 之后执行：修复学生信息与学生端目录的路由名称冲突；回滚方式见脚本 |
| [015_standard_grade_class.sql](015_standard_grade_class.sql) | 在 001、011 后检查旧数据，再由数据库管理员添加年级班级约束；执行顺序、兼容范围及回滚见脚本 |
| [016_business_menu_icons.sql](016_business_menu_icons.sql) | 在业务菜单脚本后执行：为未设置图标的业务菜单指定图标；回滚方式见脚本 |
| [017_student_profile_menu.sql](017_student_profile_menu.sql) | 在 010 后执行：新增学生基本信息菜单及学生角色授权；回滚方式见脚本 |
| [018_score_management_menu.sql](018_score_management_menu.sql) | 在 004 后执行：将学生成绩菜单改名为成绩管理；回滚方式见脚本 |
| [019_unique_class_teacher.sql](019_unique_class_teacher.sql) | 在 011 后执行：检查并解决重复分配后，新增年级班级唯一索引；执行和回滚见脚本 |
| [020_enable_self_registration.sql](020_enable_self_registration.sql) | 在 010 后执行：启用自助注册；修改配置后重启后端刷新缓存，回滚见脚本 |
| [006_hide_unrelated_menus.sql](006_hide_unrelated_menus.sql) | 隐藏与项目无关的若依菜单；只改 `visible`，不删表和数据 |
| [init_database.sh](init_database.sh) | 既有的若依基础库初始化脚本，负责创建 `student_system` 并导入 `sql/ry_20260417.sql`、`sql/quartz.sql` |
| [../sql/ry_20260417.sql](../sql/ry_20260417.sql) | 若依基础表与初始数据，不要修改 |
| [../sql/quartz.sql](../sql/quartz.sql) | Quartz 表，不要修改 |

## 2. 四张表

| 表 | 作用 | 关键字段 |
| --- | --- | --- |
| `stu_student` | 学生档案 | `student_no` 学号唯一、`student_name`、`grade` 年级、`class_name` 班级、`user_id` 可空，关联若依用户 |
| `stu_exam` | 考试信息 | `exam_name` 考试名称、`exam_time` 考试日期 |
| `stu_score` | 学生成绩 | `student_id`、`exam_id`、六科成绩、`absent_subjects` 缺考科目；`(student_id, exam_id)` 唯一 |
| `stu_review` | 查卷申请 | `student_id`、`exam_id`、`subject`、`reason`、`status`、`reply`、创建/更新时间 |

### 2.1 六科与满分

| 科目 | 字段 | 满分 | 缺考代码 |
| --- | --- | --- | --- |
| 语文 | `chinese` | 150 | `chinese` |
| 数学 | `math` | 150 | `math` |
| 英语 | `english` | 150 | `english` |
| 物理 | `physics` | 100 | `physics` |
| 化学 | `chemistry` | 100 | `chemistry` |
| 生物 | `biology` | 100 | `biology` |

六科成绩均为 `decimal(5,2)`，允许 `NULL`：

- `NULL` 表示**尚未录入**；
- `0` 表示**已录入且为 0 分，或该科缺考**；
- 某科缺考时，成绩记 `0`，并把科目代码写入 `absent_subjects`，例如 `chinese,physics`；
- 代码之间使用英文逗号，**不要加空格**，便于 `FIND_IN_SET` 查询。

## 3. 表间关系

```text
stu_student 1 ──── N  stu_score  N ──── 1  stu_exam
      │                                         │
      └────────── N  stu_review  N ─────────────┘

stu_student.user_id ──> sys_user.user_id（逻辑关联，可空，唯一）
```

- `stu_student` 与 `stu_score`：一对多，`stu_score.student_id`。
- `stu_exam` 与 `stu_score`：一对多，`stu_score.exam_id`。
- `stu_score` 是学生与考试的组合：一个学生一次考试只有一行，六科横向存放。
- `stu_student` 与 `stu_review`：一对多，`stu_review.student_id`。
- `stu_exam` 与 `stu_review`：一对多，`stu_review.exam_id`。
- `stu_student.user_id` 逻辑关联若依 `sys_user.user_id`，允许为 `NULL`；唯一索引保证一个系统账号最多绑定一个学生。MySQL/MariaDB 的唯一索引允许多个 `NULL`，因此“未绑定”的学生可以有多条。
- 不建物理外键，沿用若依基础表的“逻辑关联 + 索引 + 业务校验”方式。`sys_user` 账号被删除时，`stu_student.user_id` 需要后端清理或拦截。

**不需要“每个学生一张表”，也不需要“单科排名表”和“学生排名表”。** 所有学生的成绩都在 `stu_score` 中，按 `student_id`、`exam_id` 查询；排名在查询时用窗口函数计算，不落库。

## 4. 设计约定

1. 四张表的主键都是 `id int not null auto_increment`，符合课程要求。
2. 所有表和字段都有中文 `COMMENT`，便于若依代码生成器识别字段标签。
3. 不存总分、不存排名：总分是六科之和，排名用 `RANK()` 等窗口函数按考试、年级、班级计算。
4. 不建教师表、科目表、排名表、预测表；科目固定为六科，教师身份复用若依用户/角色。
5. 不建选课表；`stu_score` 中的一行表示“这个学生参加了这次考试并有成绩”。
6. 不使用物理外键、触发器和存储过程，保持简单、可移植。
7. 不插入测试数据；测试数据由开发和测试人员按需自行准备，且不得使用真实个人信息。
8. 本期 `stu_exam` 不单独加“考试年级”字段。如果考试按年级独立，建议先在 `exam_name` 中写清（如“高一期中”）；确实需要按年级过滤时，再通过后续迁移增加 `grade` 字段。

## 5. 执行方法

### 5.1 前置条件

- 已通过 [init_database.sh](init_database.sh) 创建 `student_system` 并导入若依基础表；
- 使用 MariaDB 11.8 或 MySQL 8.x；
- 使用**数据库管理员账号**执行建表。`student_app` 只有 `SELECT/INSERT/UPDATE/DELETE` 权限，没有 `CREATE` 权限。

### 5.2 Fedora / Linux

```bash
# 方式一：本机 sudo + unix_socket 管理员认证
sudo mariadb student_system < database/001_create_school_tables.sql

# 方式二：使用有 DDL 权限的账号，按提示输入密码
mariadb -u root -p student_system < database/001_create_school_tables.sql
```

### 5.3 Windows

在 CMD 中：

```cmd
mysql -u root -p student_system < database\001_create_school_tables.sql
```

在 PowerShell 中，如果 `<` 重定向不可用，可改用：

```powershell
Get-Content .\database\001_create_school_tables.sql -Raw | mysql -u root -p student_system
```

### 5.4 不要用应用账号建表

`student_app` 的权限由基础初始化脚本控制，只够业务读写。建表、改表应使用本机管理员账号，避免为了建表扩大应用账号权限。

### 5.5 关于 init_database.sh

[init_database.sh](init_database.sh) 目前把基础表数量校验写死为 `31`。新增 4 张业务表后，如果再次运行它做“表数量验证”，会得到 35 并判定失败。该脚本本轮不改；它只用于首次基础库初始化，业务表请用本文件中的 SQL 单独创建。后续如需让初始化脚本识别业务表，应另开任务修改校验逻辑，不要直接改共享脚本。

### 5.6 业务菜单 SQL

`002_stu_student_menu.sql`、`003_stu_exam_menu.sql`、`004_stu_score_menu.sql`、`005_stu_review_menu.sql` 是代码生成器输出的菜单和按钮 SQL。生成器原始 `parent_id` 是 3（系统工具），这些文件已改为 1（系统管理）；如果项目已有“教学管理”目录，请先查出它的 `menu_id` 并替换 `parent_id`。

确认后每个文件只执行一次：

```bash
sudo mariadb student_system < database/002_stu_student_menu.sql
sudo mariadb student_system < database/003_stu_exam_menu.sql
sudo mariadb student_system < database/004_stu_score_menu.sql
sudo mariadb student_system < database/005_stu_review_menu.sql
```

这些脚本不是可重复执行的迁移：重复执行会重复插入菜单和按钮。执行后需要在“角色管理”中给测试角色授权，管理员账号通常不需要额外授权。

### 5.7 隐藏无关菜单

[006_hide_unrelated_menus.sql](006_hide_unrelated_menus.sql) 只把 `sys_menu.visible` 改为 `1`，隐藏系统监控、若依官网、部门/岗位/字典/参数/通知/日志等菜单，**不删除任何表和数据**。学生信息、考试信息、学生成绩、查卷申请，以及用户/角色/菜单管理入口会保留；代码生成默认保留，便于实验三演示。

```bash
sudo mariadb student_system < database/006_hide_unrelated_menus.sql
```

执行后重新登录即可看到精简后的菜单。脚本末尾有恢复显示语句；如果之后要再演示代码生成，也可以把 `3`、`116` 恢复为显示。不要通过 `DROP TABLE` 删除 `sys_user`、`sys_role`、`sys_menu`、`sys_dept`、`sys_dict_*` 等表，它们承载登录、权限、菜单和字典，不是示例表。

## 6. 验证方法

执行后，用管理员账号检查四张表是否存在、注释和索引是否正确：

```sql
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = 'student_system'
  AND table_name IN ('stu_student', 'stu_exam', 'stu_score', 'stu_review');

SELECT table_name, column_name, column_type, is_nullable, column_default, column_comment
FROM information_schema.columns
WHERE table_schema = 'student_system'
  AND table_name = 'stu_score'
ORDER BY ordinal_position;

SHOW CREATE TABLE stu_score;
SHOW INDEX FROM stu_score;
```

预期结果：

- 4 张表都存在，表注释分别为“学生信息表”“考试信息表”“学生成绩表”“查卷申请表”；
- `stu_score` 有 `uk_stu_score_student_exam` 唯一索引，列顺序为 `(student_id, exam_id)`；
- `stu_student` 有 `uk_stu_student_no`、`uk_stu_student_user_id` 唯一索引；
- 四张表都没有物理外键。

检查执行前是否已存在同名表：

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'student_system'
  AND table_name IN ('stu_student', 'stu_exam', 'stu_score', 'stu_review');
```

如果有返回结果，先停止并人工确认。**不要删除已有表，不要用 `DROP TABLE` 重来。**

## 7. 重复执行与回滚

### 7.1 重复执行

脚本使用 `CREATE TABLE IF NOT EXISTS`：

- 表不存在：创建；
- 表已存在：跳过，不覆盖、不清空数据；
- 如果已有表结构与脚本不一致，脚本**不会**自动修改，需要人工对比 `SHOW CREATE TABLE` 后另写迁移脚本。

因此，重复执行是“安全但不会升级结构”的。不要把 `IF NOT EXISTS` 当成迁移工具。

### 7.2 回滚

本脚本不包含 `DROP TABLE`，也不会自动回滚。确需回滚时，先备份数据库，再由管理员按以下依赖顺序人工处理：

```text
stu_review -> stu_score -> stu_exam -> stu_student
```

不要在共享数据库、教师演示库或未备份的环境中直接删表。

## 8. 常用查询示例

以下 SQL 中的 `?` 是占位符，由后端 MyBatis 传入，不要直接当测试数据执行。

### 8.1 查看某个学生历次考试的六科成绩

```sql
SELECT e.exam_name,
       e.exam_time,
       sc.chinese,
       sc.math,
       sc.english,
       sc.physics,
       sc.chemistry,
       sc.biology,
       sc.absent_subjects
FROM stu_score sc
JOIN stu_exam e ON e.id = sc.exam_id
WHERE sc.student_id = ?
ORDER BY e.exam_time DESC, e.id DESC;
```

### 8.2 查看某次考试的总分和班级排名

```sql
SELECT t.exam_id,
       t.student_no,
       t.student_name,
       t.grade,
       t.class_name,
       t.total_score,
       RANK() OVER (
         PARTITION BY t.exam_id, t.grade, t.class_name
         ORDER BY t.total_score DESC
       ) AS class_rank
FROM (
  SELECT sc.exam_id,
         st.student_no,
         st.student_name,
         st.grade,
         st.class_name,
         COALESCE(sc.chinese, 0)
         + COALESCE(sc.math, 0)
         + COALESCE(sc.english, 0)
         + COALESCE(sc.physics, 0)
         + COALESCE(sc.chemistry, 0)
         + COALESCE(sc.biology, 0) AS total_score
  FROM stu_score sc
  JOIN stu_student st ON st.id = sc.student_id
  WHERE sc.exam_id = ?
) t
ORDER BY t.grade, t.class_name, class_rank;
```

说明：

- 按 `grade + class_name` 分组，避免不同年级的“1班”混在一起；
- `RANK()` 遇到同分同名次，下一名次会跳号；如果老师要求连续名次，可改用 `DENSE_RANK()`，要求严格顺序可用 `ROW_NUMBER()`；
- 缺考科目按 0 计入总分；如果某科是 `NULL`（未录入），也会按 0 计算，因此总分排名前应先确认六科成绩完整。

### 8.3 查看某次考试单科排名

以语文为例：

```sql
SELECT st.student_no,
       st.student_name,
       st.grade,
       st.class_name,
       sc.chinese,
       RANK() OVER (
         PARTITION BY sc.exam_id, st.grade, st.class_name
         ORDER BY sc.chinese DESC
       ) AS chinese_rank
FROM stu_score sc
JOIN stu_student st ON st.id = sc.student_id
WHERE sc.exam_id = ?
  AND sc.chinese IS NOT NULL
  AND FIND_IN_SET('chinese', sc.absent_subjects) = 0
ORDER BY st.grade, st.class_name, chinese_rank;
```

说明：

- 上面的写法**排除**该科缺考学生；
- 如果老师要求缺考也参与排名（按 0 分排），去掉 `FIND_IN_SET` 条件即可；
- 其他科目把 `chinese` 换成 `math`、`english`、`physics`、`chemistry`、`biology`。

### 8.4 查询某科缺考学生

```sql
SELECT sc.student_id,
       st.student_no,
       st.student_name,
       sc.exam_id,
       sc.absent_subjects
FROM stu_score sc
JOIN stu_student st ON st.id = sc.student_id
WHERE FIND_IN_SET('physics', sc.absent_subjects) > 0;
```

### 8.5 查卷申请

学生提交：

```sql
INSERT INTO stu_review (student_id, exam_id, subject, reason, status)
VALUES (?, ?, 'math', ?, '0');
```

班主任受理或驳回：

```sql
-- 受理
UPDATE stu_review
SET status = '1', reply = ?
WHERE id = ? AND status = '0';

-- 驳回
UPDATE stu_review
SET status = '2', reply = ?
WHERE id = ? AND status = '0';
```

`create_time`、`update_time` 由数据库默认值维护；如果后端也显式设置，不冲突。

## 9. 数据库支持与后端职责

| 需求 | 数据库支持 | 需要后端实现 |
| --- | --- | --- |
| 班主任录入、修改、删除成绩 | `stu_score` 的 CRUD；`(student_id, exam_id)` 唯一约束防止重复 | 班主任角色/权限、班级数据范围、成绩范围校验、缺考与 0 分一致性 |
| 学生查看历次考试六科成绩 | `stu_score JOIN stu_exam`，按 `student_id` 查询 | 根据当前登录用户查 `stu_student.user_id`；未绑定时拒绝访问 |
| 查询各科成绩 | 六科字段直接查询 | 科目代码到中文名的映射、权限过滤 |
| 查询总分 | 六科 `COALESCE` 相加 | 是否要求六科完整、缺考如何计入总分 |
| 班级排名 | `RANK()/DENSE_RANK()` 窗口函数 | 按年级+班级分组；决定同分规则、缺考是否参与、是否只看完整成绩 |
| 单科排名 | 同上的单科版本 | 决定缺考是否参与、是否按班级或全年级排名 |
| 缺考成绩记 0 | `absent_subjects` 保存代码，成绩列存 0 | 保存/修改时校验“缺考 => 0”“0 分不一定是缺考” |
| 学生提交查卷申请 | `stu_review` 插入，`status` 默认 0 | 校验学生身份、考试和科目存在、是否已有重复申请 |
| 班主任受理/驳回 | `stu_review` 更新 `status`、`reply` | 状态流转（0→1/2，1→3）、权限、处理意见必填规则 |
| 高考成绩预测 | 历史六科成绩可提供数据 | 不建预测表；由后端按需计算/调用算法，属于可选功能 |

**数据库不负责的事情：**

- 不校验“班主任只能改自己班”：需要后端结合 `sys_user`、角色和班级做数据权限。
- 不自动计算排名和总分：查询时计算，不落库。
- 不校验分数上限：`decimal(5,2)` 只保证类型和精度，0–150/0–100 的范围由 Service 校验。
- 不保证“缺考科目成绩一定为 0”：这是跨字段业务规则，由 Service 保证。
- 不做高考预测：四张表只保存历史和基础数据，预测逻辑属于后端可选功能。

## 10. 若依代码生成器注意事项

1. 四张表都有 `id` 主键和中文注释，可以被 `ruoyi-generator` 导入。
2. 当前 [generator.yml](../ruoyi-generator/src/main/resources/generator.yml) 是 `tablePrefix: sys_`、`autoRemovePre: false`。导入 `stu_*` 表后，实体类默认是 `StuStudent`、`StuExam`、`StuScore`、`StuReview`；业务名是 `student`、`exam`、`score`、`review`，权限前缀可设为 `system:student:*` 等。
3. 如果希望实体类叫 `Student`、`Exam`、`Score`、`Review`，需要把生成器配置改为 `autoRemovePre: true`、`tablePrefix: sys_,stu_`；这是生成器配置变更，应先评审，不要随手改。
4. 生成时把功能名手动改成“学生管理”“考试管理”“成绩管理”“查卷管理”，不要把表注释“学生信息表”直接当菜单名。
5. 字段类型映射：`int` → `Integer`，`bigint` → `Long`，`decimal` → `BigDecimal`，`date` → `Date`，`varchar/char` → `String`。
6. `stu_review.status` 是 `char(1)`，建议在若依字典管理中添加 `stu_review_status`（0 待处理、1 已受理、2 已驳回、3 已完成），不要新建字典表。
7. `stu_score` 的六科字段会生成六个数字输入框；总分、排名、联表查询需要手写 Mapper/Service，不能只依赖生成代码。
8. `stu_student.user_id` 会生成普通数字输入；实际页面应改成用户选择器或只读展示，避免手工填错。
9. `absent_subjects` 是逗号分隔字符串，生成器只会当普通输入框；保存前必须由后端校验科目代码、去空格、去重。

## 11. 已知边界

- 学生转班后，历史班级排名会按 `stu_student.class_name` 的当前值计算；如果老师要求“按考试时班级排名”，后续需要在 `stu_score` 增加班级快照字段，或把班级放到考试/成绩快照中。
- `absent_subjects` 使用逗号分隔，简单但不符合第一范式；这是按当前需求做的有意简化，查询用 `FIND_IN_SET`。如果以后要做选考、走班或大量科目，建议改为独立的成绩明细表。
- 四张表没有教师表；班主任身份、权限和“只能处理本班”由若依用户、角色和后端数据权限控制。
- 本目录的 `001_create_school_tables.sql` 只建表；`002_stu_student_menu.sql` 是代码生成器输出的菜单 SQL，执行前要确认 `parent_id`（当前改为 1 系统管理）和角色授权，不要直接当迁移脚本反复执行。
