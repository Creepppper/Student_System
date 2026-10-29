# SQL 设计草稿说明

> **设计草稿，尚未执行，尚未部署，需评审后使用。**

## 文件

| 文件 | 内容 | 状态 |
| --- | --- | --- |
| `schema_catalog.py` | P0/P1 表/字段/约束的单一事实源 | 设计源 |
| `generate_schema.py` | 从 catalog 渲染 DDL，不连接数据库 | 工具 |
| `01-p0-schema.sql` | 19 张 P0 表、索引、CHECK、物理 FK | **未执行** |
| `02-p1-extensions.sql` | 3 张 P1 表 | **未执行** |
| `static-check.txt` | 静态检查输出（无数据库连接） | PASS（仅静态） |

## 建表顺序

1. `stu_year` → `stu_term` → `stu_grade`
2. `stu_class` → `stu_student` → `stu_student_class`
3. `stu_head_teacher` → `stu_head_teacher_assignment` → `stu_subject`
4. `stu_exam` → `stu_exam_class` → `stu_exam_subject`
5. `stu_score_task` → `stu_score`
6. `stu_publication` → `stu_publication_version` → `stu_publication_version_detail`
7. `stu_audit_log` → `stu_account_binding`
8. 补 `stu_publication.current_version_id` 外键
9. 评审后执行 P1：`stu_review_application` → `stu_review_action` / `stu_import_batch`

## 与 `sys_user` 的关联

- `stu_account_binding.user_id` 为 **BIGINT**，与 `sys_user.user_id` 同类型；
- 不建物理 FK，服务层校验账号存在/状态/类型；
- 不修改 `sys_user` 主键、表结构、数据；
- 不把学号当自增主键。

## 兼容性

- 目标：MySQL 8 / MariaDB 11.8，InnoDB，`utf8mb4`，`utf8mb4_unicode_ci`；
- 使用生成列、CHECK、`ON UPDATE CURRENT_TIMESTAMP`；
- 审计 JSON 使用 `LONGTEXT`，不依赖数据库 JSON 类型；
- 未在真实库执行，**不能声称已验证**；
- 时间区间不重叠不能只靠 UNIQUE：需要事务 + 行锁 + Service 校验。

## 潜在迁移风险

| 风险 | 处理 |
| --- | --- |
| 循环 FK（publication.current_version） | 建表后 ALTER；或评审降级为逻辑关联 |
| 生成列/索引差异 | 测试库实跑；保留回滚脚本 |
| CHECK 版本差异 | Service 同步校验；上线前双库验证 |
| 历史数据/已存在同表 | 本阶段不执行；上线前检查表名冲突 |
| 误删历史 | 全部 `RESTRICT`；不提供级联删除 |
| 大表发布明细 | 先评估数据量/索引；必要时分批快照 |

## 上线前验证清单

- [ ] 测试库执行 P0 DDL 成功
- [ ] 19 张表/索引/约束与字段字典一致
- [ ] `id INT AUTO_INCREMENT` 全部满足
- [ ] `user_id BIGINT` 兼容
- [ ] P0 29 个 FK（含 current_version）全部可创建
- [ ] 0.5/三态 CHECK 实测
- [ ] 生成列唯一键并发测试
- [ ] 发布/撤回/更正事务回滚测试
- [ ] 权限：admin 仍不能查成绩明细
- [ ] 备份/回滚脚本/迁移审批
- [ ] 未修改若依原表/原数据
