#!/usr/bin/env python3
"""Generate editable SVG sequence/flow diagrams (no external Python deps)."""
from pathlib import Path
import html

OUT = Path(__file__).resolve().parent

def svg(w,h,body,title):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">
<defs>
  <marker id="arrow" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L9,4.5 L0,9 z" fill="#52616B"/></marker>
  <marker id="arrow-d" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L9,4.5 L0,9 z" fill="#9AA7B4"/></marker>
</defs>
<rect width="{w}" height="{h}" fill="#FFFFFF"/>
<text x="{w/2}" y="30" text-anchor="middle" font-family="SimHei" font-size="20" font-weight="bold" fill="#111827">{html.escape(title)}</text>
{body}
</svg>'''

def esc(s): return html.escape(str(s))

def box(x,y,w,h,label,fill="#DCEEFF",stroke="#2F6FED",dash=False,fs=14):
    d=' stroke-dasharray="6 4"' if dash else ''
    # multi-line centered
    lines=label.split('\n'); n=len(lines); lh=18
    y0=y+h/2-(n-1)*lh/2+5
    tsp=''.join(f'<text x="{x+w/2}" y="{y0+i*lh}" text-anchor="middle" font-family="SimHei" font-size="{fs}" fill="#111827">{esc(t)}</text>' for i,t in enumerate(lines))
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="{fill}" stroke="{stroke}" stroke-width="1.4"{" stroke-dasharray=\"6 4\"" if dash else ""}/>{tsp}'

def line(x1,y1,x2,y2,label="",dash=False,color="#52616B",label_y=None):
    d=' stroke-dasharray="5 4"' if dash else ''
    mark='arrow-d' if dash else 'arrow'
    s=f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="1.3"{d} marker-end="url(#{mark})"/>'
    if label:
        ly=label_y if label_y is not None else (y1+y2)/2-6
        s+=f'<text x="{(x1+x2)/2}" y="{ly}" text-anchor="middle" font-family="SimHei" font-size="11" fill="#374151">{esc(label)}</text>'
    return s

def note(x,y,w,h,lines,fill="#F8FAFC",stroke="#9AA7B4",fs=11):
    s=f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" fill="{fill}" stroke="{stroke}" stroke-width="1"/>'
    for i,l in enumerate(lines):
        s+=f'<text x="{x+10}" y="{y+20+i*16}" font-family="SimHei" font-size="{fs}" fill="#374151">{esc(l)}</text>'
    return s

# D3B: two vertical flows side by side
w,h=1600,920
b=[]
# panels
b.append('<rect x="40" y="55" width="730" height="790" rx="12" fill="#F7FAFF" stroke="#2F6FED" stroke-width="1.3"/>')
b.append('<rect x="820" y="55" width="740" height="790" rx="12" fill="#FFFDF8" stroke="#D97706" stroke-width="1.3"/>')
b.append('<text x="405" y="85" text-anchor="middle" font-family="SimHei" font-size="16" font-weight="bold" fill="#2F6FED">登录初始化（现有）</text>')
b.append('<text x="1190" y="85" text-anchor="middle" font-family="SimHei" font-size="16" font-weight="bold" fill="#D97706">普通业务请求（学生业务为规划）</text>')
# login column
login=[('登录页\nlogin.vue','#DCEEFF','#2F6FED',False),('api/login.js\nlogin()','#DCEEFF','#2F6FED',False),('request.js\nAxios 实例','#DCEEFF','#2F6FED',False),('SysLoginController\nPOST /login','#DCEEFF','#2F6FED',False),('SysLoginService\nTokenService','#DCEEFF','#2F6FED',False),('Valkey\n验证码/LoginUser','#DFF3E4','#2E7D32',False),('SysUser / Role / Menu\nDB 查询','#DFF3E4','#2E7D32',False)]
ys=[115,210,305,400,495,600,695]
for (lab,fill,st,dash),y in zip(login,ys):
    b.append(box(160,y,490,55,lab,fill,st,False))
for i in range(len(login)-1):
    b.append(line(405,ys[i]+55,405,ys[i]+85,'',False))
# business column
biz=[('业务页面（规划）\nsrc/views/business','#FFE7CF','#D97706',True),('业务 API（规划）\nsrc/api/business','#FFE7CF','#D97706',True),('request.js\nBearer Token','#DCEEFF','#2F6FED',False),('Spring Security\nJWT Filter','#DCEEFF','#2F6FED',False),('业务 Controller（规划）\n@PreAuthorize','#FFE7CF','#D97706',True),('业务 Service（规划）\n@DataScope + 业务范围','#FFE7CF','#D97706',True),('Mapper + MyBatis XML\nMariaDB（规划业务表）','#DFF3E4','#2E7D32',True)]
bys=[115,200,285,370,455,540,625]
for (lab,fill,st,dash),y in zip(biz,bys):
    b.append(box(950,y,480,55,lab,fill,st,None))
for i in range(len(biz)-1):
    b.append(line(1190,bys[i]+55,1190,bys[i]+70,'',False))
b.append(note(940,700,490,58,['后端必须二次校验；前端按钮隐藏不是安全边界；','学生业务 Controller/Service 尚未实现。']))
Path(OUT/'FIG-D3B-请求与权限交互.svg').write_text(svg(w,h,'\n'.join(b),'FIG-D3B  请求与权限交互（登录初始化 vs 普通业务请求）'),encoding='utf-8')

# Sequence generator
def seq(title, actors, messages, note_lines, outname):
    # actors: list of (label, planned)
    n=len(actors); W=1700; H=1010; left=100; right=W-100
    gap=(right-left)/(n-1); cx=[left+i*gap for i in range(n)]
    b=[f'<rect x="15" y="45" width="{W-30}" height="{H-60}" fill="#FFFFFF" stroke="#D1D5DB" stroke-width="1"/>']
    for i,(lab,planned) in enumerate(actors):
        fill='#FFE7CF' if planned else '#DCEEFF'; st='#D97706' if planned else '#2F6FED'
        b.append(box(cx[i]-88,55,176,58,lab,fill,st,planned,fs=13))
        b.append(f'<line x1="{cx[i]}" y1="113" x2="{cx[i]}" y2="{H-95}" stroke="#9CA3AF" stroke-width="1" stroke-dasharray="4 4"/>')
    start=175; step=40
    for j,(src,dst,label,ret) in enumerate(messages):
        y=start+j*step
        color='#9AA7B4' if ret else '#52616B'
        b.append(line(cx[src],y,cx[dst],y,label,ret,color,label_y=y-7))
        b.append(f'<text x="28" y="{y+4}" font-family="SimHei" font-size="10" fill="#6B7280">{j+1}</text>')
    b.append(note(40,H-85,W-80,45,note_lines))
    Path(OUT/outname).write_text(svg(W,H,'\n'.join(b),title),encoding='utf-8')

seq('FIG-D7  登录与权限获取时序图',
    [('登录页\nlogin.vue',False),('Axios\nrequest.js',False),('SysLoginController\n/login',False),('SysLoginService\nTokenService',False),('Valkey\n验证码/LoginUser',False),('SysUser/Role/Menu\nDB 查询',False)],
    [(0,1,'GET /captchaImage',False),(1,2,'转发',False),(2,4,'生成验证码/UUID',False),(2,0,'uuid + 图片（返回）',True),
     (0,1,'POST /login',False),(1,2,'转发',False),(2,3,'login(username,password,code,uuid)',False),
     (3,4,'校验验证码',False),(3,5,'selectUserByUserName / getRolePermission / getMenuPermission',False),(3,4,'保存 LoginUser',False),
     (2,0,'JWT Token（不展开）',True),(0,1,'GET /getInfo + Bearer',False),(1,2,'转发 + Bearer',False),
     (2,5,'getInfo / roles / permissions',False),(2,0,'roles / permissions（返回）',True),
     (0,1,'GET /getRouters',False),(1,2,'转发',False),(2,5,'selectMenuTreeByUserId',False),(2,0,'菜单树（返回）',True)],
    ['依据：SysLoginController / SysLoginService / TokenService / SysPermissionService / SecurityConfig；Token 不应出现在文档中。'],
    'FIG-D7-登录与权限获取时序图.svg')

seq('FIG-D8  用户管理调用链时序图',
    [('用户页\nsystem/user/index.vue',False),('API\napi/system/user.js',False),('Axios\nrequest.js',False),('SysUserController\n/system/user',False),('SysUserServiceImpl\nISysUserService',False),('SysUserMapper +\nXML',False),('MariaDB\n(sys_user/sys_dept)',False)],
    [(0,1,'getList() → listUser(query)',False),(1,2,'GET /system/user/list',False),(2,3,'Authorization: Bearer',False),
     (3,4,'@PreAuthorize("system:user:list")\nselectUserList(query)',False),(4,5,'@DataScope(...)\nselectUserList(query)',False),
     (5,6,'SQL: select ...',False),(6,5,'用户记录 rows',True),(5,4,'List<SysUser>',True),(4,3,'TableDataInfo(rows,total)',True),
     (3,2,'JSON 响应',True),(2,0,'userList + total → 表格',True)],
    ['Mapper/XML 不是独立进程；SQL 由 MyBatis 执行；@DataScope 由 AOP/查询拼接，不能等同“本班”业务范围。'],
    'FIG-D8-用户管理调用链时序图.svg')
print('SVGs written')
