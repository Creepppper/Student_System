# SDD 模块与接口覆盖检查报告

> 检查对象：`学生管理系统概要设计说明书-v1.0-draft.md` 与 04 阶段 API 目录。  
> 结论：35 P0 / 11 P1 均有设计落点；所有 68/11 API 为规划。

## 1. 数量

| 项 | 数量 |
| --- | ---: |
| P0 FR | 35 |
| P1 FR | 11 |
| P0 API | 68 |
| P1 API | 11 |
| P0 表 | 19 |
| P1 表 | 3 |
| 现有 Maven 模块 | 6 |
| 计划新增业务模块 | 1（`ruoyi-student`，未创建） |
| 业务角色 | 4 |
| 任课教师/任教关系 | 0 |

## 2. 模块覆盖

| SDD 模块 | FR | API | 表 |
| --- | --- | --- | --- |
| 认证/绑定 | AUTH-003/004 | API-AUTH-001/002/003、STU-009/010 | binding |
| 组织 | ORG-001～005 | API-ORG-001～016、TCH-005 | year/term/grade/class/assignment |
| 学生 | STU-001～004 | API-STU-001～010 | student/student_class/binding |
| 班主任/科目 | TCH-001/002 | API-TCH-001～005、SUB-001～003 | teacher/assignment/subject |
| 考试 | EXAM-001～004、SCORE-001 | API-EXAM-001～010 | exam/exam_class/exam_subject/task |
| 成绩 | SCORE-001～005、PUB-001 | API-SCORE-001～007 | task/score/audit |
| 发布 | PUB-001～004 | API-PUB-001～008 | publication/version/detail/audit |
| 查询/统计 | QUERY/STAT | API-QUERY/STAT | version/detail |
| 审计 | AUDIT-001/002 | API-AUDIT-001/002 | audit_log |
| P1 | 11 项 | 11 P1 API | 3 P1 表 |

## 3. 检查结论

- 35 个 P0 FR 全部有模块/API/表/AC 落点；
- AUTH-001/002/005 复用现有若依接口，不重复造登录/RBAC；
- 11 个 P1 FR 独立，不进入 P0；
- 68 个 P0 API 数量与 OpenAPI 一致；
- 19 张 P0 表与第三阶段一致；
- 四类角色一致，无任课教师；
- 未把 `sys_user` 当学生、`sys_dept` 当班级；
- 未把前端原型当已实现页面。

## 4. 缺口/待确认

- 68/11 API 未实现；
- 现有 146 Mapping 运行未验证；
- P1 状态/时限未定；
- 多班班主任 A/B 未定；
- 具体 `sys_role` 角色映射未定。
