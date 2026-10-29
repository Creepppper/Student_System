const params = new URLSearchParams(location.search);
const view = params.get('view') || 'login';
const state = params.get('state') || '';
const modal = params.get('modal') || '';
const forcedRole = params.get('role') || '';

const roleInfo = {
  academic: {name:'教务管理员', short:'教务', avatar:'李'},
  head: {name:'班主任', short:'班主任', avatar:'张'},
  student: {name:'学生', short:'学生', avatar:'陈'},
  sysadmin: {name:'系统管理员', short:'管理员', avatar:'A'}
};

const menus = {
  academic: [
    {section:'基础资料', items:[
      {id:'students', label:'学生信息管理', view:'students'},
      {id:'classes', label:'班主任与班级', view:'classes'}
    ]},
    {section:'考试与成绩', items:[
      {id:'exams', label:'考试管理', view:'exams'},
      {id:'review', label:'成绩审核与发布', view:'review'}
    ]},
    {section:'分析与审计', items:[
      {id:'stats', label:'成绩统计', view:'stats', role:'academic'},
      {id:'audit', label:'业务变更记录', view:'audit'}
    ]},
    {section:'P1 规划功能', items:[
      {id:'review-final', label:'复核最终确认', view:'review-final', p1:true}
    ]}
  ],
  head: [
    {section:'我的班级', items:[
      {id:'grades', label:'成绩录入', view:'grades'},
      {id:'stats', label:'本班成绩统计', view:'stats'}
    ]},
    {section:'P1 规划功能', items:[
      {id:'review-process', label:'复核受理与核查', view:'review-process', p1:true}
    ]}
  ],
  student: [
    {section:'我的成绩', items:[
      {id:'query', label:'本人成绩查询', view:'query'}
    ]},
    {section:'P1 规划功能', items:[
      {id:'review-apply', label:'成绩复核申请', view:'review-apply', p1:true}
    ]}
  ],
  sysadmin: [
    {section:'系统与安全', items:[
      {id:'audit', label:'审计摘要（脱敏）', view:'audit', sys:true}
    ]}
  ]
};

const menuMeta = {
  login:{eyebrow:'SECURITY ACCESS', title:'登录系统', sub:'账号、密码与验证码校验；原型不连接真实认证服务。'},
  students:{role:'academic', active:'students', eyebrow:'ACADEMIC / STUDENTS', title:'学生信息管理', sub:'维护学生档案、班级归属与账号关联；数据为虚构演示数据。'},
  classes:{role:'academic', active:'classes', eyebrow:'ACADEMIC / CLASS OWNERSHIP', title:'班主任与班级管理', sub:'查看班级当前班主任、任职起始与变更历史；每班同一时刻最多一名有效班主任。'},
  exams:{role:'academic', active:'exams', eyebrow:'ACADEMIC / EXAMS', title:'考试管理', sub:'配置考试范围、适用科目与分值；考试状态和成绩发布状态分别管理。'},
  grades:{role:'head', active:'grades', eyebrow:'HEAD TEACHER / SCORE ENTRY', title:'成绩录入与提交', sub:'班主任维护本班全部适用科目；支持 0.5 分步长、缺考与未录入三态。'},
  review:{role:'academic', active:'review', eyebrow:'ACADEMIC / REVIEW & PUBLISH', title:'成绩审核与发布', sub:'逐任务审核、退回、按考试×班级发布；发布版本与撤回/更正留痕。'},
  query:{role:'student', active:'query', eyebrow:'STUDENT / MY SCORES', title:'本人成绩查询', sub:'仅展示本人已发布成绩、总分和班级排名；不展示其他学生数据。'},
  stats:{role:'head', active:'stats', eyebrow:'HEAD TEACHER / ANALYTICS', title:'本班成绩统计', sub:'本班单场考试、单科与总分汇总；缺考/未录入单列，完整总分才参与排名。'},
  audit:{role:'academic', active:'audit', eyebrow:'ACADEMIC / BUSINESS AUDIT', title:'业务变更记录', sub:'查看成绩、任务、发布与更正事件；前后值、原因和操作人可追踪。'},
  'review-apply':{role:'student', active:'review-apply', eyebrow:'P1 / STUDENT REVIEW', title:'成绩复核申请', sub:'P1 规划功能：学生仅对本人已发布成绩提交复核申请。'},
  'review-process':{role:'head', active:'review-process', eyebrow:'P1 / HEAD TEACHER REVIEW', title:'复核受理与核查', sub:'P1 规划功能：班主任受理本班申请并提交核查意见，不直接改分。'},
  'review-final':{role:'academic', active:'review-final', eyebrow:'P1 / ACADEMIC CONFIRM', title:'复核最终确认', sub:'P1 规划功能：教务最终确认、驳回或受控更正；更正生成新版本并留痕。'}
};

function esc(s){return String(s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));}
function tag(text, cls='gray'){return `<span class="tag ${cls}">${esc(text)}</span>`;}
function p1(){return `<span class="pill p1">P1 · 规划功能</span>`;}
function btn(label, cls='', extra=''){return `<button class="btn ${cls}" ${extra}>${label}</button>`;}
function icon(name){
  const map={search:'⌕',plus:'＋',refresh:'↻',more:'⋯',bell:'○',user:'○',download:'↓',history:'↺',check:'✓',close:'×'};
  return map[name]||'';
}

function nav(role, active){
  const groups=menus[role]||menus.academic;
  return groups.map(g=>`<div class="nav-section">${g.section}</div>`+
    g.items.map(i=>{
      const href=`?view=${encodeURIComponent(i.view)}${i.role?'&role='+i.role:''}${i.view==='audit'&&i.sys?'&role=sys':''}`;
      return `<a class="nav-item ${active===i.id?'active':''}" href="${href}"><span class="dot"></span><span>${i.label}</span>${i.p1?'<span class="pill p1" style="margin-left:auto;transform:scale(.82)">P1</span>':''}</a>`;
    }).join('')
  ).join('');
}

function layout(role, active, eyebrow, title, subtitle, actions, content, opts={}){
  const r=roleInfo[role]||roleInfo.academic;
  const crumb=opts.crumb||`${r.short} / ${eyebrow.split('/').slice(-1)[0].trim()}`;
  return `<div class="app">
    <aside class="sidebar">
      <div class="brand"><div class="brand-mark">高</div><div><div class="brand-title">学生信息与成绩</div><div class="brand-sub">HIGH SCHOOL · SE</div></div></div>
      <div class="side-school"><span>●</span><span>2026—2027 学年 · <b>第一学期</b></span></div>
      <nav class="nav">${nav(role,active)}</nav>
      <div class="side-foot">需求原型 · 非正式前端<br>虚构数据 · 仅用于实验一截图</div>
    </aside>
    <div class="main">
      <header class="topbar">
        <div class="crumbs"><span>首页</span><span>/</span><b>${esc(crumb)}</b></div>
        <div class="topbar-title">${esc(title)}</div>
        <div class="top-actions">
          <button class="icon-btn" title="刷新">↻</button>
          <button class="icon-btn" title="通知">○</button>
          <div class="role-chip"><div class="avatar">${r.avatar}</div><span>${r.name}</span></div>
          <span class="muted small">原型环境</span>
        </div>
      </header>
      <main class="content">
        <div class="page-head">
          <div>
            <div class="page-eyebrow">${eyebrow}</div>
            <h1 class="page-title">${title}${opts.p1?' '+p1():''}</h1>
            <div class="page-sub">${subtitle}</div>
          </div>
          <div class="page-actions">${actions||''}</div>
        </div>
        ${content}
      </main>
    </div>
  </div>`;
}

function filters(fields, actions){
  return `<div class="card"><div class="card-body"><div class="filters">${fields}${actions?`<div class="filter-actions">${actions}</div>`:''}</div></div></div>`;
}
function field(label, control){return `<div class="field"><label>${label}</label>${control}</div>`;}
function select(options, selected){return `<select class="select">${options.map(o=>`<option ${o===selected?'selected':''}>${o}</option>`).join('')}</select>`;}
function input(value, placeholder=''){return `<input class="input" value="${esc(value)}" placeholder="${esc(placeholder)}">`;}

function pager(total, current=1){
  return `<div class="pagination"><span>共 ${total} 条记录 · 每页 10 条</span><div class="pager"><span>‹</span><span class="active">${current}</span><span>2</span><span>3</span><span>›</span></div></div>`;
}

/* ---------------- Login ---------------- */
function loginPage(){
  const err=state==='error';
  return `<div class="login">
    <section class="login-left">
      <div class="login-brand"><div class="brand-mark">高</div><div><b>高中学生信息与成绩管理系统</b><span>STUDENT INFORMATION & SCORE MANAGEMENT</span></div></div>
      <div class="login-hero">
        <h1>把每一次成绩，<br>变成可追溯的教学事实。</h1>
        <p>面向教务、班主任与学生的统一信息与成绩工作台。以班级责任、审核发布和业务审计为核心，减少重复录入，守住成绩数据的可信边界。</p>
        <div class="login-features">
          <div class="login-feature"><b>35</b><span>P0 必须交付功能</span></div>
          <div class="login-feature"><b>4</b><span>业务角色 · 清晰职责</span></div>
          <div class="login-feature"><b>100%</b><span>关键变更可审计</span></div>
        </div>
      </div>
      <div class="login-foot">课程原型 · 不连接真实数据库 / 不包含真实学生信息</div>
    </section>
    <section class="login-right">
      <div class="login-card">
        <div class="eyebrow">WELCOME BACK</div>
        <h2>登录工作台</h2>
        <p class="welcome">使用学校统一账号登录。不同角色登录后只看到授权范围内的功能与数据。</p>
        <form class="login-form" onsubmit="return false">
          ${err?`<div class="login-error"><span>!</span><span>验证码不正确，请重新输入。</span></div>`:''}
          <div class="field"><label>账号</label><input class="input" value="academic01" autocomplete="off"></div>
          <div class="field"><label>密码</label><input class="input" type="password" value="demo123456"></div>
          <div class="field"><label>验证码</label><div class="captcha-row"><input class="input" value="7K3P" autocomplete="off"><div class="captcha">7K3P</div></div></div>
          <div class="login-options"><label class="check"><input type="checkbox" checked> 记住账号</label><span>忘记密码请联系管理员</span></div>
          <button class="login-btn" type="submit">登 录</button>
        </form>
        <div class="demo-line">演示角色（截图用，不执行真实认证）
          <div class="demo-roles"><span>系统管理员</span><span>教务管理员</span><span>班主任</span><span>学生</span></div>
        </div>
      </div>
      <div class="login-legal">v1.2 · 实验一需求原型</div>
    </section>
  </div>`;
}

/* ---------------- UI-02 students ---------------- */
const students=[
  ['20260101','陈星宇','男','高二(3)班','在读','已绑定','138****2210'],
  ['20260102','李若彤','女','高二(3)班','在读','已绑定','139****0182'],
  ['20260103','王浩然','男','高二(3)班','在读','已绑定','136****7741'],
  ['20260104','赵雨桐','女','高二(3)班','在读','已绑定','137****9062'],
  ['20260105','周子轩','男','高二(3)班','在读','未绑定','—'],
  ['20260106','林可欣','女','高二(3)班','在读','已绑定','135****4418'],
  ['20260107','孙嘉宁','女','高二(3)班','休学','已绑定','138****2207'],
  ['20260108','吴思远','男','高二(3)班','在读','已绑定','139****5639']
];
function studentsPage(){
  const rows=students.map((s,i)=>`<tr>
    <td class="mono">${s[0]}</td><td class="table-name">${s[1]}</td><td>${s[2]}</td><td>${s[3]}</td>
    <td>${tag(s[4],s[4]==='在读'?'green':'orange')}</td><td>${s[5]==='已绑定'?tag('已绑定','blue'):tag('未绑定','gray')}</td>
    <td class="muted">${s[6]}</td><td><button class="btn link" onclick="location.href='?view=students&modal=edit'">编辑</button><button class="btn link">归属</button></td>
  </tr>`).join('');
  const content=`${filters(
    field('学年',select(['2026—2027','2025—2026'],'2026—2027'))+
    field('年级',select(['高二','高一','高三'],'高二'))+
    field('班级',select(['高二(3)班','高二(1)班','高二(2)班'],'高二(3)班'))+
    field('关键词',input('','学号 / 姓名'))+
    field('学籍状态',select(['全部','在读','休学','转出'],'全部')),
    `<button class="btn soft">${icon('search')} 查询</button><button class="btn">${icon('refresh')} 重置</button>`)}
    <div class="card">
      <div class="card-head"><div class="card-title"><span class="accent"></span>学生档案 <span class="card-sub">共 8 名演示学生</span></div><div class="rowline"><button class="btn soft" onclick="location.href='?view=students&modal=edit'">${icon('plus')} 新增学生</button><button class="btn">${icon('download')} 导出名单</button></div></div>
      <div class="card-body" style="padding-top:10px">
        <div class="table-wrap"><table>
          <colgroup><col style="width:11%"><col style="width:10%"><col style="width:6%"><col style="width:12%"><col style="width:9%"><col style="width:9%"><col style="width:13%"><col style="width:16%"></colgroup>
          <thead><tr><th>学号</th><th>姓名</th><th>性别</th><th>当前班级</th><th>学籍状态</th><th>账号</th><th>联系电话</th><th>操作</th></tr></thead>
          <tbody>${rows}</tbody>
        </table></div>
        ${pager(8)}
      </div>
    </div>
    ${modal==='edit'?studentEditModal():''}`;
  return layout('academic','students','ACADEMIC / STUDENTS','学生信息管理','维护学生档案、班级归属与账号关联；数据为虚构演示数据。',`<button class="btn">${icon('download')} 导出</button><button class="btn primary" onclick="location.href='?view=students&modal=edit'">${icon('plus')} 新增学生</button>`,content);
}
function studentEditModal(){
  return `<div class="modal-backdrop"><div class="modal wide">
    <div class="modal-head"><div><div class="modal-title">编辑学生档案</div><div class="modal-sub">学号 20260101 · 当前归属 2026—2027 / 高二(3)班</div></div><button class="close" onclick="location.href='?view=students'">×</button></div>
    <div class="modal-body">
      <div class="form-grid">
        <div class="form-item"><label>学号 <span style="color:#d64545">*</span></label><input value="20260101" disabled><span class="hint">学号全校唯一且稳定，不允许物理删除后重建。</span></div>
        <div class="form-item"><label>姓名 <span style="color:#d64545">*</span></label><input value="陈星宇"></div>
        <div class="form-item"><label>性别</label><select><option>男</option><option>女</option></select></div>
        <div class="form-item"><label>学籍状态</label><select><option>在读</option><option>休学</option><option>转出</option></select></div>
        <div class="form-item"><label>当前班级</label><input value="高二(3)班" disabled><span class="hint">班级变更请使用“归属变更”，不得直接覆盖历史。</span></div>
        <div class="form-item"><label>账号绑定</label><input value="已绑定 · stu20260101" disabled></div>
        <div class="form-item full"><label>班级归属历史</label><div class="note"><b>2026—2027 第一学期</b> · 高二(3)班 · 2026-09-01 至今 · 当前有效<br><span class="muted">历史归属不可覆盖；转班将新增一条归属记录并保留原记录。</span></div></div>
      </div>
    </div>
    <div class="modal-foot"><button class="btn" onclick="location.href='?view=students'">取消</button><button class="btn primary">保存档案</button></div>
  </div></div>`;
}

/* ---------------- UI-03 classes ---------------- */
const classes=[
  ['高二(1)班','高二','张老师','T2018','2026-09-01','有效'],
  ['高二(2)班','高二','王老师','T2021','2026-09-01','有效'],
  ['高二(3)班','高二','张老师','T2018','2026-09-01','有效'],
  ['高二(4)班','高二','刘老师','T2019','2026-09-01','有效'],
  ['高二(5)班','高二','陈老师','T2022','2026-09-01','有效'],
  ['高二(6)班','高二','—','—','—','待指派']
];
function classesPage(){
  const rows=classes.map(c=>`<tr><td class="table-name">${c[0]}</td><td>${c[1]}</td><td>${c[5]==='待指派'?'—':c[2]}</td><td class="mono">${c[3]}</td><td>${c[4]}</td><td>${tag(c[5],c[5]==='有效'?'green':'orange')}</td>
    <td><button class="btn link">${c[5]==='有效'?'变更':'指派'}</button><button class="btn link">历史</button></td></tr>`).join('');
  const content=`<div class="grid-2">
    <div>
      ${filters(field('学年',select(['2026—2027','2025—2026'],'2026—2027'))+field('年级',select(['高二','高一','高三'],'高二'))+field('班级',select(['全部','高二(1)班','高二(3)班'],'全部')),`<button class="btn soft">${icon('search')} 查询</button>`)}
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>班级与当前班主任</div><div class="rowline"><button class="btn primary" onclick="location.href='?view=classes&modal=assign'">${icon('plus')} 指派班主任</button></div></div>
      <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><colgroup><col style="width:18%"><col style="width:9%"><col style="width:13%"><col style="width:11%"><col style="width:13%"><col style="width:9%"><col style="width:15%"></colgroup><thead><tr><th>班级</th><th>年级</th><th>当前班主任</th><th>工号</th><th>任职起始</th><th>状态</th><th>操作</th></tr></thead><tbody>${rows}</tbody></table></div>${pager(6)}</div></div>
    </div>
    <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>高二(3)班 · 当前任职</div><span class="tag green">有效</span></div>
      <div class="side-info"><div class="profile-line"><div class="avatar-big">张</div><div><h3>张老师</h3><p>工号 T2018 · 班主任</p></div></div>
      <div class="info-list" style="margin-top:16px"><div class="info-row"><span>任职起始</span><span>2026-09-01</span></div><div class="info-row"><span>当前班级数</span><span>2 个班</span></div><div class="info-row"><span>责任范围</span><span>本班全部适用科目</span></div></div>
      <div class="note" style="margin-top:13px">每班同一时刻最多 1 名有效班主任；班主任可负责多个班。历史任职保留，不覆盖。</div>
      <div class="card-title" style="margin:17px 0 8px;font-size:11.5px"><span class="accent" style="height:12px"></span>任职历史</div>
      <div class="info-list"><div class="info-row"><span>2025—2026 学年</span><span>高二(2)班 · 已结束</span></div><div class="info-row"><span>2024—2025 学年</span><span>高一(3)班 · 已结束</span></div></div>
      <div style="margin-top:14px"><button class="btn soft sm" onclick="location.href='?view=classes&modal=assign'">变更当前班主任</button></div>
      </div>
    </div>
  </div>${modal==='assign'?assignModal():''}`;
  return layout('academic','classes','ACADEMIC / CLASS OWNERSHIP','班主任与班级管理','查看班级当前班主任、任职起始与变更历史；每班同一时刻最多一名有效班主任。',`<button class="btn">${icon('history')} 变更记录</button><button class="btn primary" onclick="location.href='?view=classes&modal=assign'">${icon('plus')} 指派班主任</button>`,content);
}
function assignModal(){
  return `<div class="modal-backdrop"><div class="modal">
    <div class="modal-head"><div><div class="modal-title">变更班主任 · 高二(3)班</div><div class="modal-sub">变更后原任职结束，新任职生效；历史记录保留。</div></div><button class="close" onclick="location.href='?view=classes'">×</button></div>
    <div class="modal-body"><div class="form-grid">
      <div class="form-item"><label>当前班主任</label><input value="张老师（T2018）" disabled></div>
      <div class="form-item"><label>新班主任 <span style="color:#d64545">*</span></label><select><option>王老师（T2021）</option><option>刘老师（T2019）</option><option>陈老师（T2022）</option></select></div>
      <div class="form-item"><label>生效日期 <span style="color:#d64545">*</span></label><input value="2026-10-20"></div>
      <div class="form-item"><label>变更原因 <span style="color:#d64545">*</span></label><input value="教学分工调整"></div>
      <div class="form-item full"><label>影响提示</label><div class="note warn">变更只影响后续录入授权与班级查看范围；历史成绩任务的第一责任人快照不自动改变。每班同一时刻只能有一名有效班主任。</div></div>
    </div></div>
    <div class="modal-foot"><button class="btn" onclick="location.href='?view=classes'">取消</button><button class="btn primary">确认变更</button></div>
  </div></div>`;
}

/* ---------------- UI-04 exams ---------------- */
const exams=[
  ['2026-2027-1- MID','2026—2027 高二期中考试','期中','2026-11-11 至 11-12','高二(1)—(6)班','语文/数学/英语/物理/化学/生物','EXAM_OPEN','未发布'],
  ['2026-2027-1- QUIZ','高二数学单元检测','单元','2026-10-18','高二(3)班','数学','EXAM_CLOSED','已发布 v1'],
  ['2026-2027-1- ENG','高二英语听力测试','单元','2026-10-10','高二(1)—(4)班','英语','EXAM_CLOSED','已发布 v2'],
  ['2026-2027-1- PHY','高二物理实验测评','单元','2026-09-28','高二(3)班','物理','EXAM_ARCHIVED','已发布 v1'],
  ['2026-2027-2- MID','2026—2027 高二期末考试','期末','2027-01-15 至 01-16','高二(1)—(6)班','语文/数学/英语/物理/化学/生物','EXAM_DRAFT','未发布']
];
function examsPage(){
  const rows=exams.map(e=>`<tr><td class="table-name">${e[1]}</td><td>${e[2]}</td><td>${e[3]}</td><td class="muted">${e[4]}</td><td class="muted">${e[5]}</td><td>${tag(e[6],e[6]==='EXAM_OPEN'?'blue':e[6]==='EXAM_DRAFT'?'gray':e[6]==='EXAM_ARCHIVED'?'purple':'green')}</td><td>${tag(e[7],e[7]==='未发布'?'gray':'green')}</td><td><button class="btn link" onclick="location.href='?view=exams&modal=create'">配置</button><button class="btn link">详情</button></td></tr>`).join('');
  const content=`${filters(field('学年',select(['2026—2027','2025—2026'],'2026—2027'))+field('考试类型',select(['全部','期中','期末','单元'],'全部'))+field('考试状态',select(['全部','EXAM_DRAFT','EXAM_OPEN','EXAM_CLOSED','EXAM_ARCHIVED'],'全部'))+field('关键词',input('','考试名称')),`<button class="btn soft">${icon('search')} 查询</button><button class="btn">${icon('refresh')} 重置</button>`)}
    <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>考试列表 <span class="card-sub">状态与成绩发布状态分开管理</span></div><button class="btn primary" onclick="location.href='?view=exams&modal=create'">${icon('plus')} 新建考试</button></div>
    <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><colgroup><col style="width:19%"><col style="width:8%"><col style="width:14%"><col style="width:13%"><col style="width:20%"><col style="width:10%"><col style="width:9%"><col style="width:9%"></colgroup><thead><tr><th>考试名称</th><th>类型</th><th>考试时间</th><th>范围</th><th>适用科目</th><th>考试状态</th><th>成绩发布</th><th>操作</th></tr></thead><tbody>${rows}</tbody></table></div>${pager(5)}</div></div>
    <div class="note info" style="margin-top:12px">说明：EXAM_OPEN 表示录入窗口开放，不表示成绩已发布；成绩发布状态由 PUB_UNPUBLISHED / PUB_PUBLISHED / PUB_WITHDRAWN 独立表达。</div>
    ${modal==='create'?examCreateModal():''}`;
  return layout('academic','exams','ACADEMIC / EXAMS','考试管理','配置考试范围、适用科目与分值；考试状态和成绩发布状态分别管理。',`<button class="btn">${icon('refresh')} 刷新</button><button class="btn primary" onclick="location.href='?view=exams&modal=create'">${icon('plus')} 新建考试</button>`,content);
}
function examCreateModal(){
  return `<div class="modal-backdrop"><div class="modal wide">
    <div class="modal-head"><div><div class="modal-title">新建考试</div><div class="modal-sub">考试创建后为 EXAM_DRAFT；配置范围/科目/分值完整后才能开放。</div></div><button class="close" onclick="location.href='?view=exams'">×</button></div>
    <div class="modal-body"><div class="form-grid">
      <div class="form-item"><label>考试名称 <span style="color:#d64545">*</span></label><input value="2026—2027 高二期中考试"></div>
      <div class="form-item"><label>考试类型 <span style="color:#d64545">*</span></label><select><option>期中</option><option>期末</option><option>单元</option></select></div>
      <div class="form-item"><label>学期</label><select><option>2026—2027 第一学期</option><option>2026—2027 第二学期</option></select></div>
      <div class="form-item"><label>考试时间</label><input value="2026-11-11 至 2026-11-12"></div>
      <div class="form-item"><label>参与年级</label><select><option>高二</option><option>高一</option><option>高三</option></select></div>
      <div class="form-item"><label>参与班级</label><input value="高二(1)—(6)班"></div>
      <div class="form-item full"><label>适用科目与分值</label>
        <div class="table-wrap"><table><thead><tr><th>科目</th><th>满分</th><th>及格线</th><th>优秀线</th><th>操作</th></tr></thead><tbody>
        <tr><td>语文</td><td><input class="score-table-input" style="width:70px;height:26px;border:1px solid #dce4ef;border-radius:4px;text-align:center" value="150"></td><td>90</td><td>127.5</td><td><button class="btn link">移除</button></td></tr>
        <tr><td>数学</td><td><input class="score-table-input" style="width:70px;height:26px;border:1px solid #dce4ef;border-radius:4px;text-align:center" value="150"></td><td>90</td><td>127.5</td><td><button class="btn link">移除</button></td></tr>
        <tr><td>英语</td><td><input class="score-table-input" style="width:70px;height:26px;border:1px solid #dce4ef;border-radius:4px;text-align:center" value="150"></td><td>90</td><td>127.5</td><td><button class="btn link">移除</button></td></tr>
        </tbody></table></div><span class="hint">0.5 分步长；优秀线不得低于及格线。已有任务后不得修改范围/满分。</span>
      </div>
      <div class="form-item full"><div class="note">创建后状态：<b>EXAM_DRAFT</b>。需完成范围与科目配置后才能执行“开放考试”；开放后班主任才可录入。</div></div>
    </div></div>
    <div class="modal-foot"><button class="btn" onclick="location.href='?view=exams'">取消</button><button class="btn">保存草稿</button><button class="btn primary">保存并配置</button></div>
  </div></div>`;
}

/* ---------------- UI-05 grades ---------------- */
const gradeStudents=[
  ['20260101','陈星宇','92.5','正常',''],
  ['20260102','李若彤','88.0','正常',''],
  ['20260103','王浩然','缺考','缺考',''],
  ['20260104','赵雨桐','0.0','正常','真实零分'],
  ['20260105','周子轩','86.5','正常',''],
  ['20260106','林可欣','91.0','正常',''],
  ['20260107','孙嘉宁','79.5','正常',''],
  ['20260108','吴思远','84.0','正常','']
];
function gradesPage(){
  const bad=state==='invalid';
  const returned=state==='returned';
  const rows=gradeStudents.map((s,i)=>{
    let val=s[2], err=false, note=s[4];
    if(bad && s[0]==='20260101'){val='105.0';err=true;note='超过满分 100';}
    if(bad && s[0]==='20260105'){val='';note='未录入';}
    const isAbsent=val==='缺考';
    return `<tr><td class="mono">${s[0]}</td><td class="table-name">${s[1]}</td><td>${isAbsent?'缺考':`<input class="score-table-input ${err?'err':''}" value="${val}" ${isAbsent?'disabled':''}>`}${note?`<span class="cell-error" style="${note==='真实零分'?'color:#6b7785':''}">${note}</span>`:''}</td><td>${isAbsent?'<span class="tag orange">缺考</span>':val===''?'<span class="tag red">未录入</span>':val==='0.0'?'<span class="tag green">真实零分</span>':'<span class="tag blue">已录入</span>'}</td><td class="muted small">${s[0]==='20260103'?'不计入分母/排名':s[0]==='20260104'?'参与分母/排名':'—'}</td></tr>`;
  }).join('');
  const banner=bad?`<div class="alert error"><span class="alert-icon">!</span><div><b>提交校验未通过</b>：存在 1 个超满分成绩、1 个未录入学生。请修正后再提交；系统不会把未录入当作 0 分。</div></div>`:
    returned?`<div class="alert warn"><span class="alert-icon">↺</span><div><b>任务已退回</b>：教务退回原因“王浩然缺考状态需补充说明”。任务为 TASK_RETURNED，可修改后重新提交。</div></div>`:
    `<div class="alert info"><span class="alert-icon">i</span><div><b>录入提示</b>：本班全部适用科目由班主任负责；提交前系统检查未录入、分数精度和满分上限。</div></div>`;
  const status=bad?'TASK_DRAFT':returned?'TASK_RETURNED':'TASK_DRAFT';
  const content=`${banner}
    <div class="card" style="margin-bottom:14px"><div class="card-body" style="display:flex;align-items:center;gap:16px;flex-wrap:wrap">
      <div class="field"><label>当前考试</label>${select(['2026—2027 高二期中考试'],'2026—2027 高二期中考试')}</div>
      <div class="field"><label>班级</label>${select(['高二(3)班'],'高二(3)班')}</div>
      <div class="field"><label>科目</label>${select(['数学','语文','英语','物理','化学','生物'],'数学')}</div>
      <div class="field"><label>任务状态</label><div style="height:32px;display:flex;align-items:center">${tag(status,status==='TASK_RETURNED'?'orange':'blue')}</div></div>
      <div class="spacer"></div>
      <div class="note" style="min-width:310px;padding:8px 11px">满分 100 · 及格 60 · 优秀 85 · 0.5 分步长</div>
    </div></div>
    <div class="split">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>学生成绩录入 <span class="card-sub">高二(3)班 · 数学</span></div><div class="rowline"><span class="pill">已录入 ${bad?'7':'8'} / 8</span><span class="pill">缺考 1</span><span class="pill">未录入 ${bad?'1':'0'}</span></div></div>
        <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table class="score-table"><thead><tr><th style="width:18%">学号</th><th style="width:18%">姓名</th><th style="width:28%">分数（0.5 步长）</th><th style="width:18%">状态</th><th style="width:18%">统计口径</th></tr></thead><tbody>${rows}</tbody></table></div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:14px"><div class="muted small">最后暂存：2026-10-29 09:42 · 操作人：张老师</div><div class="rowline"><button class="btn">暂存草稿</button><button class="btn primary" ${bad?'disabled':''}>${bad?'提交审核（校验未通过）':'提交审核'}</button></div></div>
        </div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>提交前检查</div><span class="tag ${bad?'red':returned?'orange':'green'}">${bad?'有错误':returned?'已退回':'可提交'}</span></div>
        <div class="side-info"><div class="info-list">
          <div class="info-row"><span>任务编号</span><span class="mono">TASK-20261111-0301</span></div>
          <div class="info-row"><span>提交人</span><span>张老师（班主任）</span></div>
          <div class="info-row"><span>未录入</span><span style="color:${bad?'#c43c3c':'#13864a'}">${bad?'1 人':'0 人'}</span></div>
          <div class="info-row"><span>超满分</span><span style="color:${bad?'#c43c3c':'#13864a'}">${bad?'1 人':'0 人'}</span></div>
          <div class="info-row"><span>缺考</span><span>1 人 · 不计分母</span></div>
          <div class="info-row"><span>真实零分</span><span>1 人 · 参与统计</span></div>
        </div>
        <div class="note ${bad?'danger':'info'}" style="margin-top:14px">${bad?'提交按钮应被阻止；未录入不得自动转为 0 分，缺考不得显示为 0。':'提交后任务变为 TASK_SUBMITTED 并锁定；如需修改须由教务退回。'}</div>
        <div class="note warn" style="margin-top:10px">缺考、未录入、真实零分三态必须分开；0.5 分步长；不得录入 0.3 或负分。</div>
        </div></div>
    </div>`;
  return layout('head','grades','HEAD TEACHER / SCORE ENTRY','成绩录入与提交','班主任维护本班全部适用科目；支持 0.5 分步长、缺考与未录入三态。',`<button class="btn">${icon('history')} 退回记录</button><button class="btn primary" ${bad?'disabled':''}>${icon('check')} ${bad?'保存并提交（需修正）':'保存并提交'}</button>`,content);
}

/* ---------------- UI-06 review ---------------- */
const reviewTasks=[
  ['TASK-20261111-0301','高二(3)班','数学','张老师','TASK_SUBMITTED','92.5','8/8'],
  ['TASK-20261111-0302','高二(3)班','语文','张老师','TASK_APPROVED','91.2','8/8'],
  ['TASK-20261111-0303','高二(3)班','英语','张老师','TASK_SUBMITTED','89.4','8/8'],
  ['TASK-20261111-0304','高二(3)班','物理','张老师','TASK_RETURNED','—','7/8'],
  ['TASK-20261111-0305','高二(3)班','化学','张老师','TASK_SUBMITTED','86.0','8/8'],
  ['TASK-20261111-0306','高二(3)班','生物','张老师','TASK_APPROVED','84.8','8/8']
];
function reviewPage(){
  const rows=reviewTasks.map((t,i)=>`<tr><td class="mono">${t[0]}</td><td>${t[1]}</td><td>${t[2]}</td><td>${t[3]}</td><td>${tag(t[4],t[4]==='TASK_APPROVED'?'green':t[4]==='TASK_RETURNED'?'orange':'blue')}</td><td>${t[5]}</td><td><button class="btn link" onclick="location.href='?view=review&modal=${i===0?'return':'publish'}'">查看</button></td></tr>`).join('');
  const content=`${filters(field('考试',select(['2026—2027 高二期中考试'],'2026—2027 高二期中考试'))+field('班级',select(['高二(3)班','高二(1)班'],'高二(3)班'))+field('任务状态',select(['全部','TASK_SUBMITTED','TASK_APPROVED','TASK_RETURNED'],'全部')),`<button class="btn soft">${icon('search')} 查询</button><button class="btn">${icon('refresh')} 重置</button>`)}
    <div class="split">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>待审核任务 <span class="card-sub">共 6 个任务 · 3 个待审核</span></div><div class="rowline"><button class="btn">批量通过</button><button class="btn" onclick="location.href='?view=review&modal=return'">退回</button><button class="btn primary" onclick="location.href='?view=review&modal=publish'">发布该班</button></div></div>
        <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><colgroup><col style="width:17%"><col style="width:12%"><col style="width:9%"><col style="width:11%"><col style="width:16%"><col style="width:9%"><col style="width:11%"></colgroup><thead><tr><th>任务编号</th><th>班级</th><th>科目</th><th>提交人</th><th>任务状态</th><th>均分</th><th>操作</th></tr></thead><tbody>${rows}</tbody></table></div>
        <div class="note info" style="margin-top:12px">审核通过只把任务变为 TASK_APPROVED，不等于 PUB_PUBLISHED。发布是班级级动作，须所有适用任务通过。</div>
        </div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>审核详情 · 数学</div><span class="tag blue">TASK_SUBMITTED</span></div>
        <div class="side-info"><div class="detail-title">高二(3)班 · 数学 · 张老师提交</div><div class="detail-sub">提交时间 2026-10-29 09:42 · 版本 1</div>
          <div class="table-wrap"><table><thead><tr><th>学生</th><th>分数</th><th>状态</th></tr></thead><tbody><tr><td>陈星宇</td><td>92.5</td><td>正常</td></tr><tr><td>李若彤</td><td>88.0</td><td>正常</td></tr><tr><td>王浩然</td><td>缺考</td><td>缺考</td></tr><tr><td>赵雨桐</td><td>0.0</td><td>真实零分</td></tr></tbody></table></div>
          <div class="info-list" style="margin-top:13px"><div class="info-row"><span>完整性</span><span>8/8 · 无未录入</span></div><div class="info-row"><span>职责分离</span><span>审核人 ≠ 提交人</span></div><div class="info-row"><span>发布状态</span><span>PUB_UNPUBLISHED</span></div></div>
          <div class="rowline" style="margin-top:14px"><button class="btn danger" onclick="location.href='?view=review&modal=return'">退回</button><button class="btn success">审核通过</button></div>
        </div></div>
    </div>${modal==='return'?returnModal():''}${modal==='publish'?publishModal():''}${modal==='withdraw'?withdrawModal():''}`;
  return layout('academic','review','ACADEMIC / REVIEW & PUBLISH','成绩审核与发布','逐任务审核、退回、按考试×班级发布；发布版本与撤回/更正留痕。',`<button class="btn" onclick="location.href='?view=review&modal=withdraw'">${icon('history')} 发布记录</button><button class="btn primary" onclick="location.href='?view=review&modal=publish'">按班发布</button>`,content);
}
function returnModal(){
  return `<div class="modal-backdrop"><div class="modal">
    <div class="modal-head"><div><div class="modal-title">审核退回 · 高二(3)班数学</div><div class="modal-sub">任务将变为 TASK_RETURNED；班主任修改后重新提交。</div></div><button class="close" onclick="location.href='?view=review'">×</button></div>
    <div class="modal-body"><div class="form-grid">
      <div class="form-item full"><label>退回原因 <span style="color:#d64545">*</span></label><textarea>王浩然缺考状态需补充说明；请核对后重新提交。</textarea><span class="hint">原因必填；退回不改成绩，只改变任务状态并写审计。</span></div>
      <div class="form-item full"><label>退回范围</label><div class="note warn">仅退回本任务（高二(3)班 / 数学），不自动退回其他科目。班主任重新提交后需再次审核，不能跳过 TASK_SUBMITTED。</div></div>
    </div></div>
    <div class="modal-foot"><button class="btn" onclick="location.href='?view=review'">取消</button><button class="btn danger">确认退回</button></div>
  </div></div>`;
}
function publishModal(){
  return `<div class="modal-backdrop"><div class="modal">
    <div class="modal-head"><div><div class="modal-title">发布班级成绩 · 高二(3)班</div><div class="modal-sub">发布后学生可见；生成版本 1；发布不等于审核通过。</div></div><button class="close" onclick="location.href='?view=review'">×</button></div>
    <div class="modal-body"><div class="note info" style="margin-bottom:12px"><b>发布前检查</b>：该班 6 个适用科目任务均须为 TASK_APPROVED，且无 SCORE_NOT_ENTERED。<br><span class="muted">演示数据说明：此处为“审核完成后的发布快照”，与 UI-06 审核中的列表不是同一时点。</span></div>
      <div class="table-wrap"><table><thead><tr><th>科目</th><th>任务状态</th><th>未录入</th><th>检查</th></tr></thead><tbody>
        <tr><td>语文</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr><tr><td>数学</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr><tr><td>英语</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr><tr><td>物理</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr><tr><td>化学</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr><tr><td>生物</td><td>${tag('TASK_APPROVED','green')}</td><td>0</td><td>通过</td></tr>
      </tbody></table></div>
      <div class="form-grid" style="margin-top:13px"><div class="form-item"><label>发布版本</label><input value="v1" disabled></div><div class="form-item"><label>发布范围</label><input value="高二(3)班 · 全部适用科目" disabled></div></div>
      <div class="note warn" style="margin-top:12px">发布后如发现错误：撤回后学生不可见；更正生成新版本，不直接覆盖历史。所有发布/撤回/更正写业务审计。</div>
    </div>
    <div class="modal-foot"><button class="btn" onclick="location.href='?view=review'">取消</button><button class="btn primary">确认发布 v1</button></div>
  </div></div>`;
}

function withdrawModal(){
  return `<div class="modal-backdrop"><div class="modal wide">
    <div class="modal-head"><div><div class="modal-title">发布记录与受控更正 · 高二(3)班语文</div><div class="modal-sub">PUB_PUBLISHED · 当前版本 v1 · 发布后修改必须留痕</div></div><button class="close" onclick="location.href='?view=review'">×</button></div>
    <div class="modal-body">
      <div class="grid-2">
        <div><div class="card-title" style="font-size:11.5px;margin-bottom:8px"><span class="accent" style="height:12px"></span>发布版本</div>
          <div class="table-wrap"><table><thead><tr><th>版本</th><th>时间</th><th>操作</th><th>说明</th></tr></thead><tbody>
            <tr><td class="mono">v1</td><td>2026-11-13 09:20</td><td>李晓</td><td>首次发布</td></tr>
            <tr><td class="mono">v2</td><td>2026-11-13 10:05</td><td>李晓</td><td>受控更正 · 92.0 → 93.0</td></tr>
          </tbody></table></div>
          <div class="note warn" style="margin-top:10px">撤回后学生不可见；重新发布要求全部任务仍为 TASK_APPROVED，版本 +1。更正保持 PUB_PUBLISHED，生成新不可变版本，不覆盖旧版本。</div>
        </div>
        <div><div class="card-title" style="font-size:11.5px;margin-bottom:8px"><span class="accent" style="height:12px"></span>处理方式</div>
          <div class="form-item"><label>操作</label><select><option>受控更正（推荐）</option><option>撤回发布</option></select></div>
          <div class="form-item" style="margin-top:10px"><label>原因 <span style="color:#d64545">*</span></label><textarea>复核成立：第 14 题步骤分补 1.0 分，更正后重算总分/排名。</textarea></div>
          <div class="form-grid" style="margin-top:10px"><div class="form-item"><label>原值</label><input value="92.0" disabled></div><div class="form-item"><label>新值</label><input value="93.0"></div></div>
          <div class="note" style="margin-top:10px">操作人：李晓（教务管理员） · 时间：2026-11-13 10:05 · 审计：业务变更记录 / 发布记录</div>
        </div>
      </div>
    </div>
    <div class="modal-foot"><button class="btn danger">撤回发布</button><button class="btn">取消</button><button class="btn primary">保存更正并生成 v2</button></div>
  </div></div>`;
}

/* ---------------- UI-07 query ---------------- */
const subjectScores=[['语文','92.5','150','正常'],['数学','88.0','150','正常'],['英语','91.0','150','正常'],['物理','84.5','100','正常'],['化学','86.0','100','正常'],['生物','79.0','100','正常']];
function queryPage(){
  const unpublished=state==='unpublished';
  const rows=subjectScores.map(s=>`<tr><td class="table-name">${s[0]}</td><td style="font-size:15px;font-weight:700;color:#23344d">${s[1]}</td><td class="muted">${s[2]}</td><td>${s[3]==='正常'?tag('正常','green'):tag(s[3],'orange')}</td><td class="muted">—</td></tr>`).join('');
  const content=unpublished?`
    <div class="card"><div class="card-body"><div class="empty"><div class="empty-mark">◷</div><div><b style="color:#526277">该考试成绩尚未发布</b><div class="small" style="margin-top:7px">高二(3)班 · 2026—2027 高二期中考试<br>发布后才能查看各科成绩；已撤回成绩不会显示旧值。</div></div><button class="btn soft" style="margin-top:4px">返回已发布考试</button></div></div></div>
    <div class="note info" style="margin-top:12px">学生只能查询本人已发布成绩；未发布、已撤回、未录入成绩均不可见。</div>`:`
    <div class="card" style="margin-bottom:14px"><div class="card-body"><div class="rowline" style="justify-content:space-between"><div class="profile-line"><div class="avatar-big">陈</div><div><h3>陈星宇</h3><p>学号 20260101 · 高二(3)班 · 学生</p></div></div><div class="rowline"><span class="tag green">PUB_PUBLISHED</span><span class="pill">发布版本 v1</span><span class="pill">2026-11-13 发布</span></div></div></div></div>
    <div class="split">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>2026—2027 高二期中考试</div><div class="seg"><span>全部考试</span><span class="active">期中</span><span>单元</span></div></div>
        <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>科目</th><th>成绩</th><th>满分</th><th>状态</th><th>备注</th></tr></thead><tbody>${rows}</tbody></table></div>
        <div class="note info" style="margin-top:12px">只展示本人数据；不显示班级平均、他人排名或年级排名。缺考不显示 0；未录入不参与总分。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>本次成绩摘要</div></div><div class="side-info">
        <div class="stat-card" style="border:0;padding:0;min-height:auto"><div class="stat-label">总分（完整）</div><div class="stat-value">521.0<small>分</small></div><div class="stat-foot">6 科完整 · 含真实零分/缺考规则</div></div>
        <div class="info-list" style="margin-top:14px"><div class="info-row"><span>班级排名</span><span>第 3 名 / 42 人</span></div><div class="info-row"><span>总分排名规则</span><span>完整总分 · 1,2,2,4</span></div><div class="info-row"><span>缺考科目</span><span>无</span></div><div class="info-row"><span>发布状态</span><span>已发布 v1</span></div></div>
        <div class="note" style="margin-top:13px">本页只读；如需复核请使用 P1 成绩复核申请（规划功能）。</div>
      </div></div>
    </div>`;
  return layout('student','query','STUDENT / MY SCORES','本人成绩查询','仅展示本人已发布成绩、总分和班级排名；不展示其他学生数据。',`<button class="btn">${icon('refresh')} 刷新</button>`,content);
}

/* ---------------- UI-08 stats / audit ---------------- */
function statsPage(){
  if(forcedRole==='academic') return academicStatsPage();
  const bars=[['语文','91.2',91],['数学','86.4',86],['英语','90.1',90],['物理','82.7',83],['化学','84.3',84],['生物','80.8',81]];
  const content=`${filters(field('考试',select(['2026—2027 高二期中考试'],'2026—2027 高二期中考试'))+field('班级',select(['高二(3)班'],'高二(3)班'))+field('统计口径',select(['单科 + 班级总分','单科'],'单科 + 班级总分')),`<button class="btn soft">${icon('search')} 生成统计</button><button class="btn">${icon('download')} 导出</button>`)}
    <div class="grid-4" style="margin-top:14px"><div class="stat-card"><div class="stat-label">有效人数</div><div class="stat-value">41<small>人</small></div><div class="stat-foot">缺考 1 人单列，不进入分母</div></div><div class="stat-card"><div class="stat-label">班级总分均分</div><div class="stat-value">512.6<small>分</small></div><div class="stat-foot">仅完整总分学生参与</div></div><div class="stat-card"><div class="stat-label">最高总分</div><div class="stat-value">579.0<small>分</small></div><div class="stat-foot">竞争排名 1,2,2,4</div></div><div class="stat-card"><div class="stat-label">总分完整率</div><div class="stat-value">97.6<small>%</small></div><div class="stat-foot">1 人缺考 · 0 人未录入</div></div></div>
    <div class="grid-2" style="margin-top:14px">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>学科平均分</div><div class="legend"><span><i></i>平均分</span><span><i style="background:#9fb7e8"></i>及格率</span></div></div><div class="card-body"><div class="chart">${bars.map(b=>`<div class="bar-wrap"><div class="bar" style="height:${b[2]}%"><span class="bar-value">${b[1]}</span></div><div class="bar alt" style="height:${b[2]-8}%"></div><div class="bar-label">${b[0]}</div></div>`).join('')}</div><div class="note" style="margin-top:12px">同口径才比较；不同满分用得分率，不直接混算。缺考/未录入不进入有效人数。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>科目统计明细</div><span class="tag blue">本班</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>科目</th><th>有效人数</th><th>平均分</th><th>及格率</th><th>最高</th></tr></thead><tbody>
        <tr><td>语文</td><td>41</td><td>91.2</td><td>97.6%</td><td>126.0</td></tr><tr><td>数学</td><td>41</td><td>86.4</td><td>92.7%</td><td>121.5</td></tr><tr><td>英语</td><td>41</td><td>90.1</td><td>95.1%</td><td>124.0</td></tr><tr><td>物理</td><td>40</td><td>82.7</td><td>90.0%</td><td>96.0</td></tr><tr><td>化学</td><td>41</td><td>84.3</td><td>92.7%</td><td>95.5</td></tr><tr><td>生物</td><td>41</td><td>80.8</td><td>87.8%</td><td>94.0</td></tr>
      </tbody></table></div><div class="note" style="margin-top:12px">班主任只查看本班汇总；不展示其他班级/学生个人成绩。</div></div></div>
    </div>`;
  return layout('head','stats','HEAD TEACHER / ANALYTICS','本班成绩统计','本班单场考试、单科与总分汇总；缺考/未录入单列，完整总分才参与排名。',`<button class="btn">${icon('history')} 口径说明</button>`,content);
}
function academicStatsPage(){
  const bars=[['高二(1)班','512.4',91],['高二(2)班','508.6',89],['高二(3)班','512.6',90],['高二(4)班','503.2',87],['高二(5)班','499.8',85],['高二(6)班','507.1',88]];
  const content=`${filters(field('考试',select(['2026—2027 高二期中考试'],'2026—2027 高二期中考试'))+field('年级',select(['高二','高一','高三'],'高二'))+field('班级',select(['全部班级','高二(1)班','高二(2)班'],'全部班级')),`<button class="btn soft">${icon('search')} 生成统计</button><button class="btn">${icon('download')} 导出</button>`)}
    <div class="grid-4" style="margin-top:14px"><div class="stat-card"><div class="stat-label">参考班级</div><div class="stat-value">42<small>个</small></div><div class="stat-foot">教务授权范围：全校</div></div><div class="stat-card"><div class="stat-label">有效学生</div><div class="stat-value">1,680<small>人</small></div><div class="stat-foot">缺考/未录入单列</div></div><div class="stat-card"><div class="stat-label">全校总分均分</div><div class="stat-value">508.7<small>分</small></div><div class="stat-foot">仅完整总分参与</div></div><div class="stat-card"><div class="stat-label">总分完整率</div><div class="stat-value">96.8<small>%</small></div><div class="stat-foot">竞争排名 1,2,2,4</div></div></div>
    <div class="grid-2" style="margin-top:14px">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>班级总分均分</div><div class="legend"><span><i></i>班级均分</span><span><i style="background:#9fb7e8"></i>完整率</span></div></div><div class="card-body"><div class="chart">${bars.map(b=>`<div class="bar-wrap"><div class="bar" style="height:${b[2]}%"><span class="bar-value">${b[1]}</span></div><div class="bar alt" style="height:${b[2]-10}%"></div><div class="bar-label">${b[0].replace('高二','')}</div></div>`).join('')}</div><div class="note" style="margin-top:12px">仅同一考试、同口径；教务可看全校，班主任只看本班；不做班级好坏评价。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>班级汇总</div><span class="tag blue">全校</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>班级</th><th>有效人数</th><th>均分</th><th>完整率</th><th>最高</th></tr></thead><tbody>
        <tr><td>高二(1)班</td><td>40</td><td>512.4</td><td>100%</td><td>579.0</td></tr><tr><td>高二(2)班</td><td>40</td><td>508.6</td><td>97.5%</td><td>571.5</td></tr><tr><td>高二(3)班</td><td>41</td><td>512.6</td><td>97.6%</td><td>579.0</td></tr><tr><td>高二(4)班</td><td>39</td><td>503.2</td><td>95.0%</td><td>566.0</td></tr><tr><td>高二(5)班</td><td>40</td><td>499.8</td><td>96.8%</td><td>560.5</td></tr><tr><td>高二(6)班</td><td>40</td><td>507.1</td><td>94.6%</td><td>568.0</td></tr>
      </tbody></table></div><div class="note" style="margin-top:12px">统计口径：有效人数只含 SCORE_NORMAL；缺考/未录入单列；完整总分参与 1,2,2,4 排名。</div></div></div>
    </div>`;
  return layout('academic','stats','ACADEMIC / SCHOOL ANALYTICS','全校成绩统计','教务授权范围内的全校/多班级统计；不评价班级好坏，不混算不同口径。',`<button class="btn">${icon('history')} 口径说明</button>`,content);
}

const auditRows=[
  ['2026-10-29 09:42','成绩提交','高二(3)班 / 数学','TASK_DRAFT','TASK_SUBMITTED','张老师','班主任','正常提交'],
  ['2026-10-29 10:05','审核通过','高二(3)班 / 语文','TASK_SUBMITTED','TASK_APPROVED','李晓','教务','职责分离校验'],
  ['2026-10-29 10:18','发布','高二(3)班 / 语文','PUB_UNPUBLISHED','PUB_PUBLISHED（v1）','李晓','教务','班级发布'],
  ['2026-10-29 11:20','撤回','高二(3)班 / 英语','PUB_PUBLISHED（v1）','PUB_WITHDRAWN','李晓','教务','发现录入错误'],
  ['2026-10-29 11:32','受控更正','高二(3)班 / 英语 / 陈星宇','91.0','93.0（v2）','李晓','教务','复核成立，重算排名'],
  ['2026-10-29 11:32','复核更正','REV-20261029-003','REV_PROCESSING','REV_CORRECTED','李晓','教务','关联成绩版本 v2']
];
function auditPage(){
  const sys=forcedRole==='sys';
  const rows=auditRows.map(a=>sys?`<tr><td class="mono">${a[0]}</td><td>${a[1]}</td><td class="muted">${a[2].replace(' / 陈星宇',' · 已脱敏')}</td><td>${a[5]}</td><td>${a[6]}</td><td>${tag('成功','green')}</td><td class="muted">成绩字段已脱敏</td></tr>`:`<tr><td class="mono">${a[0]}</td><td>${a[1]}</td><td>${a[2]}</td><td class="mono">${a[3]}</td><td class="mono">${a[4]}</td><td>${a[5]}</td><td>${a[6]}</td><td class="muted">${a[7]}</td></tr>`).join('');
  const content=`${filters(field('时间范围',input('2026-10-01 至 2026-10-31'))+field('事件类型',select(['全部','成绩提交','审核通过','发布','撤回','受控更正'],'全部'))+field('对象',input('','班级 / 学生 / 任务')),`<button class="btn soft">${icon('search')} 查询</button><button class="btn">${icon('refresh')} 重置</button>`)}
    ${sys?`<div class="alert warn"><span class="alert-icon">!</span><div><b>系统管理员脱敏视图</b>：仅显示系统/安全审计元数据；不显示成绩修改前后值，不能展开或导出成绩明细。系统管理权限不等于教务审批权。</div></div>`:`<div class="alert info"><span class="alert-icon">i</span><div><b>业务审计</b>：教务可查看成绩/任务/发布/更正的前后值、原因、操作人和版本；审计记录不可修改或删除。</div></div>`}
    <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>${sys?'审计摘要（脱敏）':'变更记录'}<span class="card-sub">${sys?'仅元数据':'共 6 条演示记录'}</span></div><div class="rowline"><span class="tag ${sys?'orange':'blue'}">${sys?'SCOPE_SYSTEM · 脱敏':'SCOPE_ALL · 业务审计'}</span><button class="btn" ${sys?'disabled':''}>导出</button></div></div>
    <div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><colgroup>${sys?'<col style="width:15%"><col style="width:10%"><col style="width:23%"><col style="width:14%"><col style="width:10%"><col style="width:8%"><col style="width:20%">':'<col style="width:14%"><col style="width:10%"><col style="width:20%"><col style="width:11%"><col style="width:11%"><col style="width:8%"><col style="width:8%"><col style="width:18%">'}</colgroup><thead><tr>${sys?'<th>时间</th><th>事件</th><th>对象（脱敏）</th><th>操作人</th><th>角色</th><th>结果</th><th>说明</th>':'<th>时间</th><th>事件</th><th>对象</th><th>变更前</th><th>变更后</th><th>操作人</th><th>角色</th><th>原因/说明</th>'}</tr></thead><tbody>${rows}</tbody></table></div>${pager(6)}</div></div>
    ${sys?'<div class="note p1" style="margin-top:12px">系统安全日志与成绩业务审计分开：本页不展示成绩业务前后值，避免系统管理员越权查看成绩明细。</div>':''}`;
  return layout(sys?'sysadmin':'academic','audit',sys?'SYSTEM / SECURITY AUDIT':'ACADEMIC / BUSINESS AUDIT',sys?'审计摘要（脱敏）':'业务变更记录',sys?'系统管理员只查看系统/安全审计元数据；不展示成绩修改前后值。':'查看成绩、任务、发布与更正事件；前后值、原因和操作人可追踪。',`<button class="btn" ${sys?'disabled':''}>${icon('download')} 导出</button>`,content);
}

/* ---------------- P1 pages ---------------- */
function reviewApplyPage(){
  const content=`<div class="grid-2">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>可申请复核的已发布成绩</div><span class="tag green">PUB_PUBLISHED</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>考试</th><th>科目</th><th>成绩</th><th>版本</th><th>操作</th></tr></thead><tbody>
        <tr><td>2026—2027 高二期中考试</td><td>数学</td><td><b>88.0</b></td><td>v1</td><td><button class="btn link">选择</button></td></tr>
        <tr><td>2026—2027 高二期中考试</td><td>物理</td><td><b>84.5</b></td><td>v1</td><td><button class="btn link">选择</button></td></tr>
        <tr><td>2026—2027 高二期中考试</td><td>英语</td><td><b>91.0</b></td><td>v1</td><td><button class="btn link">选择</button></td></tr>
      </tbody></table></div><div class="note" style="margin-top:12px">仅本人已发布成绩可申请；同一学生+考试+科目最多 1 个活跃申请。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>提交复核申请</div>${p1()}</div><div class="side-info"><div class="form-item"><label>考试</label><select><option>2026—2027 高二期中考试</option></select></div><div class="form-item" style="margin-top:11px"><label>科目</label><select><option>数学</option><option>物理</option><option>英语</option></select></div><div class="form-item" style="margin-top:11px"><label>申请理由 <span style="color:#d64545">*</span></label><textarea>第 14 题步骤分疑似漏加，申请核查。</textarea></div><div class="form-item" style="margin-top:11px"><label>联系方式</label><input value="138****2210"></div><button class="btn primary" style="width:100%;justify-content:center;margin-top:14px">提交复核申请</button><div class="note p1" style="margin-top:12px">提交后由本班班主任受理/核查，再由教务最终确认；P1 不改变 P0 发布状态机。</div></div></div>
    </div>
    <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>我的复核申请</div><span class="pill p1">P1</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>申请编号</th><th>考试/科目</th><th>提交时间</th><th>状态</th><th>处理结果</th></tr></thead><tbody>
      <tr><td class="mono">REV-20261029-003</td><td>高二期中 / 数学</td><td>2026-10-29 14:10</td><td>${tag('REV_PROCESSING','orange')}</td><td class="muted">班主任核查中</td></tr>
      <tr><td class="mono">REV-20261028-001</td><td>高二期中 / 英语</td><td>2026-10-28 16:20</td><td>${tag('REV_NO_CHANGE','green')}</td><td>无需更正 · 已确认</td></tr>
    </tbody></table></div></div></div>`;
  return layout('student','review-apply','P1 / STUDENT REVIEW','成绩复核申请','P1 规划功能：学生仅对本人已发布成绩提交复核申请。',`<span class="hero-badge">P1 · 规划功能</span>`,content,{p1:true});
}
function reviewProcessPage(){
  const content=`<div class="alert info"><span class="alert-icon">P1</span><div><b>P1 规划功能</b>：班主任只受理/核查本班申请并提交教务，不直接修改已发布成绩；本页不代表后端已完成。</div></div>
    <div class="split">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>本班待受理复核</div><span class="tag orange">2 条待处理</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>申请编号</th><th>学生</th><th>考试/科目</th><th>状态</th><th>提交</th></tr></thead><tbody>
        <tr><td class="mono">REV-20261029-003</td><td>陈星宇</td><td>高二期中 / 数学</td><td>${tag('REV_PENDING','orange')}</td><td>10-29 14:10</td></tr>
        <tr><td class="mono">REV-20261029-004</td><td>李若彤</td><td>高二期中 / 物理</td><td>${tag('REV_PENDING','orange')}</td><td>10-29 14:26</td></tr>
        <tr><td class="mono">REV-20261028-001</td><td>陈星宇</td><td>高二期中 / 英语</td><td>${tag('REV_NO_CHANGE','green')}</td><td>10-28 16:20</td></tr>
      </tbody></table></div><div class="note" style="margin-top:12px">班主任只能看到本班申请；受理时记录 handler_id，核查意见不可省略。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>核查详情 · 陈星宇</div><span class="tag purple">P1</span></div><div class="side-info">
        <div class="detail-title">REV-20261029-003 · 数学</div><div class="detail-sub">成绩 88.0 / 满分 150 · 发布版本 v1</div>
        <div class="note" style="margin-bottom:12px">学生理由：第 14 题步骤分疑似漏加，申请核查。</div>
        <div class="form-item"><label>核查意见 <span style="color:#d64545">*</span></label><select><option>无需修改</option><option>建议更正</option><option>建议驳回</option></select></div>
        <div class="form-item" style="margin-top:11px"><label>核查说明</label><textarea>已核对原始评分记录，第 14 题步骤分应补 2.0 分，建议更正。</textarea></div>
        <div class="note warn" style="margin-top:11px">班主任不直接改分；提交后由教务最终确认/更正。更正需原因/前后值/版本+1/审计。</div>
        <button class="btn primary" style="width:100%;justify-content:center;margin-top:13px">提交教务确认</button>
      </div></div>
    </div>`;
  return layout('head','review-process','P1 / HEAD TEACHER REVIEW','复核受理与核查','P1 规划功能：班主任受理本班申请并提交核查意见，不直接改分。',`<span class="hero-badge">P1 · 规划功能</span>`,content,{p1:true});
}
function reviewFinalPage(){
  const content=`<div class="alert info"><span class="alert-icon">P1</span><div><b>P1 规划功能</b>：教务最终确认；需要更正时复用受控更正能力，生成新版本并写审计。P1 不进入 P0 退出条件。</div></div>
    <div class="grid-2">
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>待最终确认</div><span class="tag orange">REV_PROCESSING</span></div><div class="card-body" style="padding-top:10px"><div class="table-wrap"><table><thead><tr><th>申请</th><th>学生/科目</th><th>班主任意见</th><th>状态</th></tr></thead><tbody>
        <tr><td class="mono">REV-20261029-003</td><td>陈星宇 / 数学</td><td>建议更正 +2.0</td><td>${tag('REV_PROCESSING','orange')}</td></tr>
        <tr><td class="mono">REV-20261029-004</td><td>李若彤 / 物理</td><td>无需修改</td><td>${tag('REV_PROCESSING','orange')}</td></tr>
      </tbody></table></div><div class="note" style="margin-top:12px">最终确认只能由教务执行；班主任不参与最终改分。</div></div></div>
      <div class="card"><div class="card-head"><div class="card-title"><span class="accent"></span>最终确认 · REV-20261029-003</div><span class="pill p1">P1</span></div><div class="side-info">
        <div class="info-list"><div class="info-row"><span>申请人</span><span>陈星宇 · 高二(3)班</span></div><div class="info-row"><span>成绩记录</span><span>数学 88.0 · v1</span></div><div class="info-row"><span>班主任核查</span><span>建议更正 +2.0</span></div></div>
        <div class="form-item" style="margin-top:12px"><label>最终结论</label><select><option>需要更正</option><option>无需更正</option><option>驳回</option></select></div>
        <div class="form-item" style="margin-top:11px"><label>更正后分数</label><input value="90.0"></div>
        <div class="form-item" style="margin-top:11px"><label>结论/更正原因</label><textarea>复核成立：第 14 题步骤分补 2.0 分，生成成绩版本 v2。</textarea></div>
        <div class="note warn" style="margin-top:11px">保持 PUB_PUBLISHED；生成新不可变版本，保留原值/新值/原因/操作人/时间；重算总分/排名；写审计。</div>
        <button class="btn primary" style="width:100%;justify-content:center;margin-top:13px">确认并保存</button>
      </div></div>
    </div>`;
  return layout('academic','review-final','P1 / ACADEMIC CONFIRM','复核最终确认','P1 规划功能：教务最终确认、驳回或受控更正；更正生成新版本并留痕。',`<span class="hero-badge">P1 · 规划功能</span>`,content,{p1:true});
}

/* ---------------- render ---------------- */
function render(){
  switch(view){
    case 'login': return loginPage();
    case 'students': return studentsPage();
    case 'classes': return classesPage();
    case 'exams': return examsPage();
    case 'grades': return gradesPage();
    case 'review': return reviewPage();
    case 'query': return queryPage();
    case 'stats': return statsPage();
    case 'audit': return auditPage();
    case 'review-apply': return reviewApplyPage();
    case 'review-process': return reviewProcessPage();
    case 'review-final': return reviewFinalPage();
    default: return loginPage();
  }
}
document.getElementById('app').innerHTML=render();
