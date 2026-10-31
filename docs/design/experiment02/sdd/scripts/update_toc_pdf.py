#!/usr/bin/env python3
import time, subprocess
from pathlib import Path
import uno
from com.sun.star.beans import PropertyValue
ROOT=Path(__file__).resolve().parents[1]
DOCX=ROOT/'output/学生管理系统概要设计说明书-v1.1-review.docx'
PDF=ROOT/'output/学生管理系统概要设计说明书-v1.1-review.pdf'
PORT=2007
proc=subprocess.Popen(['soffice','-env:UserInstallation=file:///tmp/lo-sdd-update','--headless','--norestore','--nofirststartwizard',f'--accept=socket,host=127.0.0.1,port={PORT};urp;StarOffice.ServiceManager'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
local=uno.getComponentContext(); resolver=local.ServiceManager.createInstanceWithContext('com.sun.star.bridge.UnoUrlResolver',local)
ctx=None
for _ in range(40):
    try: ctx=resolver.resolve(f'uno:socket,host=127.0.0.1,port={PORT};urp;StarOffice.ComponentContext'); break
    except Exception: time.sleep(.4)
if not ctx: raise SystemExit('no ctx')
desktop=ctx.ServiceManager.createInstanceWithContext('com.sun.star.frame.Desktop',ctx)
doc=desktop.loadComponentFromURL(uno.systemPathToFileUrl(str(DOCX)),'_blank',0,())
doc.refresh()
try:
    idxs=doc.getDocumentIndexes()
    print('indexes',idxs.getCount())
    for i in range(idxs.getCount()): idxs.getByIndex(i).update()
except Exception as e: print('update err',e)
doc.refresh()
def pv(n,v):
    p=PropertyValue(); p.Name=n; p.Value=v; return p
doc.storeToURL(uno.systemPathToFileUrl(str(DOCX)),(pv('FilterName','Office Open XML Text'),))
doc.storeToURL(uno.systemPathToFileUrl(str(PDF)),(pv('FilterName','writer_pdf_Export'),))
doc.close(True)
try: proc.terminate(); proc.wait(timeout=5)
except Exception: proc.kill()
print('updated')
