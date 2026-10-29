#!/usr/bin/env python3
"""Static OpenAPI checks; no HTTP/DB calls."""
import yaml, re, sys
from pathlib import Path
HERE=Path(__file__).resolve().parent
files={'P0':HERE/'p0-business-api.yaml','P1':HERE/'p1-extension-api.yaml'}
errs=[]; docs={}; ids={}; opids={}; refs_all=[]
for label,path in files.items():
    d=yaml.safe_load(path.read_text(encoding='utf-8')); docs[label]=d
    if d.get('openapi')!='3.0.3': errs.append(f'{label}: not OpenAPI 3.0.3')
    for p,item in d['paths'].items():
        if not p.startswith('/business/'): errs.append(f'{label}: path not /business/: {p}')
        for m,o in item.items():
            if m not in ('get','post','put','delete','patch'): continue
            oid=o.get('operationId')
            if not oid: errs.append(f'{label}: missing operationId {m} {p}')
            elif oid in opids: errs.append(f'duplicate operationId {oid}')
            else: opids[oid]=(label,m,p)
            aid=o.get('x-api-id')
            if not aid: errs.append(f'{label}: missing x-api-id {m} {p}')
            elif aid in ids: errs.append(f'duplicate x-api-id {aid}')
            else: ids[aid]=(label,m,p)
            if label=='P0' and str(aid).startswith('API-P1-'): errs.append(f'P1 id in P0: {aid}')
            if label=='P1' and not str(aid).startswith('API-P1-'): errs.append(f'non-P1 id in P1: {aid}')
            params=o.get('parameters',[]); pnames=[x['name'] for x in params if x.get('in')=='path']
            for n in re.findall(r'\{([^}]+)\}',p):
                if n not in pnames: errs.append(f'{label}: missing path param {n} {m} {p}')
            for x in params:
                if x.get('in')=='path' and not x.get('required'): errs.append(f'{label}: path param not required {x["name"]}')
            if '200' not in o.get('responses',{}): errs.append(f'{label}: no 200 {m} {p}')
            if 'requestBody' in o:
                sch=o['requestBody']['content']['application/json']['schema']
                if '$ref' not in sch: errs.append(f'{label}: non-ref body {m} {p}')
            for k in ('x-data-schema',):
                if k in o:
                    refs_all.append(o[k])
            def walk(x):
                if isinstance(x,dict):
                    for k,v in x.items():
                        if k=='$ref': refs_all.append(v)
                        walk(v)
                elif isinstance(x,list):
                    for v in x: walk(v)
            walk(o)
# refs
for label,d in docs.items():
    comps=d['components']['schemas']
    for r in refs_all:
        if r.startswith('#/components/schemas/'):
            n=r.split('/')[-1]
            if n not in comps: errs.append(f'{label}: missing schema {n}')
# counts
p0ops=sum(1 for v in docs['P0']['paths'].values() for m in v if m in ('get','post','put','delete','patch'))
p1ops=sum(1 for v in docs['P1']['paths'].values() for m in v if m in ('get','post','put','delete','patch'))
if len(ids)!=p0ops+p1ops: errs.append('x-api-id count != operation count')
# no P1 path in P0
p1paths=set(docs['P1']['paths'])
for p in docs['P0']['paths']:
    if p in p1paths: errs.append(f'P1 path also in P0: {p}')
report=[
 'OPENAPI STATIC CHECK (no HTTP, no DB)',
 f'P0 operations: {p0ops}; P1 operations: {p1ops}',
 f'P0 paths: {len(docs["P0"]["paths"])}; P1 paths: {len(docs["P1"]["paths"])}',
 f'unique x-api-id: {len(ids)}; unique operationId: {len(opids)}',
 'P0 only /business/: ' + str(all(p.startswith('/business/') for p in docs['P0']['paths'])),
 'P1 only /business/: ' + str(all(p.startswith('/business/') for p in docs['P1']['paths'])),
 'P1 paths excluded from P0: ' + str(not any(p in docs['P0']['paths'] for p in p1paths)),
 'schema refs: ' + ('OK' if not [e for e in errs if 'schema' in e] else 'ERROR'),
 'RESULT: PASS (static only; no endpoint deployed)' if not errs else 'RESULT: FAIL\n'+'\n'.join(errs)
]
(HERE/'static-check.txt').write_text('\n'.join(report)+'\n',encoding='utf-8')
print('\n'.join(report))
if errs: sys.exit(1)
