#!/usr/bin/env python3
import zipfile, shutil, sys
from lxml import etree
src, dst = sys.argv[1], sys.argv[2]
shutil.copy(src, dst)
with zipfile.ZipFile(src, 'r') as z:
    data = {n: z.read(n) for n in z.namelist()}
root = etree.fromstring(data['word/document.xml'])
ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
W = '{%s}' % ns['w']
# Repeat header row and prevent row splitting in every table.
for tbl in root.xpath('//w:tbl', namespaces=ns):
    rows = tbl.xpath('./w:tr', namespaces=ns)
    for idx, tr in enumerate(rows):
        trPr = tr.find(W+'trPr')
        if trPr is None:
            trPr = etree.Element(W+'trPr')
            tr.insert(0, trPr)
        if trPr.find(W+'cantSplit') is None:
            trPr.append(etree.Element(W+'cantSplit'))
        if idx == 0 and trPr.find(W+'tblHeader') is None:
            trPr.append(etree.Element(W+'tblHeader'))
# Wrap the reviewed static TOC entries in a Word TOC field so Word can refresh it.
toc = []
for p in root.xpath('//w:p', namespaces=ns):
    st = p.xpath('./w:pPr/w:pStyle/@w:val', namespaces=ns)
    if st and st[0] in ('TOC1', 'TOC2'):
        toc.append(p)
if toc:
    def run(xml):
        return etree.fromstring('<w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">' + xml + '</w:r>')
    for i, el in enumerate([
        run('<w:fldChar w:fldCharType="begin" w:dirty="true"/>'),
        run('<w:instrText xml:space="preserve"> TOC \\o "1-2" \\h \\z \\u </w:instrText>'),
        run('<w:fldChar w:fldCharType="separate"/>')]):
        toc[0].insert(i, el)
    toc[-1].append(run('<w:fldChar w:fldCharType="end"/>'))
    print('wrapped TOC entries', len(toc))
data['word/document.xml'] = etree.tostring(root, xml_declaration=True, encoding='UTF-8', standalone=True)
with zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED) as z:
    for n, b in data.items():
        z.writestr(n, b)
print('wrote', dst, 'tables', len(root.xpath('//w:tbl', namespaces=ns)))
