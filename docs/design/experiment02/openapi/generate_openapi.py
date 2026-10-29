#!/usr/bin/env python3
"""Generate OpenAPI 3.0.3 drafts from api_catalog.py. No HTTP/DB calls."""
from pathlib import Path
import yaml
from api_catalog import P0, P1

OUT=Path(__file__).resolve().parent

def schemas():
    S={}
    def obj(name, desc, props, required=()):
        S[name]={'type':'object','description':desc,'properties':props}
        if required: S[name]['required']=list(required)
    def ref(n): return {'$ref':'#/components/schemas/'+n}
    S['AjaxResult']={'type':'object','required':['code','msg'],'properties':{
        'code':{'type':'integer','example':200},'msg':{'type':'string','example':'操作成功'},
        'data':{'type':'object','nullable':True,'additionalProperties':True}}}
    S['TableDataInfo']={'type':'object','required':['total','rows','code','msg'],'properties':{
        'total':{'type':'integer','format':'int64'},'rows':{'type':'array','items':{'type':'object','additionalProperties':True}},
        'code':{'type':'integer'},'msg':{'type':'string'}}}
    S['ErrorData']={'type':'object','properties':{
        'errorCode':{'type':'string','example':'SCORE_VERSION_CONFLICT'},'details':{'type':'object','additionalProperties':True},
        'traceId':{'type':'string'}}}
    obj('PageQuery','分页与排序',{
        'pageNum':{'type':'integer','minimum':1,'default':1},'pageSize':{'type':'integer','minimum':1,'maximum':100,'default':10},
        'orderByColumn':{'type':'string','description':'白名单字段'},'isAsc':{'type':'string','enum':['asc','desc']}})
    obj('YearUpsert','学年新增/修改',{
        'yearCode':{'type':'string','maxLength':16},'yearName':{'type':'string','maxLength':32},
        'startDate':{'type':'string','format':'date'},'endDate':{'type':'string','format':'date'},
        'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer','minimum':0}},['yearCode','yearName','startDate','endDate'])
    obj('YearCurrentRequest','设为当前学年',{'version':{'type':'integer','minimum':0}},['version'])
    obj('Year','学年',{'id':{'type':'integer'},'yearCode':{'type':'string'},'yearName':{'type':'string'},
        'startDate':{'type':'string','format':'date'},'endDate':{'type':'string','format':'date'},
        'isCurrent':{'type':'boolean'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('TermUpsert','学期新增/修改',{
        'yearId':{'type':'integer'},'termNo':{'type':'integer','minimum':1},'termName':{'type':'string','maxLength':32},
        'startDate':{'type':'string','format':'date'},'endDate':{'type':'string','format':'date'},
        'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer'}},['yearId','termNo','termName','startDate','endDate'])
    obj('Term','学期',{'id':{'type':'integer'},'yearId':{'type':'integer'},'termNo':{'type':'integer'},'termName':{'type':'string'},
        'startDate':{'type':'string','format':'date'},'endDate':{'type':'string','format':'date'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('GradeUpsert','年级新增/修改',{'gradeCode':{'type':'string','maxLength':16},'gradeName':{'type':'string','maxLength':32},
        'sortOrder':{'type':'integer'},'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer'}},['gradeCode','gradeName'])
    obj('Grade','年级',{'id':{'type':'integer'},'gradeCode':{'type':'string'},'gradeName':{'type':'string'},'sortOrder':{'type':'integer'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('ClassUpsert','班级新增/修改',{'yearId':{'type':'integer'},'gradeId':{'type':'integer'},'classCode':{'type':'string','maxLength':32},
        'className':{'type':'string','maxLength':64},'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer'}},['yearId','gradeId','classCode','className'])
    obj('Class','班级',{'id':{'type':'integer'},'yearId':{'type':'integer'},'gradeId':{'type':'integer'},'classCode':{'type':'string'},'className':{'type':'string'},
        'rosterVersion':{'type':'integer'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('AssignmentCreate','班主任指派',{'headTeacherId':{'type':'integer'},'effectiveFrom':{'type':'string','format':'date'},
        'reason':{'type':'string','maxLength':255},'version':{'type':'integer'}} ,['headTeacherId','effectiveFrom'])
    obj('TeacherAssignmentCreate','按班主任指派',{'classId':{'type':'integer'},'effectiveFrom':{'type':'string','format':'date'},
        'reason':{'type':'string','maxLength':255}},['classId','effectiveFrom'])
    obj('EndAssignmentRequest','结束任职',{'effectiveTo':{'type':'string','format':'date'},'reason':{'type':'string','maxLength':255},'version':{'type':'integer'}},['effectiveTo','version'])
    obj('Assignment','班主任任职',{'id':{'type':'integer'},'headTeacherId':{'type':'integer'},'classId':{'type':'integer'},
        'effectiveFrom':{'type':'string','format':'date'},'effectiveTo':{'type':'string','format':'date','nullable':True},
        'isCurrent':{'type':'boolean'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('StudentUpsert','学生档案新增/修改',{'studentNo':{'type':'string','maxLength':32},'studentName':{'type':'string','maxLength':64},
        'gender':{'type':'string','enum':['M','F','U']},'enrollmentDate':{'type':'string','format':'date','nullable':True},
        'status':{'type':'string','enum':['ACTIVE','INACTIVE','GRADUATED']},'version':{'type':'integer'}},['studentNo','studentName'])
    obj('Student','学生档案',{'id':{'type':'integer'},'studentNo':{'type':'string'},'studentName':{'type':'string'},'gender':{'type':'string'},
        'enrollmentDate':{'type':'string','format':'date','nullable':True},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('StatusRequest','状态变更',{'status':{'type':'string'},'reason':{'type':'string','maxLength':255},'version':{'type':'integer'}},['status','version'])
    obj('ClassChangeRequest','转班/归属变更',{'classId':{'type':'integer'},'effectiveFrom':{'type':'string','format':'date'},
        'reason':{'type':'string','maxLength':255},'version':{'type':'integer'}},['classId','effectiveFrom','version'])
    obj('StudentClass','学生班级归属',{'id':{'type':'integer'},'studentId':{'type':'integer'},'classId':{'type':'integer'},
        'effectiveFrom':{'type':'string','format':'date'},'effectiveTo':{'type':'string','format':'date','nullable':True},
        'isCurrent':{'type':'boolean'},'reason':{'type':'string','nullable':True},'version':{'type':'integer'}})
    obj('BindingCreate','账号档案绑定',{'userId':{'type':'integer','format':'int64','description':'sys_user.user_id BIGINT'},
        'profileType':{'type':'string','enum':['STUDENT','HEAD_TEACHER']},'studentId':{'type':'integer','nullable':True},
        'headTeacherId':{'type':'integer','nullable':True}},['userId','profileType'])
    obj('Binding','账号档案绑定',{'id':{'type':'integer'},'userId':{'type':'integer','format':'int64'},'profileType':{'type':'string'},
        'studentId':{'type':'integer','nullable':True},'headTeacherId':{'type':'integer','nullable':True},
        'status':{'type':'string','enum':['ACTIVE','REVOKED']},'effectiveFrom':{'type':'string','format':'date-time'},'effectiveTo':{'type':'string','format':'date-time','nullable':True},'version':{'type':'integer'}})
    obj('RevokeRequest','撤销绑定',{'reason':{'type':'string','maxLength':255},'version':{'type':'integer'}},['version'])
    obj('Identity','当前业务身份',{'userId':{'type':'integer','format':'int64'},'userName':{'type':'string'},
        'roles':{'type':'array','items':{'type':'string'}},'scope':{'type':'string','enum':['SCOPE_SYSTEM','SCOPE_ALL','SCOPE_CLASS','SCOPE_SELF']},
        'studentId':{'type':'integer','nullable':True},'headTeacherId':{'type':'integer','nullable':True},
        'bound':{'type':'boolean'},'permissions':{'type':'array','items':{'type':'string'}}})
    obj('HeadTeacherUpsert','班主任档案',{'teacherNo':{'type':'string','maxLength':32},'teacherName':{'type':'string','maxLength':64},
        'gender':{'type':'string','enum':['M','F','U']},'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer'}},['teacherNo','teacherName'])
    obj('HeadTeacher','班主任',{'id':{'type':'integer'},'teacherNo':{'type':'string'},'teacherName':{'type':'string'},'gender':{'type':'string'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('SubjectUpsert','科目',{'subjectCode':{'type':'string','maxLength':16},'subjectName':{'type':'string','maxLength':32},
        'sortOrder':{'type':'integer'},'status':{'type':'string','enum':['ACTIVE','INACTIVE']},'version':{'type':'integer'}},['subjectCode','subjectName'])
    obj('Subject','科目',{'id':{'type':'integer'},'subjectCode':{'type':'string'},'subjectName':{'type':'string'},'sortOrder':{'type':'integer'},'status':{'type':'string'},'version':{'type':'integer'}})
    obj('ExamUpsert','考试',{'yearId':{'type':'integer'},'termId':{'type':'integer'},'examCode':{'type':'string','maxLength':32},
        'examName':{'type':'string','maxLength':128},'examType':{'type':'string','maxLength':32},'examDate':{'type':'string','format':'date','nullable':True},
        'version':{'type':'integer'}},['yearId','termId','examCode','examName'])
    obj('ExamStatusRequest','考试状态转换',{'targetStatus':{'type':'string','enum':['EXAM_OPEN','EXAM_CLOSED','EXAM_ARCHIVED']},'version':{'type':'integer'}},['targetStatus','version'])
    obj('Exam','考试',{'id':{'type':'integer'},'yearId':{'type':'integer'},'termId':{'type':'integer'},'examCode':{'type':'string'},'examName':{'type':'string'},
        'status':{'type':'string','enum':['EXAM_DRAFT','EXAM_OPEN','EXAM_CLOSED','EXAM_ARCHIVED']},'version':{'type':'integer'}})
    obj('ExamClassSet','考试班级范围',{'classIds':{'type':'array','items':{'type':'integer'}},'version':{'type':'integer'}},['classIds','version'])
    obj('ExamSubjectItem','考试科目项',{'subjectId':{'type':'integer'},'fullMark':{'type':'number','multipleOf':0.5},'passLine':{'type':'number','multipleOf':0.5},'excellentLine':{'type':'number','multipleOf':0.5}},['subjectId','fullMark','passLine','excellentLine'])
    obj('ExamSubjectSet','考试科目集合',{'subjects':{'type':'array','items':ref('ExamSubjectItem')},'version':{'type':'integer'}},['subjects','version'])
    obj('ExamDetail','考试详情',{'exam':ref('Exam'),'classes':{'type':'array','items':ref('Class')},'subjects':{'type':'array','items':ref('ExamSubjectItem')}})
    obj('GenerateTasksRequest','生成成绩任务',{'classIds':{'type':'array','items':{'type':'integer'}},'subjectIds':{'type':'array','items':{'type':'integer'}},'idempotencyKey':{'type':'string'}})
    obj('GenerateTasksResult','任务生成结果',{'created':{'type':'integer'},'skipped':{'type':'integer'},'tasks':{'type':'array','items':ref('ScoreTask')}})
    obj('ScoreTask','成绩任务',{'id':{'type':'integer'},'examId':{'type':'integer'},'classId':{'type':'integer'},'subjectId':{'type':'integer'},
        'status':{'type':'string','enum':['TASK_DRAFT','TASK_SUBMITTED','TASK_APPROVED','TASK_RETURNED']},
        'firstOwnerTeacherId':{'type':'integer'},'rosterVersion':{'type':'integer'},'version':{'type':'integer'}})
    obj('ScoreItem','成绩项',{'studentId':{'type':'integer'},'scoreStatus':{'type':'string','enum':['SCORE_NORMAL','SCORE_ABSENT','SCORE_NOT_ENTERED']},
        'scoreValue':{'type':'number','multipleOf':0.5,'nullable':True},'version':{'type':'integer'}},['studentId','scoreStatus'])
    obj('ScoreBatchRequest','批量保存成绩',{'version':{'type':'integer'},'items':{'type':'array','items':ref('ScoreItem')},'idempotencyKey':{'type':'string'}},['version','items'])
    obj('ScoreEntry','成绩记录',{'id':{'type':'integer'},'taskId':{'type':'integer'},'studentId':{'type':'integer'},'scoreStatus':{'type':'string'},
        'scoreValue':{'type':'number','nullable':True},'version':{'type':'integer'}})
    obj('ScoreSaveResult','保存结果',{'saved':{'type':'integer'},'notEntered':{'type':'integer'},'absent':{'type':'integer'},'normal':{'type':'integer'},'version':{'type':'integer'}})
    obj('VersionRequest','版本请求',{'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['version'])
    obj('TaskProgress','任务进度',{'task':ref('ScoreTask'),'total':{'type':'integer'},'normal':{'type':'integer'},'absent':{'type':'integer'},'notEntered':{'type':'integer'}})
    obj('ReviewRequest','审核任务',{'action':{'type':'string','enum':['APPROVE','RETURN']},'reason':{'type':'string','maxLength':500},
        'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['action','version'])
    obj('PublishRequest','发布请求',{'examId':{'type':'integer'},'classId':{'type':'integer'},'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['examId','classId','version'])
    obj('WithdrawRequest','撤回请求',{'reason':{'type':'string','maxLength':500},'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['reason','version'])
    obj('RepublishRequest','重新发布',{'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['version'])
    obj('CorrectionRequest','受控更正',{'reason':{'type':'string','maxLength':500},'items':{'type':'array','items':ref('ScoreItem')},
        'version':{'type':'integer'},'idempotencyKey':{'type':'string'}},['reason','items','version'])
    obj('Publication','班级发布',{'id':{'type':'integer'},'examId':{'type':'integer'},'classId':{'type':'integer'},
        'status':{'type':'string','enum':['PUB_UNPUBLISHED','PUB_PUBLISHED','PUB_WITHDRAWN']},
        'currentVersionId':{'type':'integer','nullable':True},'version':{'type':'integer'}})
    obj('PublicationVersion','发布版本',{'id':{'type':'integer'},'publicationId':{'type':'integer'},'versionNo':{'type':'integer'},
        'snapshotAt':{'type':'string','format':'date-time'},'changeReason':{'type':'string','nullable':True},'snapshotHash':{'type':'string','nullable':True}})
    obj('MyScoreDetail','本人成绩',{'examId':{'type':'integer'},'examName':{'type':'string'},'versionNo':{'type':'integer'},
        'totalScore':{'type':'number','nullable':True},'rank':{'type':'integer','nullable':True},'classSize':{'type':'integer','nullable':True},
        'subjects':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('ClassStat','班级统计',{'classId':{'type':'integer'},'examId':{'type':'integer'},'subjects':{'type':'array','items':{'type':'object','additionalProperties':True}},
        'students':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('SchoolStat','全校统计',{'examId':{'type':'integer'},'classes':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('AuditRecord','业务审计',{'id':{'type':'integer'},'eventType':{'type':'string'},'objectType':{'type':'string'},'objectId':{'type':'integer'},
        'objectVersion':{'type':'integer','nullable':True},'reason':{'type':'string','nullable':True},'operatorUserId':{'type':'integer','format':'int64'},
        'operatorRole':{'type':'string'},'occurredAt':{'type':'string','format':'date-time'},'source':{'type':'string'}})
    obj('BinaryFile','二进制文件',{'fileName':{'type':'string'},'contentType':{'type':'string'},'size':{'type':'integer'}})
    # P1 schemas (also harmless in P0 components; P1 paths are separate)
    obj('ReviewApplyRequest','P1 复核申请',{'examId':{'type':'integer'},'subjectId':{'type':'integer'},'reason':{'type':'string','maxLength':500},'idempotencyKey':{'type':'string'}},['examId','subjectId','reason'])
    obj('ReviewHandleRequest','P1 受理/驳回',{'action':{'type':'string'},'comment':{'type':'string','maxLength':500},'version':{'type':'integer'}},['action','comment','version'])
    obj('ReviewConfirmRequest','P1 教务确认',{'action':{'type':'string'},'comment':{'type':'string','maxLength':500},'version':{'type':'integer'}},['action','version'])
    obj('ImportRequest','P1 导入请求',{'taskId':{'type':'integer'},'fileName':{'type':'string'},'fileContent':{'type':'string','format':'byte'},'idempotencyKey':{'type':'string'}},['taskId','fileName'])
    obj('ExportRequest','P1 导出请求',{'examId':{'type':'integer'},'classId':{'type':'integer'},'subjectId':{'type':'integer','nullable':True},'idempotencyKey':{'type':'string'}})
    obj('Review','P1 复核申请',{'id':{'type':'integer'},'studentId':{'type':'integer'},'examId':{'type':'integer'},'subjectId':{'type':'integer'},'status':{'type':'string'},'resultComment':{'type':'string','nullable':True},'deadlineAt':{'type':'string','format':'date-time'}})
    obj('ImportPreview','P1 导入预检',{'total':{'type':'integer'},'valid':{'type':'integer'},'invalid':{'type':'integer'},'errors':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('ImportResult','P1 导入结果',{'batchId':{'type':'integer'},'total':{'type':'integer'},'success':{'type':'integer'},'failed':{'type':'integer'}})
    obj('PersonTrend','P1 个人趋势',{'subjects':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('ClassTrend','P1 班级趋势',{'classId':{'type':'integer'},'subjects':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    obj('ClassCompare','P1 班级对比',{'examId':{'type':'integer'},'classes':{'type':'array','items':{'type':'object','additionalProperties':True}}})
    return S

def op(a):
    params=[]; seen=set()
    import re
    for p in re.findall(r'\{([^}]+)\}',a['path']):
        if p not in seen:
            seen.add(p); params.append({'name':p,'in':'path','required':True,'schema':{'type':'integer' if p!='versionNo' else 'integer'}})
    if a['paged']:
        for n,desc in [('pageNum','页码'),('pageSize','每页条数')]:
            if n not in seen: params.append({'name':n,'in':'query','required':False,'schema':{'type':'integer'},'description':desc}); seen.add(n)
    for n in a['query']:
        if n not in seen:
            typ='integer' if n in ('yearId','termId','gradeId','classId','examId','subjectId','taskId') else 'string'
            params.append({'name':n,'in':'query','required':False,'schema':{'type':typ}}); seen.add(n)
    responses={
        '200':{'description':'成功','content':{'application/json':{'schema':{'$ref':'#/components/schemas/'+('TableDataInfo' if a['success']=='TableDataInfo' else 'AjaxResult')}}}},
        '400':{'description':'参数/业务校验失败','content':{'application/json':{'schema':{'$ref':'#/components/schemas/AjaxResult'}}}},
        '401':{'description':'未登录/Token 失效','content':{'application/json':{'schema':{'$ref':'#/components/schemas/AjaxResult'}}}},
        '403':{'description':'无权限/数据范围拒绝','content':{'application/json':{'schema':{'$ref':'#/components/schemas/AjaxResult'}}}},
        '404':{'description':'资源不存在','content':{'application/json':{'schema':{'$ref':'#/components/schemas/AjaxResult'}}}},
        '409':{'description':'状态/版本/重复冲突','content':{'application/json':{'schema':{'$ref':'#/components/schemas/AjaxResult'}}}},
    }
    o={'tags':[a['tag']],'summary':a['name'],'description':a['id']+'；状态：规划（未实现）。',
       'operationId':'op_'+a['id'].lower().replace('-','_'),'parameters':params,
       'responses':responses,
       'x-api-id':a['id'],'x-status':a['status'],'x-fr':a['fr'],'x-br':a['br'],'x-ac':a['ac'],
       'x-permission':a['perm'],'x-roles':a['roles'],'x-scope':a['scope'],'x-tables':a['tables'],
       'x-controller':a['controller'],'x-service':a['service'],'x-frontend':a['frontend'],
       'x-idempotency':a['idem'],'x-version':a['version'],'x-transaction':a['tx']}
    if a['body']:
        o['requestBody']={'required':True,'content':{'application/json':{'schema':{'$ref':'#/components/schemas/'+a['body']}}}}
    if a['success'] not in ('AjaxResult','TableDataInfo'):
        o['x-data-schema']='#/components/schemas/'+a['success']
    return o

def build(apis, title, desc):
    paths={}
    for a in apis.values():
        p=a['path']; paths.setdefault(p,{})[a['method'].lower()]=op(a)
    return {'openapi':'3.0.3','info':{'title':title,'version':'0.1-draft','description':desc},
            'servers':[{'url':'http://localhost:8080','description':'本地开发（不是部署地址）'}],
            'paths':paths,
            'components':{'schemas':schemas(),'securitySchemes':{'bearerAuth':{'type':'http','scheme':'bearer','bearerFormat':'JWT'}}},
            'security':[{'bearerAuth':[]}]}

p0=build(P0,'学生管理系统 P0 业务 API（设计草稿）','设计草稿；未实现、未部署；68 个 P0 API。')
p1=build(P1,'学生管理系统 P1 扩展 API（设计草稿）','设计草稿；P1 独立，不进入 P0。')
(OUT/'p0-business-api.yaml').write_text(yaml.safe_dump(p0,allow_unicode=True,sort_keys=False),encoding='utf-8')
(OUT/'p1-extension-api.yaml').write_text(yaml.safe_dump(p1,allow_unicode=True,sort_keys=False),encoding='utf-8')
print('P0 ops',len(P0),'P1 ops',len(P1),'paths P0',len(p0['paths']),'P1',len(p1['paths']))
