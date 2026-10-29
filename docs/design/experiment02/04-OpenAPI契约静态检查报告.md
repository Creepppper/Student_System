# 04 OpenAPI 契约静态检查报告

> 检查方式：Python + PyYAML 解析；**不启动服务、不连接数据库、不发 HTTP 请求**。  
> 结论：契约静态检查通过；**接口未实现、未部署、未运行时验证**。

## 1. 文件

| 文件 | 内容 |
| --- | --- |
| `openapi/p0-business-api.yaml` | P0 业务 API，68 个操作 |
| `openapi/p1-extension-api.yaml` | P1，11 个操作 |
| `openapi/api-catalog.json` | 24 项规约/权限/事务/幂等/表/Controller/Service/前端/测试的机器目录 |
| `openapi/api_catalog.py` | 生成源 |
| `openapi/generate_openapi.py` | 生成器 |
| `openapi/check_openapi.py` | 静态检查脚本 |
| `openapi/static-check.txt` | 检查输出 |

## 2. 检查结果

| 检查 | 结果 |
| --- | --- |
| OpenAPI 版本 | 3.0.3（Springdoc 2.9 可生成/兼容） |
| YAML 解析 | 通过 |
| P0 操作 | **68** |
| P0 路径 | 53（同一路径多方法） |
| P1 操作 | **11** |
| P1 路径 | 11 |
| 唯一 `operationId` | 79/79 |
| 唯一 `x-api-id` | 79/79 |
| 路径参数定义 | 通过（无缺失/非必填） |
| `$ref` Schema | 通过（无缺失引用） |
| `requestBody` Schema | 通过（全部 `$ref`） |
| P1 路径混入 P0 | **否** |
| P0 路径前缀 | 全部 `/business/` |
| P1 路径前缀 | 全部 `/business/` |
| 响应 200/400/401/403/404/409 | 通过 |
| 运行时验证 | **未执行**（8080 未启动） |

输出：`openapi/static-check.txt`。

## 3. Schema 覆盖

`components.schemas` 覆盖：

- 公共：`AjaxResult`、`TableDataInfo`、`ErrorData`、`PageQuery`；
- 组织：`Year`、`Term`、`Grade`、`Class`、`YearUpsert`、`TermUpsert`、`GradeUpsert`、`ClassUpsert`；
- 人员：`Student`、`HeadTeacher`、`Binding`、`Identity`、`Assignment`；
- 考试：`Exam`、`ExamDetail`、`ExamClassSet`、`ExamSubjectSet`、`ExamSubjectItem`；
- 成绩：`ScoreTask`、`ScoreItem`、`ScoreBatchRequest`、`ScoreSaveResult`、`TaskProgress`、`ReviewRequest`；
- 发布：`Publication`、`PublicationVersion`、`PublishRequest`、`WithdrawRequest`、`RepublishRequest`、`CorrectionRequest`；
- 查询/统计：`MyScoreDetail`、`ClassStat`、`SchoolStat`；
- 审计：`AuditRecord`；
- P1：`ReviewApplyRequest`、`ReviewHandleRequest`、`ReviewConfirmRequest`、`ImportRequest`、`ExportRequest`、`Review`、`ImportPreview`、`ImportResult`、`PersonTrend`、`ClassTrend`、`ClassCompare`。

## 4. 契约约定

- `servers` 仅写本地开发地址，不是部署地址；
- `security` 全局 Bearer JWT；
- 每个操作有 `x-api-id`、`x-fr`、`x-br`、`x-ac`、`x-permission`、`x-roles`、`x-scope`、`x-tables`、`x-controller`、`x-service`、`x-frontend`、`x-idempotency`、`x-version`、`x-transaction`；
- `x-data-schema` 指向具体 data schema；无该扩展表示 data 为通用对象/无；
- 成绩/线 `number` + `multipleOf: 0.5`；
- 状态枚举严格使用 SRS 代码；
- 业务错误码在 `data.errorCode`，不是新增顶层字段。

## 5. 未验证/限制

| 项 | 状态 |
| --- | --- |
| Springdoc 实际生成 | 未运行 |
| Swagger UI | 未运行 |
| YAML→Java 类型映射 | 未实现 |
| 请求/响应实际序列化 | 未实现 |
| 业务接口契约测试 | 未执行 |
| P1 状态/时限 | 待评审 |
| OpenAPI 3.1 | 未采用；当前 3.0.3 |
| 完整字段 Schema | 部分列表/统计用 object；实现阶段细化 |

## 6. 结论

- **静态检查通过**：可解析、引用闭合、operationId/ID 唯一、路径参数完整；
- **不能称为接口已实现**：没有真实 Controller/Service；
- **不能称为运行时验证**：8080 未启动；
- P1 独立文件，不混入 P0 实现清单。
