# SDD 数据设计一致性检查报告

> 对象：第 5 章、第三阶段数据库设计、OpenAPI Schema。  
> 结论：正文数据叙述与 31/19/3 表一致；19/3 仍是设计草稿，未建表。

## 1. 数量核对

| 项 | 正文/来源 | 结果 |
| --- | --- | --- |
| 现有若依/Quartz 表 | 31 = 18 sys + 2 gen + 11 QRTZ | 一致 |
| P0 计划表 | 19 | 一致 |
| P1 计划表 | 3 | 一致 |
| P0 API | 68 | 一致 |
| P1 API | 11 | 一致 |
| 现有学生业务表 | 0 | 一致 |

## 2. 关键概念

| 检查 | 结果 |
| --- | --- |
| `sys_user` 不是学生档案 | 通过；绑定表独立 |
| `sys_dept` 不是行政班 | 通过；`stu_class` 独立 |
| 学生账号绑定 | `stu_account_binding`；类型/唯一 |
| 成绩三态 | `SCORE_NORMAL/ABSENT/NOT_ENTERED` |
| 0.5 精度 | `DECIMAL(6,1)` / `multipleOf:0.5` |
| 任务唯一 | 考试×班级×科目 |
| 成绩唯一 | 任务×学生 |
| 发布唯一 | 考试×班级 |
| 版本不可变 | publication/version/detail 三表 |
| 审计 | 独立 `stu_audit_log`，同事务 |
| P1 | 3 表，不进入 P0 |

## 3. API/Schema 对应

- 请求字段与第 3 阶段字段类型一致：ID integer、user_id int64/BIGINT、成绩 number/0.5；
- 状态枚举与 SRS 一致；
- `current_version_id` 在正文按逻辑关联说明，避免与物理 FK 二选一未决混淆；
- 发布/更正 API 与版本表对应；
- 查询 API 只读当前 PUB_PUBLISHED 版本。

## 4. 未验证

- P0/P1 DDL 未执行；
- SQL 静态检查通过，但未在 MySQL/MariaDB 实跑；
- 生成列/CHECK/循环 FK 未实跑；
- 时间区间并发未测试；
- 完整字段字典/ER 见附录引用，不在正文重复。
