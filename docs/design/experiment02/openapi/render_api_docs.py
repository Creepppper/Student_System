#!/usr/bin/env python3
from pathlib import Path
import json
from api_catalog import P0,P1
HERE=Path(__file__).resolve().parent; ROOT=HERE.parent

def clean(s): return str(s).replace('|','\\|').replace('\n',' ')
def fmt_req(a):
    q=', '.join(a['query']) if a['query'] else ''
    body=('body `'+a['body']+'`') if a['body'] else ''
    return '；'.join(x for x in [('query: '+q if q else ''),body] if x) or '路径参数/无'

def row(a):
    return '| ' + ' | '.join([
        f"`{a['id']}`", clean(a['name']), '规划' if a['status']=='NEW_P0' else 'P1 规划', a['tag'],
        f"{a['fr']}；{a['br']}；{a['ac']}", f"`{a['method']} {a['path']}`", fmt_req(a),
        f"`{a['success']}`", '400/401/403/404/409', f"`{a['perm']}`<br>{a['scope']}", f"{a['pre']} → {a['post']}",
        f"{a['tx']}；幂等 {a['idem']}；版本 {a['version']}", f"{a['tables']}<br>{a['controller']}<br>{a['service']}", f"{a['frontend']}<br>TC-{a['id']}"]) + ' |'

head='''# 04 P0 业务 API 完整规约

> 状态：**设计草稿，未实现、未部署**。所有接口均为拟新增 P0；现有若依接口见 `04-现有若依API核实与复用清单.md`。  
> 机器可读完整字段（含 24 项规约、权限、事务、幂等、版本、表、Controller/Service、前端、测试关联）见 `openapi/api-catalog.json`；OpenAPI 见 `openapi/p0-business-api.yaml`。  
> 本表不重复公共成功/失败 Schema；统一规则见 `04-请求响应与错误码规范.md`。成绩/日期/布尔/枚举/版本/幂等规则也见该文档。

## 1. 公共约定摘要

- 前缀：`/business/**`；不使用 `/dev-api`（Vite 开发代理）作为后端路径。
- 分页：`pageNum/pageSize/orderByColumn/isAsc`；响应 `TableDataInfo`。
- 单对象/操作：`AjaxResult{code,msg,data}`。
- 时间：`yyyy-MM-dd HH:mm:ss`；日期：`yyyy-MM-dd`；时区 `Asia/Shanghai`。
- 成绩：JSON number，`DECIMAL(6,1)`，`multipleOf: 0.5`；不用字符串/浮点近似。
- 枚举：大写代码；未知代码拒绝。
- 版本：更新/提交/审核/发布/撤回/更正必须带 `version`；冲突 409。
- 幂等：提交/发布/撤回/重发/更正/导入要求 `Idempotency-Key`；重复键返回原结果或 409，不重复写。
- 认证：JWT `Authorization: Bearer`；功能权限 `business:*`；数据范围 Service 二次校验。
- 现有 AjaxResult/TableDataInfo 是若依真实类型；业务错误码放在 `data.errorCode`，不修改全局响应。

## 2. 完整 API 表（68 个 P0）

| API | 名称 | 状态 | 模块 | FR/BR/AC | 方法/路径 | 请求 | 成功 data | 失败 HTTP | 权限/范围 | 前置→后置 | 事务/幂等/版本 | 表/Controller/Service | 前端/测试 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
'''
body=head+'\n'.join(row(a) for a in P0.values())+'''

## 3. 关键接口的 24 项展开

### API-SCORE-004 保存成绩草稿（批量）

1. 编号：API-SCORE-004；2. 名称：保存成绩草稿；3. 状态：规划；4. 模块：成绩任务；
5. FR-SCORE-002/003/005；BR-SCORE-004/005/008/009/013～015；AC-FR-SCORE-002/003/005；
6. `PUT /business/score-tasks/{id}/scores`；7. 路径 `id`、body `version/items/idempotencyKey`；8. `ScoreBatchRequest`；
9. `scoreValue` DECIMAL 0.5、状态枚举、version 整数；10. 成功 `AjaxResult{data=ScoreSaveResult}`；
11. 失败 400/401/403/404/409；12. `business:score:edit`；班主任；
13. `SCOPE_CLASS` + 当前有效任职；14. 前置任务 `TASK_DRAFT/TASK_RETURNED`、考试 `EXAM_OPEN`；
15. 后置草稿保存，任务不变；16. 单事务锁任务/成绩、批量 upsert、审计；
17. `Idempotency-Key` 可选；18. `version` 必须匹配；19. `stu_score_task/stu_score/stu_audit_log`；
20. `ScoreTaskController#saveDraft` / `ScoreTaskService#saveDraft`；21. `src/api/business/score.js`；
22. 示例见第 4 节；23. TC-API-SCORE-004；24. 见测试矩阵。

### API-SCORE-005 提交成绩任务

编号/名称/状态/模块：API-SCORE-005 / 提交成绩任务 / 规划 / 成绩任务；FR-SCORE-004；BR-SCORE-006/007/008、BR-PUB-001/013；AC-FR-SCORE-004；`POST /business/score-tasks/{id}/submit`；路径 id，body `VersionRequest`；成功 `AjaxResult{data=ScoreTask}`；失败 400/401/403/404/409；`business:score:submit`；班主任；`SCOPE_CLASS`；前置 `TASK_DRAFT/RETURNED`、全部 NORMAL/ABSENT、EXAM_OPEN；后置 `TASK_SUBMITTED`；单事务提交锁定+审计；`Idempotency-Key` 必须；version 必须；表 `stu_score_task/stu_score/stu_audit_log`；Controller/Service `ScoreTaskController#submit/ScoreTaskService#submit`；前端 `score.js`；示例见第 4 节；测试 TC-API-SCORE-005。

### API-SCORE-007 审核/退回

编号/名称/状态/模块：API-SCORE-007 / 审核退回 / 规划 / 成绩任务；FR-PUB-001；BR-PUB-001/002/003/013、BR-AUTH-012；AC-FR-PUB-001；`POST /business/score-tasks/{id}/review`；body `ReviewRequest`；成功 `AjaxResult{data=ScoreTask}`；失败 400/401/403/404/409；`business:score:review`；教务；`SCOPE_ALL`；前置 `TASK_SUBMITTED`，审核人≠提交人；后置 APPROVED/RETURNED；单事务+审计；幂等键必须；version 必须；表 `stu_score_task/stu_audit_log`；Controller/Service `ScoreReviewController#review/ScoreReviewService#review`；前端 `score.js`；测试 TC-API-SCORE-007。

### API-PUB-002 发布成绩

编号/名称/状态/模块：API-PUB-002 / 发布班级成绩 / 规划 / 成绩发布；FR-PUB-002；BR-PUB-001～004/009；AC-FR-PUB-002；`POST /business/publications`；body `PublishRequest`；成功 `AjaxResult{data=Publication}`；失败 400/401/403/404/409；`business:publish:create`；教务；`SCOPE_ALL`；前置全部任务 APPROVED、无未录入、至少一科；后置 `PUB_PUBLISHED` 且版本+1；单事务发布+版本+明细+审计；幂等键必须；version 必须；表 publication/version/detail/audit；`PublicationController#publish`/`PublicationService#publish`；前端 `publication.js`；测试 TC-API-PUB-002。

### API-PUB-006 受控更正

编号/名称/状态/模块：API-PUB-006 / 受控更正 / 规划 / 成绩发布；FR-PUB-004；BR-PUB-008～014；AC-FR-PUB-004；`POST /business/publications/{id}/corrections`；body `CorrectionRequest`；成功 `AjaxResult{data=Publication}`；失败 400/401/403/404/409；`business:publish:correct`；教务；`SCOPE_ALL`；前置 `PUB_PUBLISHED`、原因/非自审；后置仍 `PUB_PUBLISHED`，新版本，历史保留；单事务成绩/新版本/明细/审计；幂等键必须；version 必须；表 score/version/detail/audit；`PublicationController#correct`/`PublicationService#correct`；前端 `publication.js`；测试 TC-API-PUB-006。

### API-QUERY-002 学生本人成绩

编号/名称/状态/模块：API-QUERY-002 / 我的单场考试已发布成绩 / 规划 / 学生查询；FR-QUERY-001/002；BR-QUERY-001～009、BR-CALC；AC-FR-QUERY-001/002；`GET /business/my-scores/{examId}`；路径 examId；成功 `AjaxResult{data=MyScoreDetail}`；失败 401/403/404；`business:myscore:query`；学生；`SCOPE_SELF`（从绑定取 studentId，不接受客户端传入）；前置当前 `PUB_PUBLISHED`；后置无写入；只读事务；幂等 N/A；版本 N/A；表 publication/version/detail；`MyScoreController#detail`/`MyScoreService#detail`；前端 `query.js`；测试 TC-API-QUERY-002。

### API-AUDIT-001 业务审计

编号/名称/状态/模块：API-AUDIT-001 / 业务审计列表 / 规划 / 业务审计；FR-AUDIT-001/002；BR-AUDIT-001～008；AC-FR-AUDIT-001/002；`GET /business/audit/records`；query objectType/objectId/eventType/from/to + page；成功 `TableDataInfo`；失败 401/403；`business:audit:list`；教务/系统管理员；`SCOPE_ALL/DESENSITIZED`；系统管理员只能脱敏元数据，不能看成绩前后值；只读；幂等 N/A；版本 N/A；表 `stu_audit_log`；`AuditController#list`/`AuditService#list`；前端 `audit.js`；测试 TC-API-AUDIT-001。

## 4. 示例（全部虚构）

### 保存草稿请求

```json
{
  "version": 3,
  "idempotencyKey": "demo-score-save-001",
  "items": [
    {"studentId": 101, "scoreStatus": "SCORE_NORMAL", "scoreValue": 88.5, "version": 2},
    {"studentId": 102, "scoreStatus": "SCORE_ABSENT", "scoreValue": null, "version": 1},
    {"studentId": 103, "scoreStatus": "SCORE_NOT_ENTERED", "scoreValue": null, "version": 0}
  ]
}
```

### 成功响应

```json
{"code": 200, "msg": "操作成功", "data": {"saved": 2, "notEntered": 1, "absent": 1, "normal": 1, "version": 4}}
```

### 冲突响应

```json
{"code": 409, "msg": "成绩已被他人修改，请刷新后重试", "data": {"errorCode": "VERSION_CONFLICT", "details": {"expected": 3, "actual": 4}}}
```

## 5. 覆盖说明

- 68 个 P0 API 覆盖 35 个 P0 FR；现有认证/RBAC 重新用若依接口，不重复建。
- 未实现、未部署；表中 Controller/Service 均为计划名称。
'''
(ROOT/'04-P0业务API完整规约.md').write_text(body,encoding='utf-8')
# JSON catalog
json.dump({'p0':list(P0.values()),'p1':list(P1.values())},open(HERE/'api-catalog.json','w',encoding='utf-8'),ensure_ascii=False,indent=2)
print('P0 rows',len(P0),'P1',len(P1))
