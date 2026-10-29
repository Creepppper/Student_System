#!/usr/bin/env python3
"""Generate planned API sequence diagrams as editable SVG; no server calls."""
from pathlib import Path
import html
OUT=Path(__file__).resolve().parent

def esc(s): return html.escape(str(s))
def box(x,y,w,h,label,fill='#DCEEFF',stroke='#2F6FED',fs=15,dash=False):
    lines=label.split('\n'); lh=17; y0=y+h/2-(len(lines)-1)*lh/2+5
    ts=''.join(f'<text x="{x+w/2}" y="{y0+i*lh}" text-anchor="middle" font-family="SimHei" font-size="{fs}" fill="#111827">{esc(t)}</text>' for i,t in enumerate(lines))
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="7" fill="{fill}" stroke="{stroke}" stroke-width="1.3"{" stroke-dasharray=\"6 4\"" if dash else ""}/>{ts}'
def line(x1,y1,x2,y2,label='',dash=False,color='#52616B',fs=15):
    d=' stroke-dasharray="5 4"' if dash else ''
    s=f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="1.25"{d} marker-end="url(#arrow)"/>'
    if label: s+=f'<text x="{(x1+x2)/2}" y="{y1-6}" text-anchor="middle" font-family="SimHei" font-size="{fs}" fill="#374151">{esc(label)}</text>'
    return s
def note(x,y,w,h,lines,fill='#F8FAFC',stroke='#9AA7B4',fs=12):
    s=f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" fill="{fill}" stroke="{stroke}"/>'
    for i,l in enumerate(lines): s+=f'<text x="{x+10}" y="{y+20+i*16}" font-family="SimHei" font-size="{fs}" fill="#374151">{esc(l)}</text>'
    return s
def svg(w,h,body,title):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">
<defs><marker id="arrow" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto"><path d="M0,0 L9,4.5 L0,9 z" fill="#52616B"/></marker></defs>
<rect width="{w}" height="{h}" fill="#FFFFFF"/>
<text x="{w/2}" y="30" text-anchor="middle" font-family="SimHei" font-size="20" font-weight="bold" fill="#111827">{esc(title)}</text>
{body}</svg>'''
def seq(title,actors,messages,notes,outname):
    W=1500; H=720; left=75; right=W-75; n=len(actors); gap=(right-left)/(n-1); cx=[left+i*gap for i in range(n)]
    b=[]
    for i,(label,planned) in enumerate(actors):
        fill='#FFE7CF' if planned else '#DCEEFF'; st='#D97706' if planned else '#2F6FED'
        b.append(box(cx[i]-80,42,160,62,label,fill,st,15,planned))
        b.append(f'<line x1="{cx[i]}" y1="104" x2="{cx[i]}" y2="{H-80}" stroke="#9CA3AF" stroke-width="1" stroke-dasharray="4 4"/>')
    start=150; step=46
    for j,(src,dst,label,ret) in enumerate(messages):
        y=start+j*step; color='#9AA7B4' if ret else '#52616B'
        if src==dst:
            x=cx[src]+18
            b.append(f'<path d="M{x},{y} h72 v26 h-72" fill="none" stroke="{color}" stroke-width="1.2" marker-end="url(#arrow)"/>')
            b.append(f'<text x="{x+36}" y="{y-6}" text-anchor="middle" font-family="SimHei" font-size="10" fill="#374151">{esc(label)}</text>')
        else:
            b.append(line(cx[src],y,cx[dst],y,label,ret,color))
        b.append(f'<text x="25" y="{y+4}" font-family="SimHei" font-size="10" fill="#6B7280">{j+1}</text>')
    b.append(note(30,H-76,W-60,42,notes))
    Path(OUT/outname).write_text(svg(W,H,'\n'.join(b),title),encoding='utf-8')

# D9A
seq('FIG-D9A  班主任录入与提交成绩（计划组件交互）',
 [('班主任页面\nVue3',False),('ScoreTask\nController',False),('ScoreTask\nService',False),('Task/Score\nMapper',False),('MariaDB\n任务/成绩',False),('Audit\nService',False)],
 [(0,1,'PUT /business/score-tasks/{id}/scores',False),(1,2,'JWT Filter + business:score:edit',False),
  (2,3,'select task FOR UPDATE / select scores',False),(3,4,'SQL（任务/花名册/成绩）',False),(4,3,'rows',True),
  (2,2,'校验：任职/考试/任务/三态/0.5/版本',False),
  (2,3,'upsert 草稿；version+1',False),(2,5,'同事务写审计',False),(5,4,'insert audit',False),
  (2,1,'ScoreSaveResult',True),(1,0,'AjaxResult(data)',True)],
 ['依据：19 张 P0 表、状态机与 API-SCORE-004；橙=规划。版本冲突/非法分数返回 409/400，失败回滚、不静默覆盖。'],
 'FIG-D9A-录入提交成绩时序图.svg')
# D9B
seq('FIG-D9B  教务审核与退回（计划组件交互）',
 [('教务页面\nVue3',False),('ScoreReview\nController',False),('ScoreReview\nService',False),('Task/Audit\nMapper',False),('MariaDB\n任务/审计',False)],
 [(0,1,'POST /business/score-tasks/{id}/review',False),(1,2,'JWT Filter + business:score:review',False),
  (2,3,'锁任务 + select task',False),(3,4,'SQL',False),(4,3,'task',True),
  (2,2,'校验：TASK_SUBMITTED / currentUser≠submittedBy / reason',False),
  (2,3,'update status APPROVED/RETURNED + audit',False),(3,4,'transaction',False),
  (2,1,'ScoreTask',True),(1,0,'AjaxResult',True)],
 ['依据：FR-PUB-001、API-SCORE-007；审核与录入职责分离；退回原因必填；重复审核/自审 409/403。'],
 'FIG-D9B-审核退回时序图.svg')
# D9C
seq('FIG-D9C  成绩发布与不可变版本（计划组件交互）',
 [('教务页面\nVue3',False),('Publication\nController',False),('Publication\nService',False),('Task/Score\nMapper',False),('MariaDB\n发布/版本/明细',False),('Audit\nService',False)],
 [(0,1,'POST /business/publications',False),(1,2,'JWT Filter + business:publish:create',False),
  (2,3,'lock tasks；校验全部 APPROVED/无未录入/至少一科',False),(3,4,'select tasks/scores',False),
  (4,3,'rows',True),(2,4,'insert publication/version/detail',False),
  (2,4,'update current_version_id/status',False),(2,5,'同事务审计',False),(5,4,'insert audit',False),(2,1,'Publication + version',True),(1,0,'AjaxResult',True)],
 ['依据：FR-PUB-002、API-PUB-002；发布在单事务生成不可变版本与明细快照；任一步失败回滚；TASK_APPROVED ≠ PUB_PUBLISHED。'],
 'FIG-D9C-发布版本时序图.svg')
# D9D: named flow comparison, three branches; full /business paths
W,H=1500,650; b=[]
b.append(box(25,52,450,52,'撤回 /business/publications/{id}/withdraw','#DCEEFF','#2F6FED',14))
b.append(box(525,52,450,52,'重新发布 /business/publications/{id}/republish','#DCEEFF','#2F6FED',14))
b.append(box(1025,52,450,52,'受控更正 /business/publications/{id}/corrections','#DCEEFF','#2F6FED',14))
rows=[
 (25,[('锁 publication；校验 PUB_PUBLISHED',),('更新 status=PUB_WITHDRAWN',),('保留版本/明细',),('同事务审计',),('返回 AjaxResult',)],'#2F6FED'),
 (525,[('锁 publication/任务',),('校验 PUB_WITHDRAWN',),('校验任务仍 APPROVED',),('新 version_no+1',),('插入 version/detail',),('PUB_PUBLISHED；审计',)],'#2F6FED'),
 (1025,[('锁 publication/score',),('校验 PUB_PUBLISHED',),('校验原因/非自审',),('新版本 + 全量明细快照',),('current_version_id 更新',),('审计 before/after',)],'#6A1B9A')]
for x,items,color in rows:
    y=135
    for i,(lab,) in enumerate(items):
        b.append(box(x,y,450,46,lab,'#FFE7CF' if color=='#2F6FED' else '#EADCF8',color,15,True))
        if i<len(items)-1: b.append(line(x+225,y+46,x+225,y+64,'',False,color))
        y+=64
b.append(note(25,H-72,W-50,50,['FIG-D9D 为三条计划分支对照（不是生命周期状态机）：撤回不删历史；重发版本+1；更正保持 PUB_PUBLISHED 并生成新版本。','所有状态/版本/审计在同一事务，失败回滚；并发冲突 409；学生只读当前 PUB_PUBLISHED。']))
Path(OUT/'FIG-D9D-撤回重发更正流程对照图.svg').write_text(svg(W,H,'\n'.join(b),'FIG-D9D  撤回、重发与更正流程对照（计划组件交互）'),encoding='utf-8')
# D9E
seq('FIG-D9E  学生查询本人已发布成绩（计划组件交互）',
 [('学生页面\nVue3',False),('MyScore\nController',False),('MyScore\nService',False),('Binding/Publication\nMapper',False),('MariaDB\n绑定/发布/明细',False)],
 [(0,1,'GET /business/my-scores/{examId}',False),(1,2,'JWT + business:myscore:query',False),
  (2,3,'根据 user_id 查有效 student 绑定',False),(3,4,'select binding',False),(4,3,'studentId',True),
  (2,3,'select current PUB_PUBLISHED version/detail',False),(3,4,'SQL（不接受客户端 studentId）',False),(4,3,'rows',True),
  (2,1,'MyScoreDetail（按版本口径）',True),(1,0,'AjaxResult',True)],
 ['依据：FR-QUERY-001/002、API-QUERY-002；学生身份来自绑定，不从请求参数取 studentId；撤回/未发布/他人→403/404；系统管理员不能借通用权限查看。'],
 'FIG-D9E-学生查询成绩时序图.svg')
print('API SVGs written')
