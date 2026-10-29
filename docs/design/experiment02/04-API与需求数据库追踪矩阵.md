# 04 API 与需求、数据库追踪矩阵

> 基线：SRS v1.1-review；35 P0 + 11 P1；17 DR + ENT-08；19 张 P0 表 + 3 张 P1 表。  
> API 状态：全部为**规划/未实现**；现有若依接口单独标注 `EXT`。

## 1. 35 P0 FR → API

| FR | 功能 | API（P0） | 数据表/视图 | AC |
| --- | --- | --- | --- | --- |
| FR-AUTH-001 | 登录/登出/验证码 | EXT `/captchaImage`、`/login`、`/logout` | `sys_user`、Valkey | AC-FR-AUTH-001 |
| FR-AUTH-002 | 用户/角色/菜单/按钮 | EXT `/system/user`、`role`、`menu` | `sys_*` | AC-FR-AUTH-002 |
| FR-AUTH-003 | 业务角色/账号绑定 | API-AUTH-001/002/003、API-STU-009/010 | `stu_account_binding` | AC-FR-AUTH-003 |
| FR-AUTH-004 | 数据范围 | API-AUTH-001；全部业务查询的 scope | 绑定/任职/发布 | AC-FR-AUTH-004 |
| FR-AUTH-005 | 账号启停/密码 | EXT `/system/user`；API-AUTH-003 | `sys_user`、binding | AC-FR-AUTH-005 |
| FR-ORG-001 | 学年 | API-ORG-001～004 | `stu_year` | AC-FR-ORG-001 |
| FR-ORG-002 | 学期 | API-ORG-005～007 | `stu_term` | AC-FR-ORG-002 |
| FR-ORG-003 | 年级 | API-ORG-008～010 | `stu_grade` | AC-FR-ORG-003 |
| FR-ORG-004 | 班级 | API-ORG-011～013 | `stu_class` | AC-FR-ORG-004 |
| FR-ORG-005 | 班主任指派/变更 | API-ORG-014～016、API-TCH-005 | `stu_head_teacher_assignment` | AC-FR-ORG-005 |
| FR-STU-001 | 学生档案 | API-STU-001～004 | `stu_student` | AC-FR-STU-001 |
| FR-STU-002 | 学生归属/变更 | API-STU-006/007 | `stu_student_class` | AC-FR-STU-002 |
| FR-STU-003 | 学生账号绑定 | API-STU-008～010、API-AUTH-002 | `stu_account_binding` | AC-FR-STU-003 |
| FR-STU-004 | 学生状态 | API-STU-005 | `stu_student.status` | AC-FR-STU-004 |
| FR-TCH-001 | 班主任档案 | API-TCH-001～005 | `stu_head_teacher(res)` | AC-FR-TCH-001 |
| FR-TCH-002 | 科目 | API-SUB-001～003 | `stu_subject` | AC-FR-TCH-002 |
| FR-EXAM-001 | 考试创建/编辑 | API-EXAM-001/002/003/004 | `stu_exam` | AC-FR-EXAM-001 |
| FR-EXAM-002 | 考试范围 | API-EXAM-006/007 | `stu_exam_class` | AC-FR-EXAM-002 |
| FR-EXAM-003 | 考试科目/分值 | API-EXAM-008/009 | `stu_exam_subject` | AC-FR-EXAM-003 |
| FR-EXAM-004 | 考试状态 | API-EXAM-005 | `stu_exam.status` | AC-FR-EXAM-004 |
| FR-SCORE-001 | 任务生成 | API-EXAM-010、API-SCORE-001/002 | `stu_score_task` | AC-FR-SCORE-001 |
| FR-SCORE-002 | 录入/暂存 | API-SCORE-003/004 | `stu_score` | AC-FR-SCORE-002 |
| FR-SCORE-003 | 校验 | API-SCORE-004/005 | `stu_score` CHECK+Service | AC-FR-SCORE-003 |
| FR-SCORE-004 | 提交/重提 | API-SCORE-005/006 | `stu_score_task` | AC-FR-SCORE-004 |
| FR-SCORE-005 | 三态 | API-SCORE-003/004 | `stu_score` | AC-FR-SCORE-005 |
| FR-PUB-001 | 教务审核 | API-SCORE-007 | task+audit | AC-FR-PUB-001 |
| FR-PUB-002 | 班级发布 | API-PUB-001/002 | publication/version/detail | AC-FR-PUB-002 |
| FR-PUB-003 | 撤回 | API-PUB-004/005 | publication/version | AC-FR-PUB-003 |
| FR-PUB-004 | 更正/版本 | API-PUB-006/007/008 | version/detail/audit | AC-FR-PUB-004 |
| FR-QUERY-001 | 学生本人查询 | API-QUERY-001/002 | publication/version/detail | AC-FR-QUERY-001 |
| FR-QUERY-002 | 明细/总分/排名 | API-QUERY-002 | version detail | AC-FR-QUERY-002 |
| FR-STAT-001 | 班级学科统计 | API-STAT-001 | version detail | AC-FR-STAT-001 |
| FR-STAT-002 | 总分/排名汇总 | API-STAT-001/002 | version detail | AC-FR-STAT-002 |
| FR-AUDIT-001 | 业务变更 | API-AUDIT-001/002 | `stu_audit_log` | AC-FR-AUDIT-001 |
| FR-AUDIT-002 | 发布/撤回记录 | API-AUDIT-001/002 | audit+version | AC-FR-AUDIT-002 |

## 2. 11 P1 FR → API

| FR | API（P1） | 表/视图 |
| --- | --- | --- |
| FR-REVIEW-001 | API-P1-REVIEW-001 | `stu_review_application` |
| FR-REVIEW-002 | API-P1-REVIEW-003 | application/action |
| FR-REVIEW-004 | API-P1-REVIEW-004 | application/action + P0 更正 |
| FR-REVIEW-005 | API-P1-REVIEW-002 | application |
| FR-IMPORT-001 | API-P1-IMPORT-001 | task |
| FR-IMPORT-002 | API-P1-IMPORT-002 | import batch |
| FR-IMPORT-003 | API-P1-IMPORT-003 | score/import batch |
| FR-IMPORT-004 | API-P1-EXPORT-001 | version/detail + audit |
| FR-TREND-001 | API-P1-TREND-001 | version/detail |
| FR-TREND-002 | API-P1-TREND-002 | version/detail |
| FR-COMPARE-001 | API-P1-COMPARE-001 | version/detail |

## 3. 覆盖统计

| 项 | 数量 |
| --- | ---: |
| P0 API | 68 |
| P1 API | 11 |
| P0 FR | 35 |
| P1 FR | 11 |
| P0 表 | 19 |
| P1 表 | 3 |
| 四类角色 | 4 |
| 任课教师角色/任教关系 | 0 |

- 68 个 P0 API 全部有 P0 FR 或复用若依现有接口的明确落点；
- 35 个 P0 FR 均有 API；AUTH-001/002/005 主要由现有若依接口复用；
- 11 个 P1 FR 独立映射；
- 未将学生档案接口复用为 `sys_user`；
- 未将 `sys_dept` 当班级。

## 4. 关键一致性

| 约束 | 体现 |
| --- | --- |
| 成绩三态 | `ScoreItem.scoreStatus` / `SCORE_*` |
| 0.5 精度 | `multipleOf: 0.5`、`DECIMAL(6,1)`、Service |
| 任务唯一 | API-EXAM-010 生成、API-SCORE-001 查询 |
| 发布×班级唯一 | API-PUB-002 |
| 版本不可变 | API-PUB-006/007/008 |
| 审核/发布分离 | API-SCORE-007 vs API-PUB-002 |
| 学生本人 | API-QUERY-001/002；Service 从绑定取 |
| 管理员隔离 | 矩阵 D*；API-AUDIT 脱敏 |
| 历史 | 归属/任职/版本表；接口只追加/关闭 |

## 5. BR 137 与 122 口径

- **137**：SRS 附录 E 的基线规则表行数（分类合计）；
- **122**：SRS 正文实际引用的唯一 BR 编号数（全文 186 次出现去重）；
- 差异 15 条：正文未逐条引用；14 个编号缺口可识别，另 1 条因 `baseline/04-核心业务规则.md` 已删除无法逐行复原；
- **不删除、不重编号、不伪造**；本 API 设计只引用 SRS 有效 BR/AC；
- 该差异是文档可追溯性风险，不改变 35 P0/11 P1/17 DR 的基线。

## 6. 未覆盖/待确认

- 现有若依接口运行时未验证（8080 未启动）；
- P0 API 未实现、未部署；
- 具体 `sys_role` 四类业务角色映射未定；
- 多班班主任合并统计（A/B）未定；
- P1 状态/时限未定；
- 数据库 DDL 未执行。
