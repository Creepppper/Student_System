#!/usr/bin/env python3
# Build the formal SRS DOCX/PDF from the reviewed Markdown.
# Requires a running LibreOffice UNO listener on localhost:2002.
import os, re, sys, time, subprocess, urllib.parse
import uno
from com.sun.star.beans import PropertyValue
from com.sun.star.text.ControlCharacter import PARAGRAPH_BREAK
from com.sun.star.style.BreakType import PAGE_BEFORE
from com.sun.star.text.TextContentAnchorType import AS_CHARACTER
from com.sun.star.text.WrapTextMode import NONE
from com.sun.star.style.NumberingType import ARABIC
from com.sun.star.awt.FontWeight import BOLD, NORMAL

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
MD = os.path.join(ROOT, '学生管理系统需求规格说明书-v1.0-draft.md')
OUT = os.path.join(ROOT, 'output')
DOCX = os.path.join(OUT, 'srs-test.docx')
PDF = os.path.join(OUT, 'srs-test.pdf')
os.makedirs(OUT, exist_ok=True)


def connect():
    local = uno.getComponentContext()
    resolver = local.ServiceManager.createInstanceWithContext(
        'com.sun.star.bridge.UnoUrlResolver', local)
    ctx = resolver.resolve('uno:socket,host=127.0.0.1,port=2002;urp;StarOffice.ComponentContext')
    smgr = ctx.ServiceManager
    desktop = smgr.createInstanceWithContext('com.sun.star.frame.Desktop', ctx)
    return desktop


def prop(name, value):
    p = PropertyValue(); p.Name = name; p.Value = value
    return p


def clean(s):
    s = s.replace('\\|', '|')
    s = s.replace('`', '')
    s = re.sub(r'!\[([^\]]*)\]\([^)]*\)', r'\1', s)
    s = re.sub(r'\[([^\]]+)\]\([^)]*\)', r'\1', s)
    s = s.replace('**', '').replace('__', '')
    s = re.sub(r'(?<!\*)\*(?!\*)', '', s)
    return s.strip()


def inline_runs(s):
    """Return [(text, bold, code)] for a small Markdown subset."""
    out = []
    pos = 0
    pat = re.compile(r'(\*\*[^*]+\*\*|`[^`]+`|\[[^\]]+\]\([^)]*\))')
    for m in pat.finditer(s):
        if m.start() > pos:
            out.append((clean(s[pos:m.start()]), False, False))
        tok = m.group(0)
        if tok.startswith('**'):
            out.append((clean(tok[2:-2]), True, False))
        elif tok.startswith('`'):
            out.append((clean(tok[1:-1]), False, True))
        else:
            out.append((clean(re.sub(r'\[([^\]]+)\]\([^)]*\)', r'\1', tok)), False, False))
        pos = m.end()
    if pos < len(s):
        out.append((clean(s[pos:]), False, False))
    return [x for x in out if x[0]]


class Builder:
    def __init__(self, doc):
        self.doc = doc
        self.text = doc.getText()
        self.styles = doc.getStyleFamilies().getByName('ParagraphStyles')
        self.page_styles = doc.getStyleFamilies().getByName('PageStyles')
        self.chapter = '1'
        self.context = '正文'
        self.table_seq = {}
        self.table_count = 0
        self.figure_count = 0
        self._setup()

    def _setup(self):
        # A4, 2.5cm margins
        for name in ('Standard', 'First Page'):
            ps = self.page_styles.getByName(name)
            ps.Width = 21000; ps.Height = 29700
            ps.LeftMargin = 2500; ps.RightMargin = 2500
            ps.TopMargin = 2500; ps.BottomMargin = 2500
            ps.HeaderIsOn = (name != 'First Page')
            ps.FooterIsOn = (name != 'First Page')
            ps.HeaderHeight = 700; ps.FooterHeight = 700
        ps = self.page_styles.getByName('Standard')
        h = ps.HeaderText.createTextCursor(); h.CharFontName = 'SimSun'; h.CharFontNameAsian = 'SimSun'; h.CharHeight = 8.5
        ps.HeaderText.String = '电子科技大学 · 软件工程课程实验一 · 学生管理系统需求规格说明书'
        f = ps.FooterText.createTextCursor(); f.CharFontName = 'SimSun'; f.CharFontNameAsian = 'SimSun'; f.CharHeight = 9
        ps.FooterText.insertString(f, '第 ', False)
        pn = self.doc.createInstance('com.sun.star.text.TextField.PageNumber'); pn.NumberingType = ARABIC
        ps.FooterText.insertTextContent(f, pn, False)
        ps.FooterText.insertString(f, ' 页 / 共 ', False)
        pc = self.doc.createInstance('com.sun.star.text.TextField.PageCount'); pc.NumberingType = ARABIC
        ps.FooterText.insertTextContent(f, pc, False)
        ps.FooterText.insertString(f, ' 页', False)

        # base styles
        std = self.styles.getByName('Standard')
        std.CharFontName = 'Times New Roman'; std.CharFontNameAsian = 'SimSun'; std.CharFontNameComplex = 'Times New Roman'
        std.CharHeight = 12
        std.ParaFirstLineIndent = 846
        std.ParaLeftMargin = 0; std.ParaRightMargin = 0
        std.ParaTopMargin = 0; std.ParaBottomMargin = 80
        std.ParaAdjust = 3  # justify
        std.ParaLineSpacing = uno.createUnoStruct('com.sun.star.style.LineSpacing')
        std.ParaLineSpacing.Mode = 0; std.ParaLineSpacing.Height = 150
        std.ParaKeepTogether = False

        for i, size in ((1, 16), (2, 14), (3, 12)):
            s = self.styles.getByName(f'Heading {i}')
            s.CharFontName = 'SimHei'; s.CharFontNameAsian = 'SimHei'; s.CharFontNameComplex = 'SimHei'
            s.CharHeight = size; s.CharWeight = BOLD
            s.ParaFirstLineIndent = 0
            s.ParaLeftMargin = 0; s.ParaRightMargin = 0
            s.ParaTopMargin = 360 if i == 1 else 220
            s.ParaBottomMargin = 180 if i == 1 else 100
            s.ParaAdjust = 0
            s.ParaKeepTogether = True
            s.ParaKeepTogether = True
            s.CharColor = 0x000000

        cap = self.styles.getByName('Caption')
        cap.CharFontName = 'SimSun'; cap.CharFontNameAsian = 'SimSun'; cap.CharHeight = 9
        cap.ParaFirstLineIndent = 0; cap.ParaLeftMargin = 0
        # compact TOC so its own page count is stable
        for n in ('Contents 1','Contents 2'):
            s = self.styles.getByName(n)
            s.CharHeight = 9.5
            s.ParaTopMargin = 0; s.ParaBottomMargin = 0
            s.ParaLineSpacing = uno.createUnoStruct('com.sun.star.style.LineSpacing')
            s.ParaLineSpacing.Mode = 0; s.ParaLineSpacing.Height = 100
        cap.ParaAdjust = 3; cap.ParaTopMargin = 60; cap.ParaBottomMargin = 120

    def para(self, text='', style='Standard', break_before=False, page_style=None,
             align=None, keep_next=False, size=None, font=None, color=None,
             left=None, first=None, space_top=None, space_bottom=None):
        c = self.text.createTextCursor(); c.gotoEnd(False)
        c.ParaStyleName = style
        if page_style:
            try: c.PageDescName = page_style
            except Exception: pass
        if align is not None: c.ParaAdjust = align
        if keep_next:
            try: c.ParaKeepTogether = True
            except Exception: pass
        if left is not None: c.ParaLeftMargin = left
        if first is not None: c.ParaFirstLineIndent = first
        if space_top is not None: c.ParaTopMargin = space_top
        if space_bottom is not None: c.ParaBottomMargin = space_bottom
        c.CharFontName = font or 'Times New Roman'
        c.CharFontNameAsian = 'SimSun' if not font else font
        if size: c.CharHeight = size
        if color is not None: c.CharColor = color
        is_heading = style.startswith('Heading')
        for txt, bold, code in inline_runs(text):
            if is_heading:
                c.CharFontName = 'SimHei'; c.CharFontNameAsian = 'SimHei'; c.CharWeight = BOLD
            else:
                c.CharWeight = BOLD if bold else NORMAL
                if code:
                    c.CharFontName = 'Noto Sans Mono CJK SC'; c.CharFontNameAsian = 'Noto Sans Mono CJK SC'
            self.text.insertString(c, txt, False)
            if not is_heading:
                c.CharFontName = font or 'Times New Roman'; c.CharFontNameAsian = 'SimSun' if not font else font
                c.CharWeight = NORMAL
        if break_before:
            try: c.BreakType = PAGE_BEFORE
            except Exception: pass
        self.text.insertControlCharacter(c, PARAGRAPH_BREAK, False)
        return c

    def heading(self, text, level):
        style = f'Heading {level}'
        # Body chapters start on a new page; appendices flow to avoid near-empty pages.
        brk = (level == 1 and not text.startswith('附录'))
        c = self.para(text, style=style, break_before=brk,
                      page_style='Standard' if level == 1 else None,
                      keep_next=True)
        # ensure heading font despite inline handling
        c.CharFontName = 'SimHei'; c.CharFontNameAsian = 'SimHei'; c.CharWeight = BOLD
        return c

    def front_heading(self, text, size=16, break_before=False):
        return self.para(text, style='Standard', size=size, font='SimHei',
                         first=0, align=0, keep_next=True, space_top=300, space_bottom=120,
                         break_before=break_before)

    def bullet(self, text, numbered=False):
        c = self.para(('' if numbered else '• ') + clean(text), style='Standard',
                      first=0, left=600, space_bottom=40)
        return c

    def image(self, alt, src, max_w=16000, max_h=19000):
        path = os.path.abspath(os.path.join(ROOT, src))
        if not os.path.exists(path):
            self.para(f'[图片缺失：{src}]', style='Standard', first=0)
            return
        try:
            from PIL import Image
            with Image.open(path) as im: w, h = im.size
        except Exception:
            w, h = 1600, 900
        f = min(max_w / float(w), max_h / float(h))
        W, H = int(w * f), int(h * f)
        # image paragraph
        c = self.text.createTextCursor(); c.gotoEnd(False)
        c.ParaStyleName = 'Standard'; c.ParaFirstLineIndent = 0; c.ParaAdjust = 3
        g = self.doc.createInstance('com.sun.star.text.GraphicObject')
        g.GraphicURL = uno.systemPathToFileUrl(path)
        g.AnchorType = AS_CHARACTER
        g.Surround = NONE
        g.Width = W; g.Height = H
        self.text.insertTextContent(c, g, False)
        self.text.insertControlCharacter(c, PARAGRAPH_BREAK, False)
        self.figure_count += 1
        self.para(clean(alt), style='Caption', align=3, first=0, keep_next=False)

    def table(self, rows):
        if not rows:
            return
        ncols = max(len(r) for r in rows)
        rows = [r + [''] * (ncols - len(r)) for r in rows]
        # caption before table
        self.table_count += 1
        self.table_seq[self.chapter] = self.table_seq.get(self.chapter, 0) + 1
        ctx = re.sub(r'^(?:\d+(?:\.\d+)*|[A-G](?:\.\d+)?)\s*', '', self.context)
        ctx = re.sub(r'^附录\s*[A-G]\s*', '', ctx)
        cap = f'表 {self.chapter}-{self.table_seq[self.chapter]} {ctx}'
        self.para(cap, style='Caption', align=1, first=0, keep_next=True)
        t = self.doc.createInstance('com.sun.star.text.TextTable')
        t.initialize(len(rows), ncols)
        try:
            t.IsWidthRelative = False
            t.Width = 9070
            t.RepeatHeadline = True
            # Column proportions: keep file/description columns readable.
            header = [str(x) for x in rows[0]]
            if ncols == 2:
                weights = [0.28, 0.72]
            elif ncols == 3:
                weights = [0.25, 0.47, 0.28]
            elif ncols == 4:
                if header and header[0] == '版本':
                    weights = [0.14, 0.16, 0.22, 0.48]
                else:
                    weights = [0.20, 0.27, 0.24, 0.29]
            elif ncols == 5:
                if '文件' in header:
                    weights = [0.12, 0.40, 0.16, 0.13, 0.19]
                elif 'FID' in header or 'ID' in header:
                    weights = [0.10, 0.13, 0.34, 0.31, 0.12]
                else:
                    weights = [0.16, 0.23, 0.22, 0.23, 0.16]
            else:
                weights = [1.0 / ncols] * ncols
            t.TableColumnRelativeSum = 10000
            seps = []
            cum = 0.0
            for w in weights[:-1]:
                cum += w
                s = uno.createUnoStruct('com.sun.star.text.TableColumnSeparator')
                s.Position = int(round(10000 * cum))
                s.IsVisible = True
                seps.append(s)
            t.TableColumnSeparators = tuple(seps)
        except Exception as e:
            print('table width warn', e)
        c = self.text.createTextCursor(); c.gotoEnd(False)
        self.text.insertTextContent(c, t, False)
        # borders
        try:
            line = uno.createUnoStruct('com.sun.star.table.BorderLine')
            line.Color = 0x888888; line.InnerLineWidth = 0; line.OuterLineWidth = 15
            border = uno.createUnoStruct('com.sun.star.table.TableBorder')
            for attr in ('TopBorder','BottomBorder','LeftBorder','RightBorder','HorizontalLine','VerticalLine'):
                setattr(border, attr, line)
            for attr in ('IsTopLineValid','IsBottomLineValid','IsLeftLineValid','IsRightLineValid','IsHorizontalLineValid','IsVerticalLineValid'):
                setattr(border, attr, True)
            t.TableBorder = border
        except Exception as e:
            print('border warn', e)
        for ri, row in enumerate(rows):
            for ci, val in enumerate(row):
                cell = t.getCellByPosition(ci, ri)
                cell.String = clean(val)
                tc = cell.getText().createTextCursor()
                tc.gotoStart(False); tc.gotoEnd(True)
                tc.CharFontName = 'Times New Roman'; tc.CharFontNameAsian = 'SimSun'
                tc.CharHeight = 7.2 if len(rows) > 20 else (8.2 if len(rows) > 14 else 8.5)
                tc.ParaFirstLineIndent = 0; tc.ParaLeftMargin = 40; tc.ParaRightMargin = 40
                tc.ParaTopMargin = 0; tc.ParaBottomMargin = 0
                tc.ParaLineSpacing = uno.createUnoStruct('com.sun.star.style.LineSpacing')
                tc.ParaLineSpacing.Mode = 0; tc.ParaLineSpacing.Height = 84 if len(rows) > 20 else 100
                tc.ParaAdjust = 1 if ri == 0 else 0
                if ri == 0:
                    tc.CharWeight = BOLD; cell.BackColor = 0xE8EEF7
                else:
                    tc.CharWeight = NORMAL
                try:
                    cell.VertOrient = uno.getConstantByName('com.sun.star.table.CellVertOrient.CENTER')
                except Exception:
                    pass
        try:
            for ri in range(len(rows)):
                t.getRows().getByIndex(ri).IsSplitAllowed = (ri > 0)
        except Exception:
            pass
        # paragraph after table
        c2 = self.text.createTextCursor(); c2.gotoEnd(False)
        self.text.insertControlCharacter(c2, PARAGRAPH_BREAK, False)
        return t

    def page_break(self):
        c = self.text.createTextCursor(); c.gotoEnd(False)
        c.BreakType = PAGE_BEFORE


def parse_table(lines, i):
    rows = []
    while i < len(lines) and lines[i].strip().startswith('|'):
        line = lines[i].strip()
        if not re.match(r'^\|[\s:|-]+\|$', line):
            cells = [x.strip() for x in line.strip('|').split('|')]
            rows.append(cells)
        i += 1
    return rows, i


def chapter_label(h):
    m = re.match(r'(\d+)\.', h)
    if m: return m.group(1)
    m = re.match(r'附录\s*([A-G])', h)
    if m: return m.group(1)
    return h[:1] or '1'


def build_body(b, lines):
    i = 0
    buf = []
    def flush():
        nonlocal buf
        if buf:
            b.para(' '.join(buf).strip(), style='Standard')
            buf = []
    while i < len(lines):
        raw = lines[i].rstrip('\n')
        s = raw.strip()
        if not s:
            flush(); i += 1; continue
        if s.startswith('!['):
            flush()
            m = re.match(r'!\[([^\]]*)\]\(([^)]+)\)', s)
            if m:
                b.image(m.group(1), m.group(2))
            i += 1; continue
        if s.startswith('|'):
            flush()
            rows, i = parse_table(lines, i)
            if rows: b.table(rows)
            continue
        m = re.match(r'^(#{2,4})\s+(.*)$', s)
        if m:
            flush()
            level = len(m.group(1)) - 1
            text = clean(m.group(2))
            if level == 1:
                b.chapter = chapter_label(text)
                b.context = text
            elif level == 2:
                b.context = text
            hc = b.heading(text, min(level, 3))
            if level == 1 and text.startswith('附录'):
                try:
                    hc.BreakType = uno.getConstantByName('com.sun.star.style.BreakType.NONE')
                except Exception:
                    pass
            i += 1; continue
        if s in ('---', '***', '___'):
            flush(); i += 1; continue
        if re.match(r'^[-*]\s+', s):
            flush(); b.bullet(re.sub(r'^[-*]\s+', '', s)); i += 1; continue
        if re.match(r'^\d+\.\s+', s):
            flush(); b.bullet(s, numbered=True); i += 1; continue
        if s.startswith('> '):
            flush(); b.para(clean(s[2:]), style='Quotations', first=0, left=600); i += 1; continue
        if s.startswith('```'):
            flush(); i += 1
            while i < len(lines) and not lines[i].strip().startswith('```'):
                b.para(clean(lines[i]), style='Standard', font='Noto Sans Mono CJK SC', first=0, left=400, size=9)
                i += 1
            i += 1; continue
        buf.append(s)
        i += 1
    flush()


def main():
    desktop = connect()
    doc = desktop.loadComponentFromURL('private:factory/swriter', '_blank', 0, ())
    b = Builder(doc)
    b.para('电子科技大学', style='Title', first=0, align=3, font='SimHei', size=18, color=0x1F4E79, space_top=1800, page_style='First Page')
    b.para('软件工程课程实验一', style='Standard', first=0, align=3, font='SimHei', size=16, color=0x333333, space_top=300)
    b.para('学生管理系统需求规格说明书', style='Title', first=0, align=3, font='SimHei', size=28, color=0x000000, space_top=900, space_bottom=200)
    b.para('项目名称：高中学生信息与成绩管理系统', style='Standard', first=0, align=3, font='SimSun', size=16, color=0x444444, space_bottom=1200)
    for line in [
        '文档版本：v1.0-draft',
        '需求基线：BL-REQ-001 v1.2-provisional',
        '文档状态：初稿（待小组/教师评审，未正式批准）',
        '小组名称：待填写',
        '小组成员及学号：待填写',
        '指导教师：待填写',
        '日期：2026 年 10 月 29 日',
    ]:
        b.para(line, style='Standard', first=0, align=3, font='SimSun', size=11, space_bottom=120)
    # force cover page style on first paragraph; set it again on first content
    # revision
    b.front_heading('文档修订历史', 16, break_before=True)
    b.chapter = '0'; b.context = '文档修订历史'
    b.table([['版本','日期','状态','说明'],
             ['v1.0-draft','2026-10-29','整合初稿','整合需求基线、DFD/ER/UML 与 24 张原型截图'],
             ['v0.2','2026-10-28','需求分析初稿','功能、数据、NFR、权限与验收标准'],
             ['v0.1','2026-10-23','调研/初稿','课程要求、范围与角色规则']])
    b.front_heading('文档状态说明', 16, break_before=True)
    b.para('本文为 Markdown 初稿转换的 Word 评审稿。已完成文档级覆盖检查与静态原型渲染检查；未完成教师/小组正式评审，未完成业务代码实现与业务测试。', first=0)
    b.para('文档中的性能、并发、备份恢复、权限渗透等指标均标记为“建议目标，未验证”；原型截图是 Chrome 实际渲染的静态界面，不是已上线业务系统运行截图。', first=0)
    # TOC
    b.para('目录', style='Standard', break_before=True, page_style='Standard', size=16, font='SimHei', first=0, align=0, keep_next=True)
    import json
    toc_entries = json.load(open(os.path.join(ROOT, 'qa/toc_pages.json'), encoding='utf-8'))
    for level, title, page in toc_entries:
        style = 'Contents 1' if level == 1 else 'Contents 2'
        c = doc.getText().createTextCursor(); c.gotoEnd(False)
        c.ParaStyleName = style
        c.ParaFirstLineIndent = 0
        if level == 2:
            c.ParaLeftMargin = 600
        tab = uno.createUnoStruct('com.sun.star.style.TabStop')
        tab.Position = 15600
        tab.Alignment = 2
        tab.FillChar = '.'
        c.ParaTabStops = (tab,)
        doc.getText().insertString(c, title + '\t' + str(page), False)
        doc.getText().insertControlCharacter(c, PARAGRAPH_BREAK, False)
    # body
    text = open(MD, encoding='utf-8').read().splitlines()
    start = next((i for i, l in enumerate(text) if l.strip().startswith('## 1.')), 0)
    build_body(b, text[start:])
    # update indexes and export
    try:
        doc.refresh()
    except Exception as e:
        print('TOC update warn', e)
    # Ask Writer to update all fields/indexes/page numbers before saving.
    try:
        local = uno.getComponentContext()
        res = local.ServiceManager.createInstanceWithContext('com.sun.star.bridge.UnoUrlResolver', local)
        ctx2 = res.resolve('uno:socket,host=127.0.0.1,port=2002;urp;StarOffice.ComponentContext')
        helper = ctx2.ServiceManager.createInstanceWithContext('com.sun.star.frame.DispatchHelper', ctx2)
        helper.executeDispatch(doc.getCurrentController().getFrame(), '.uno:UpdateAll', '', 0, ())
        doc.refresh()
    except Exception as e:
        print('UpdateAll warn', e)
    # Cover uses the no-header First Page style (post-layout assignment is reliable).
    try:
        c0 = doc.getText().createTextCursor(); c0.gotoStart(False); c0.gotoEndOfParagraph(True)
        c0.PageDescName = 'First Page'
    except Exception as e:
        print('cover style warn', e)
    # save
    doc.storeToURL(uno.systemPathToFileUrl(DOCX), (prop('FilterName','MS Word 2007 XML'),))
    print('DOCX', DOCX)
    print('PDF', PDF)
    print('tables', b.table_count, 'figures', b.figure_count)
    time.sleep(1)
    doc.close(True)

if __name__ == '__main__':
    main()
