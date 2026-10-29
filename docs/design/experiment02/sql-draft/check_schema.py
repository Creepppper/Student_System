#!/usr/bin/env python3
"""Static-only checks; never connects to or executes against a database."""
import re, sys
from pathlib import Path
import schema_catalog as s

HERE=Path(__file__).resolve().parent
p0=(HERE/'01-p0-schema.sql').read_text(encoding='utf-8')
p1=(HERE/'02-p1-extensions.sql').read_text(encoding='utf-8')
allt=list(s.T.values()); names=list(s.T)
errs=[]
# unique names
if len(names)!=len(set(names)): errs.append('duplicate table names')
# id AI
for t in allt:
    idc=[c for c in t['cols'] if c['name']=='id']
    if not idc or idc[0]['type']!='INT' or idc[0].get('extra')!='AUTO_INCREMENT' or idc[0]['null']:
        errs.append(f'{t["name"]}: id is not INT NOT NULL AUTO_INCREMENT')
# FK refs / columns
for t in allt:
    cols={c['name'] for c in t['cols']}
    for fk in t['fks']:
        if fk[2] not in s.T: errs.append(f'{t["name"]}: missing FK target {fk[2]}')
        if fk[1] not in cols: errs.append(f'{t["name"]}: missing FK column {fk[1]}')
# unique/index columns
for t in allt:
    cols={c['name'] for c in t['cols']}
    for n,cs,_ in t['uniques']:
        if any(c not in cols for c in cs): errs.append(f'{t["name"]}: UK {n} unknown col')
    for n,cs,_ in t['indexes']:
        if any(c not in cols for c in cs): errs.append(f'{t["name"]}: IDX {n} unknown col')
# mutation statements (allow ON UPDATE)
muts=re.findall(r'(?im)^\s*(DROP|TRUNCATE|DELETE\s+FROM|INSERT\s+INTO|UPDATE\s+[A-Za-z_])',p0+'\n'+p1)
if muts: errs.append('mutation statements: '+str(muts))
# balanced delimiters / quotes
for label,text in [('P0',p0),('P1',p1)]:
    if text.count('(')!=text.count(')'): errs.append(f'{label}: parentheses unbalanced')
    if text.count("'")%2: errs.append(f'{label}: single quotes unbalanced')
# table counts / id AI count
p0_id=sum(1 for t in s.P0 if any(c['name']=='id' and c['type']=='INT' and c.get('extra')=='AUTO_INCREMENT' for c in t['cols']))
p1_id=sum(1 for t in s.P1 if any(c['name']=='id' and c['type']=='INT' and c.get('extra')=='AUTO_INCREMENT' for c in t['cols']))
report=[
 'SQL STATIC CHECK (no DB connection, no execution)',
 f'P0 tables: {len(s.P0)}; P1 tables: {len(s.P1)}',
 f'P0 id INT AI: {p0_id}/{len(s.P0)}; P1 id INT AI: {p1_id}/{len(s.P1)}',
 f'P0 catalog FKs: {sum(len(t["fks"]) for t in s.P0)}; P1 catalog FKs: {sum(len(t["fks"]) for t in s.P1)}',
 f'P0 SQL ALTER FK lines: {len(re.findall(r"^ALTER TABLE", p0, re.M))}',
 f'P1 SQL ALTER FK lines: {len(re.findall(r"^ALTER TABLE", p1, re.M))}',
 'mutation statements: none' if not muts else f'mutation statements: {muts}',
]
report.append('RESULT: PASS (static only; DDL not executed)' if not errs else 'RESULT: FAIL\n'+'\n'.join(errs))
(HERE/'static-check.txt').write_text('\n'.join(report)+'\n',encoding='utf-8')
print('\n'.join(report))
if errs: sys.exit(1)
