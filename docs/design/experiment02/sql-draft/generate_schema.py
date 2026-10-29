#!/usr/bin/env python3
"""Render the draft DDL from schema_catalog.py. Never connects to a database."""
from pathlib import Path
import schema_catalog as s

HERE = Path(__file__).resolve().parent
HEADER = """-- ============================================================
-- 设计草稿，尚未执行，尚未部署，需评审后使用。
-- 目标：学生信息与成绩管理系统 P0 业务表（MySQL 8 / MariaDB 11.8）。
-- 不包含 DROP、数据变更、用户/权限变更；本文件不应直接在生产库执行。
-- 主键：新建业务主表 id INT AUTO_INCREMENT。
-- 账号关联：sys_user.user_id 为 BIGINT，绑定表 user_id 同为 BIGINT，不修改原表。
-- ============================================================
SET NAMES utf8mb4;
"""

def q(s):
    return "'" + str(s).replace("'", "''") + "'"

def render_table(t, p0=True):
    lines=[]
    for c in t['cols']:
        parts=[f"  `{c['name']}` {c['type']}"]
        if c.get('generated'):
            parts.append(f"GENERATED ALWAYS AS ({c['generated']}) STORED")
        else:
            parts.append('NOT NULL' if not c['null'] else 'NULL')
            if c.get('default') is not None:
                parts.append(f"DEFAULT {c['default']}")
            if c.get('extra'):
                parts.append(c['extra'])
        parts.append(f"COMMENT {q(c['comment'])}")
        lines.append(' '.join(parts))
    lines.append('  PRIMARY KEY (`id`)')
    for name,cols,comment in t.get('uniques',[]):
        lines.append(f"  UNIQUE KEY `{name}` ({', '.join('`'+x+'`' for x in cols)}) COMMENT {q(comment)}")
    for name,cols,comment in t.get('indexes',[]):
        lines.append(f"  KEY `{name}` ({', '.join('`'+x+'`' for x in cols)}) COMMENT {q(comment)}")
    for name,expr,comment in t.get('checks',[]):
        lines.append(f"  CONSTRAINT `{name}` CHECK ({expr})")
    body=',\n'.join(lines)
    note=''
    if t.get('note'):
        note=f" -- 设计说明：{t['note']}\n"
    return f"-- {t['name']}：{t['comment']}；模块 {t['module']}；DR: {', '.join(t['drs']) or 'P1 扩展'}\n{note}CREATE TABLE `{t['name']}` (\n{body}\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT={q(t['comment'])};\n"

def render_fks(tables):
    out=['\n-- 外键：业务内部稳定父子关系使用物理 FK；sys_user 绑定使用逻辑关联；审计多态不建 FK。\n']
    for t in tables:
        for name,col,ref,colref,ondelete,comment in t.get('fks',[]):
            out.append(f"ALTER TABLE `{t['name']}` ADD CONSTRAINT `{name}` FOREIGN KEY (`{col}`) REFERENCES `{ref}` (`{colref}`) ON DELETE {ondelete} ON UPDATE RESTRICT; -- {comment}\n")
    # circular current publication version FK after both tables exist
    if any(t['name']=='stu_publication' for t in tables):
        out.append("ALTER TABLE `stu_publication` ADD CONSTRAINT `fk_stu_publication_current_version` FOREIGN KEY (`current_version_id`) REFERENCES `stu_publication_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT; -- 循环依赖：表创建后补加\n")
    return ''.join(out)

p0=''.join(render_table(t) + '\n' for t in s.P0)
p0 += render_fks(s.P0)
p1_head = """-- ============================================================
-- 设计草稿，尚未执行，尚未部署，需评审后使用。
-- P1 扩展表（复核/导入），不属于 P0 退出条件；状态码待 P1 评审。
-- 不包含 DROP、数据变更、用户/权限变更。
-- ============================================================
SET NAMES utf8mb4;
"""
p1=''.join(render_table(t) + '\n' for t in s.P1)
p1 += render_fks(s.P1)
(HERE/'01-p0-schema.sql').write_text(HEADER+'\n'+p0, encoding='utf-8')
(HERE/'02-p1-extensions.sql').write_text(p1_head+'\n'+p1, encoding='utf-8')
print('written', HERE/'01-p0-schema.sql', HERE/'02-p1-extensions.sql')
