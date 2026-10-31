#!/usr/bin/env python3
"""Build v1.1 DOCX/PDF from Markdown with LibreOffice UNO. No system installs."""
import os, re, sys, time, subprocess, signal
from pathlib import Path
import uno
from com.sun.star.beans import PropertyValue
from com.sun.star.text.ControlCharacter import PARAGRAPH_BREAK, LINE_BREAK
from com.sun.star.style.BreakType import PAGE_BEFORE
from com.sun.star.text.TextContentAnchorType import AS_CHARACTER
from com.sun.star.style.LineSpacingMode import PROP

ROOT=Path(__file__).resolve().parents[1]
MD=ROOT/'output/学生管理系统概要设计说明书-v1.1-review.md'
OUT=ROOT/'output'
PORT=2006
SOFFICE_PROFILE='file:///tmp/lo-sdd-v11'

def pv(name,value):
    p=PropertyValue(); p.Name=name; p.Value=value; return p

def start():
    proc=subprocess.Popen(['soffice',f'-env:UserInstallation={SOFFICE_PROFILE}','--headless','--norestore','--nofirststartwizard',
        f'--accept=socket,host=127.0.0.1,port={PORT};urp;StarOffice.ServiceManager'],
        stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    local=uno.getComponentContext(); resolver=local.ServiceManager.createInstanceWithContext('com.sun.star.bridge.UnoUrlResolver',local)
    ctx=None
    for _ in range(40):
        try:
            ctx=resolver.resolve(f'uno:socket,host=127.0.0.1,port={PORT};urp;StarOffice.ComponentContext'); break
        except Exception: time.sleep(.4)
    if not ctx: raise RuntimeError('UNO connect failed')
    smgr=ctx.ServiceManager; desktop=smgr.createInstanceWithContext('com.sun.star.frame.Desktop',ctx)
    return proc, desktop

def set_font(obj,name='SimSun',size=12,west='Times New Roman',bold=False):
    obj.setPropertyValue('CharFontName',west)
    obj.setPropertyValue('CharFontNameAsian',name)
    obj.setPropertyValue('CharHeight',float(size))
    obj.setPropertyValue('CharHeightAsian',float(size))
    obj.setPropertyValue('CharWeight',150.0 if bold else 100.0)
    obj.setPropertyValue('CharWeightAsian',150.0 if bold else 100.0)

def set_para(obj,size=12,indent=True,align=0,before=0,after=0,line=150):
    set_font(obj,'SimSun',size)
    obj.setPropertyValue('ParaFirstLineIndent',850 if indent else 0)
    obj.setPropertyValue('ParaTopMargin',int(before*100))
    obj.setPropertyValue('ParaBottomMargin',int(after*100))
    obj.setPropertyValue('ParaAdjust',align)
    ls=uno.createUnoStruct('com.sun.star.style.LineSpacing'); ls.Mode=PROP; ls.Height=line
    obj.setPropertyValue('ParaLineSpacing',ls)

def page_style(cursor,name):
    # Writer paragraph page-style property is PageDescName (PageStyleName is read-only on cursor).
    cursor.setPropertyValue('PageDescName',name)

def make_page_style(doc,name,landscape=False,header=True,footer=True):
    fam=doc.getStyleFamilies().getByName('PageStyles')
    try:
        st=fam.getByName(name)
    except Exception:
        st=doc.createInstance('com.sun.star.style.PageStyle'); fam.insertByName(name,st)
    st.IsLandscape=landscape
    st.Width=29700 if landscape else 21000
    st.Height=21000 if landscape else 29700
    st.LeftMargin=2500; st.RightMargin=2500; st.TopMargin=(1300 if landscape else 2500); st.BottomMargin=(1300 if landscape else 2500)
    st.HeaderIsOn=header; st.FooterIsOn=footer
    st.HeaderHeight=800; st.HeaderBodyDistance=300; st.FooterHeight=700; st.FooterBodyDistance=300
    if header:
        h=st.HeaderText; h.setString('')
        c=h.createTextCursor(); h.insertString(c,'高中学生信息与成绩管理系统  概要设计说明书',False)
        c.setPropertyValue('CharFontName','Times New Roman'); c.setPropertyValue('CharFontNameAsian','SimSun')
        c.setPropertyValue('CharHeight',9.0); c.setPropertyValue('CharHeightAsian',9.0); c.setPropertyValue('ParaAdjust',3)
    if footer:
        f=st.FooterText; f.setString('')
        c=f.createTextCursor(); f.insertString(c,'第 ',False)
        fld=doc.createInstance('com.sun.star.text.TextField.PageNumber'); fld.NumberingType=4
        f.insertTextContent(c,fld,False); f.insertString(c,' 页',False)
        c.setPropertyValue('CharFontName','Times New Roman'); c.setPropertyValue('CharFontNameAsian','SimSun')
        c.setPropertyValue('CharHeight',9.0); c.setPropertyValue('CharHeightAsian',9.0); c.setPropertyValue('ParaAdjust',3)
    return st

def add_break(cursor):
    cursor.BreakType=PAGE_BEFORE

def add_text(doc,cursor,s,style=None,size=12,indent=True,align=0,bold=False,keep=False):
    cursor.BreakType=0
    text=doc.getText(); text.insertString(cursor,s,False)
    if style: cursor.ParaStyleName=style
    set_para(cursor,size=size,indent=indent,align=align)
    if bold: set_font(cursor,'SimHei',size,bold=True)
    if keep:
        try: cursor.setPropertyValue('ParaKeepTogether',True)
        except Exception: pass
    text.insertControlCharacter(cursor,PARAGRAPH_BREAK,False)

def add_heading(doc,cursor,level,heading,page_desc=None):
    text=doc.getText(); text.insertString(cursor,heading,False)
    style=f'Heading {min(level,3)}'; cursor.ParaStyleName=style
    if page_desc:
        cursor.setPropertyValue('PageDescName',page_desc)
    size=16 if level==1 else (14 if level==2 else 12)
    set_font(cursor,'SimHei',size,bold=True)
    cursor.ParaFirstLineIndent=0
    cursor.ParaTopMargin=400 if level==1 else 250
    cursor.ParaBottomMargin=200
    try: cursor.setPropertyValue('ParaKeepTogether',True)
    except Exception: pass
    text.insertControlCharacter(cursor,PARAGRAPH_BREAK,False)
    cursor.BreakType=0

def add_table(doc,cursor,rows):
    if not rows: return
    cols=max(len(r) for r in rows)
    tbl=doc.createInstance('com.sun.star.text.TextTable'); tbl.initialize(len(rows),cols)
    doc.getText().insertTextContent(cursor,tbl,False)
    # width
    tbl.IsWidthRelative=False; tbl.Width=16000; tbl.Split=True
    # proportional column widths using TextTable separators
    try:
        import uno as _uno
        lens=[max(len(r[j]) if j<len(r) else 0 for r in rows) for j in range(cols)]
        total=sum(max(1,x) for x in lens) or 1
        widths=[max(1200,int(16000*max(1,lens[j])/total)) for j in range(cols)]
        # normalize
        s=sum(widths); widths=[int(w*16000/s) for w in widths]
        seps=tbl.TableColumnSeparators
        pos=0
        for j in range(cols-1):
            pos+=widths[j]
            seps[j].Position=pos
        tbl.TableColumnSeparators=seps
    except Exception as e: print('column separators failed',e)
    for i,row in enumerate(rows):
        for j in range(cols):
            cell=tbl.getCellByPosition(j,i)
            cell.setString(row[j] if j<len(row) else '')
            c=cell.createTextCursor(); c.gotoEnd(False)
            set_font(c,'SimSun',8.5,bold=(i==0))
            c.ParaFirstLineIndent=0; c.ParaAdjust=0
            cell.setPropertyValue('BackColor',0xEAF2FF if i==0 else 0xFFFFFF)
    try:
        tbl.setPropertyValue('TableBorder',1)
    except Exception: pass
    # paragraph after table
    p=doc.getText().createTextCursor(); p.gotoEnd(False); doc.getText().insertControlCharacter(p,PARAGRAPH_BREAK,False); p.BreakType=0

def add_image(doc,cursor,path,width_cm,landscape=False,caption='',new_page=False):
    url=uno.systemPathToFileUrl(str(path.resolve()))
    g=doc.createInstance('com.sun.star.text.TextGraphicObject')
    g.AnchorType=AS_CHARACTER
    g.GraphicURL=url
    try:
        import struct
        with open(path,'rb') as fh:
            b=fh.read(24)
        sw,sh=struct.unpack('>II',b[16:24])
    except Exception:
        sw,sh=1600,900
    w=int(width_cm*1000); h=int(w*sh/sw)
    if landscape and h>15000:
        h=15000; w=int(h*sw/sh)
    cursor.BreakType=PAGE_BEFORE if new_page else 0
    doc.getText().insertTextContent(cursor,g,False)
    if landscape:
        cursor.setPropertyValue('PageDescName','Landscape')
    g.Width=w; g.Height=h
    # image and caption in one keep-together paragraph
    text=doc.getText()
    text.insertControlCharacter(cursor,LINE_BREAK,False)
    if caption:
        text.insertString(cursor,caption,False)
        set_font(cursor,'SimSun',10.5)
        cursor.ParaAdjust=3
    cursor.ParaFirstLineIndent=0; cursor.ParaTopMargin=0; cursor.ParaBottomMargin=120
    from com.sun.star.style.LineSpacingMode import PROP as LSPROP
    ls=uno.createUnoStruct('com.sun.star.style.LineSpacing'); ls.Mode=LSPROP; ls.Height=100
    cursor.ParaLineSpacing=ls
    try: cursor.setPropertyValue('ParaKeepTogether',True)
    except Exception: pass
    text.insertControlCharacter(cursor,PARAGRAPH_BREAK,False)
    cursor.BreakType=0

def parse_md(text):
    lines=text.splitlines(); blocks=[]; i=0
    while i<len(lines):
        l=lines[i]
        if not l.strip() or l.strip() in ('---','***','___'): i+=1; continue
        if l.startswith('```'):
            i+=1; code=[]
            while i<len(lines) and not lines[i].startswith('```'):
                code.append(lines[i]); i+=1
            i+=1; blocks.append(('code','\n'.join(code))); continue
        m=re.match(r'^(#{1,4})\s+(.*)$',l)
        if m:
            blocks.append(('heading',len(m.group(1)),m.group(2).strip())); i+=1; continue
        m=re.match(r'^!\[[^\]]*\]\(([^)]+)\)\s*$',l.strip())
        if m:
            blocks.append(('image',m.group(1))); i+=1; continue
        if l.strip().startswith('|'):
            rows=[]
            while i<len(lines) and lines[i].strip().startswith('|'):
                r=lines[i].strip().strip('|').split('|')
                rows.append([x.strip() for x in r]); i+=1
            # remove separator row
            rows=[r for r in rows if not all(re.match(r'^:?-+:?$',x) for x in r)]
            blocks.append(('table',rows)); continue
        if re.match(r'^[-*]\s+',l):
            blocks.append(('bullet',re.sub(r'^[-*]\s+','',l).strip())); i+=1; continue
        if re.match(r'^\d+\.\s+',l):
            blocks.append(('num',l.strip())); i+=1; continue
        if l.startswith('>'):
            blocks.append(('quote',l.lstrip('> ').strip())); i+=1; continue
        # paragraph: accumulate until blank/heading/table/list/code/image
        para=[l.strip()]
        i+=1
        while i<len(lines) and lines[i].strip() and not re.match(r'^(#{1,4})\s|^\||^!\[|^```|^[-*]\s|^\d+\.\s|^>',lines[i].strip()):
            para.append(lines[i].strip()); i+=1
        blocks.append(('para',' '.join(para)))
    return blocks

def clean_inline(s):
    s=re.sub(r'\*\*(.*?)\*\*',r'\1',s)
    s=re.sub(r'`(.*?)`',r'\1',s)
    return s

def build():
    proc,desktop=start()
    doc=desktop.loadComponentFromURL('private:factory/swriter','_blank',0,())
    # page styles
    make_page_style(doc,'Cover',header=False,footer=False)
    make_page_style(doc,'Body',header=True,footer=True)
    make_page_style(doc,'Landscape',landscape=True,header=True,footer=True)
    text=doc.getText(); cursor=text.createTextCursor()
    # default body style
    page_style(cursor,'Cover')
    # cover
    cover=[('电子科技大学',22,True),('软件工程',16,True),('实验二  概要设计',18,True),('',10,False),
           ('高中学生信息与成绩管理系统',20,True),('概要设计说明书',24,True),('',12,False),
           ('版本：V1.1-review',12,False),('日期：2026-10-29',12,False),('',12,False),
           ('小组成员 / 学号 / 班级：待填写',12,False),('',6,False),
           ('（评审稿，未获得正式批准）',11,False)]
    for s,sz,b in cover:
        text.insertString(cursor,s,False); set_font(cursor,'SimHei' if b else 'SimSun',sz,bold=b)
        cursor.ParaAdjust=3; cursor.ParaFirstLineIndent=0
        cursor.ParaTopMargin=150 if s!='' else 100
        text.insertControlCharacter(cursor,PARAGRAPH_BREAK,False)
    # front matter
    cursor.BreakType=PAGE_BEFORE; page_style(cursor,'Body')
    md=MD.read_text(encoding='utf-8')
    # only after title, before chapter1? parse all; skip H1 title and the duplicate cover metadata until 文档信息
    blocks=parse_md(md)
    # insert front from 文档信息/修订历史; then TOC; then chapters
    started=False; toc_done=False
    current_landscape=False
    skip_idx=0
    for bidx,b in enumerate(blocks):
        if skip_idx and bidx < skip_idx: continue
        if b[0]=='heading' and b[2].startswith('文档信息'):
            started=True
        if b[0]=='heading' and b[2].startswith('1. 导言') and not toc_done:
            # TOC on its own page
            cursor.BreakType=PAGE_BEFORE; page_style(cursor,'Body')
            tocidx=doc.createInstance('com.sun.star.text.ContentIndex'); tocidx.Title='目录'; tocidx.Level=3; tocidx.CreateFromOutline=True
            text.insertTextContent(cursor,tocidx,False)
            cursor.BreakType=PAGE_BEFORE; page_style(cursor,'Body')
            toc_done=True
        if not started:
            continue
        if b[0]=='heading':
            lvl=b[1]
            # determine if the next block is a landscape figure
            next_land=False
            if bidx+1 < len(blocks) and blocks[bidx+1][0]=='image':
                nm=Path(blocks[bidx+1][1]).name
                next_land=any(x in nm for x in ['FIG-D1','FIG-D2','FIG-D9','FIG-SDD-ER'])
            desc=None
            if next_land:
                if not current_landscape:
                    cursor.BreakType=PAGE_BEFORE; desc='Landscape'; current_landscape=True
                elif lvl==2:
                    cursor.BreakType=PAGE_BEFORE
            else:
                if current_landscape:
                    cursor.BreakType=PAGE_BEFORE; desc='Body'; current_landscape=False
                elif lvl==2 and re.match(r'^\d+\.', b[2]):
                    cursor.BreakType=PAGE_BEFORE
            add_heading(doc,cursor,max(1,min(3,lvl-1)),b[2], desc)
        elif b[0]=='para':
            if b[1].startswith('**图 '):
                add_text(doc,cursor,clean_inline(b[1]),size=10.5,indent=False,align=3)
            else:
                add_text(doc,cursor,clean_inline(b[1]),size=12)
        elif b[0]=='bullet':
            add_text(doc,cursor,'• '+clean_inline(b[1]),size=12,indent=False)
        elif b[0]=='num':
            add_text(doc,cursor,clean_inline(b[1]),size=12,indent=False)
        elif b[0]=='quote':
            add_text(doc,cursor,clean_inline(b[1]),size=11,indent=False)
        elif b[0]=='code':
            for cl in b[1].splitlines():
                add_text(doc,cursor,cl,size=9,indent=False)
        elif b[0]=='table':
            add_table(doc,cursor,b[1])
        elif b[0]=='image':
            if skip_idx and bidx < skip_idx: continue
            path=(MD.parent/b[1]).resolve() if not Path(b[1]).is_absolute() else Path(b[1])
            name=path.name
            landscape=any(x in name for x in ['FIG-D1','FIG-D2','FIG-D9','FIG-SDD-ER'])
            width=24.0 if landscape else 15.5
            new_page = landscape and not current_landscape
            if new_page:
                current_landscape=True
            caption=''
            if bidx+1 < len(blocks) and blocks[bidx+1][0]=='para' and blocks[bidx+1][1].startswith('**图 '):
                caption=clean_inline(blocks[bidx+1][1]); skip_idx=bidx+2
            add_image(doc,cursor,path,width,landscape,caption,new_page)
    # update TOC / pagination
    try: doc.calculateAll()
    except Exception: pass
    doc.refresh()
    try:
        idxs=doc.getDocumentIndexes()
        for i in range(idxs.getCount()): idxs.getByIndex(i).update()
    except Exception as e: print('toc update',e)
    doc.refresh()
    # export
    docx=OUT/'学生管理系统概要设计说明书-v1.1-review.docx'
    pdf=OUT/'学生管理系统概要设计说明书-v1.1-review.pdf'
    doc.storeToURL('file://'+str(docx),(pv('FilterName','Office Open XML Text'),))
    doc.storeToURL('file://'+str(pdf),(pv('FilterName','writer_pdf_Export'),))
    doc.close(True)
    try: proc.terminate(); proc.wait(timeout=5)
    except Exception: proc.kill()
    print('exported',docx,pdf)

if __name__=='__main__':
    build()
