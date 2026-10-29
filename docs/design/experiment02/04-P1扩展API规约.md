# 04 P1 扩展 API 规约

> P1 为实验一 11 项扩展，**不进入 P0 退出条件**，不改变 P0 状态机。  
> 机器可读：`openapi/p1-extension-api.yaml`；P1 路径不混入 `p0-business-api.yaml`。  
> 状态/时限/通知标记仍待 P1 专项评审；本文件不把它们说成已批准。

## 1. P1 API（11 个）

| API | 名称 | 方法/路径 | 角色/权限 | 主要请求 | 成功 data | 关键约束 | FR |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `API-P1-REVIEW-001` | 学生提交复核申请 | `POST /business/reviews` | 学生 `business:review:add` | `ReviewApplyRequest` | Review | 本人已发布；同一学生+考试+科目最多一个活跃；时限待评审 | FR-REVIEW-001 |
| `API-P1-REVIEW-002` | 我的复核申请 | `GET /business/reviews/my` | 学生 `business:review:my` | 分页 | TableDataInfo | 只本人 | FR-REVIEW-005 |
| `API-P1-REVIEW-003` | 班主任受理/驳回 | `POST /business/reviews/{id}/handle` | 班主任 `business:review:handle` | `ReviewHandleRequest` | Review | 本班；驳回原因；状态待评审 | FR-REVIEW-002 |
| `API-P1-REVIEW-004` | 教务确认复核结论 | `POST /business/reviews/{id}/confirm` | 教务 `business:review:confirm` | `ReviewConfirmRequest` | Review | 最终确认；更正复用 P0 更正 | FR-REVIEW-004 |
| `API-P1-IMPORT-001` | 下载导入模板 | `GET /business/imports/template` | 班主任 `business:import:template` | taskId | BinaryFile | 仅可编辑任务 | FR-IMPORT-001 |
| `API-P1-IMPORT-002` | 导入预检 | `POST /business/imports/precheck` | 班主任 `business:import:precheck` | `ImportRequest` | ImportPreview | 全量预检，不写成绩 | FR-IMPORT-002 |
| `API-P1-IMPORT-003` | 提交导入 | `POST /business/imports/commit` | 班主任 `business:import:commit` | `ImportRequest` | ImportResult | 预检通过；全成功/全失败；版本 | FR-IMPORT-003 |
| `API-P1-EXPORT-001` | 导出成绩 | `POST /business/exports/scores` | 教务/班主任 `business:export:scores` | `ExportRequest` | BinaryFile | 范围过滤；审计；字段最小化 | FR-IMPORT-004 |
| `API-P1-TREND-001` | 我的成绩趋势 | `GET /business/my-score-trends` | 学生 `business:trend:my` | subjectId | PersonTrend | 只本人已发布；同口径 | FR-TREND-001 |
| `API-P1-TREND-002` | 班级学科趋势 | `GET /business/stats/class-trends` | 教务/班主任 `business:trend:class` | classId/subjectId | ClassTrend | 授权班级；同口径 | FR-TREND-002 |
| `API-P1-COMPARE-001` | 授权班级对比 | `GET /business/stats/class-compare` | 教务/班主任 `business:compare:class` | examId/subjectId/classIds | ClassCompare | 同考试/科目/满分口径；不做评价 | FR-COMPARE-001 |

## 2. P1 边界

- P1 不改变 P0 的 `TASK_*`/`PUB_*` 状态机；
- 复核更正调用 P0 受控更正，生成新发布版本；
- 学生没有班级对比权限；
- P1 不得恢复任课教师核查；
- P1 状态码/时限/通知未冻结，当前 `review_status` 可空/待评审；
- P1 不建表/不部署；P1 表在 `03-P1数据模型扩展规划.md` 中独立列出。

## 3. OpenAPI

- `openapi/p1-extension-api.yaml`：11 个 P1 操作；
- 静态检查：P1 路径不与 P0 重叠；schema 引用有效；
- 未实现、未运行。
