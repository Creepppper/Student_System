#!/usr/bin/env python3
"""Render compact physical ER diagrams from schema_catalog.py (no DB connection)."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'sql-draft'))
import schema_catalog as s
OUT=Path(__file__).resolve().parent

def label(name):
    t=s.T[name]
    fk=', '.join(x[1] for x in t['fks']) or '—'
    uk=', '.join('+'.join(x[1]) for x in t['uniques']) or '—'
    # key business columns: first 2 non-audit besides id
    cols=[x['name'] for x in t['cols'] if x['name'] not in ('id','created_at','created_by','updated_at','updated_by','version')][:3]
    return f'<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="3" COLOR="#D97706"><TR><TD BGCOLOR="#FFE7CF" COLSPAN="2"><B>{name}</B></TD></TR><TR><TD ALIGN="LEFT">PK</TD><TD ALIGN="LEFT">id INT</TD></TR><TR><TD ALIGN="LEFT">FK</TD><TD ALIGN="LEFT">{fk}</TD></TR><TR><TD ALIGN="LEFT">UK</TD><TD ALIGN="LEFT">{uk}</TD></TR><TR><TD ALIGN="LEFT">列</TD><TD ALIGN="LEFT">{", ".join(cols)}</TD></TR></TABLE>>'

def render(fname,title,tables,external=()):
    out=['digraph G {','  graph [rankdir=TB, newrank=true, splines=polyline, nodesep=0.35, ranksep=0.55, pad=0.2, bgcolor="#FFFFFF", fontname="SimHei", fontsize=13, labelloc="t", label="%s"];'%title,
         '  node [shape=plaintext, fontname="SimHei", fontsize=10];',
         '  edge [fontname="SimHei", fontsize=9, arrowsize=0.6, color="#52616B"];']
    for n in external:
        out.append(f'  {n} [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="3" COLOR="#2F6FED"><TR><TD BGCOLOR="#DCEEFF" COLSPAN="2"><B>{n}（A 现有）</B></TD></TR><TR><TD>context</TD><TD>参见 D5</TD></TR></TABLE>>];')
    for n in tables:
        out.append(f'  {n} [label={label(n)}];')
    for n in tables:
        for fk in s.T[n]['fks']:
            if (fk[2] in tables or fk[2] in external) and fk[2] != n:
                out.append(f'  {n} -> {fk[2]} [label="1 : N", dir=none];')
    if 'stu_account_binding' in tables:
        out.append('  stu_account_binding -> sys_user [label="逻辑 user_id", style=dashed, color="#6A1B9A", dir=none];')
    out.append('  note [shape=note, label="物理模型：PK/FK/UK 来自 P0 设计草稿；\\n字段字典见 03-完整数据库字段字典.md。", fillcolor="#F8FAFC", color="#9AA7B4", fontsize=10];')
    out.append('}')
    (OUT/(fname+'.dot')).write_text('\n'.join(out),encoding='utf-8')

render('FIG-D6P-A1-组织学年班级物理ER','FIG-D6P-A1  组织/学年/班级物理 ER（规划）',
 ['stu_year','stu_term','stu_grade','stu_class','stu_subject'])
render('FIG-D6P-A2-人员归属任职绑定物理ER','FIG-D6P-A2  学生/班主任/归属/绑定物理 ER（规划）',
 ['stu_student','stu_student_class','stu_head_teacher','stu_head_teacher_assignment','stu_account_binding'])
render('FIG-D6P-B-考试成绩物理ER','FIG-D6P-B  考试/成绩物理 ER（规划）',
 ['stu_exam','stu_exam_class','stu_exam_subject','stu_score_task','stu_score'])
render('FIG-D6P-C1-发布版本物理ER','FIG-D6P-C1  发布/版本/明细物理 ER（规划）',
 ['stu_publication','stu_publication_version','stu_publication_version_detail'])
render('FIG-D6P-C2-审计绑定物理ER','FIG-D6P-C2  审计/账号绑定物理 ER（规划）',
 ['stu_audit_log','stu_account_binding'], ['sys_user'])
print('written')
