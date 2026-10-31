#!/usr/bin/env python3
import zipfile, re, shutil
from pathlib import Path
p=Path('output/学生管理系统概要设计说明书-v1.1-review.docx')
tmp=p.with_suffix('.tmp.docx')
with zipfile.ZipFile(p,'r') as zin:
    items=[(i,zin.read(i.filename)) for i in zin.infolist()]
new=[]
count=0
for info,data in items:
    if info.filename=='word/document.xml':
        s=data.decode('utf-8')
        def add_header(m):
            global count
            tbl=m.group(0)
            if '<w:tblHeader' in tbl: return tbl
            # first row within this table
            trm=re.search(r'<w:tr(?:\s[^>]*)?>.*?</w:tr>',tbl,re.S)
            if not trm: return tbl
            tr=trm.group(0)
            if '<w:trPr>' in tr:
                tr2=tr.replace('<w:trPr>','<w:trPr><w:tblHeader/>',1)
            else:
                tr2=tr.replace('>','><w:trPr><w:tblHeader/></w:trPr>',1)
            count+=1
            return tbl.replace(tr,tr2,1)
        s=re.sub(r'<w:tbl>.*?</w:tbl>',add_header,s,flags=re.S)
        data=s.encode('utf-8')
    new.append((info,data))
with zipfile.ZipFile(tmp,'w',zipfile.ZIP_DEFLATED) as zout:
    for info,data in new:
        zout.writestr(info,data)
shutil.move(tmp,p)
print('tables header-marked:',count)
