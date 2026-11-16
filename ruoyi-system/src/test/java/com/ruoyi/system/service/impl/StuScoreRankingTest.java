package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuScoreDetail;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuScoreRank;
import com.ruoyi.system.domain.StuScoreSubject;
import com.ruoyi.system.domain.StuTranscriptItem;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;

/**
 * 排名计算与成绩单测试
 */
@ExtendWith(MockitoExtension.class)
class StuScoreRankingTest
{
    @Mock
    private StuScoreMapper stuScoreMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private StuExamMapper stuExamMapper;

    @Mock
    private com.ruoyi.system.service.IStuScopeService stuScopeService;

    @org.junit.jupiter.api.BeforeEach
    void allowAllScope()
    {
        lenient().when(stuScopeService.currentScope()).thenReturn(com.ruoyi.system.domain.StuScope.all(1L));
    }

    @InjectMocks
    private StuScoreServiceImpl service;

    /** 构造成绩明细；传入 null 表示未录入 */
    private StuScoreDetail row(long studentId, String studentNo, String grade, String className,
            Integer chinese, Integer math, Integer english, Integer physics, Integer chemistry, Integer biology)
    {
        StuScoreDetail d = new StuScoreDetail();
        d.setScoreId(studentId * 10);
        d.setExamId(1L);
        d.setExamName("期中");
        d.setStudentId(studentId);
        d.setStudentNo(studentNo);
        d.setStudentName("学生" + studentNo);
        d.setGrade(grade);
        d.setClassName(className);
        d.setChinese(dec(chinese));
        d.setMath(dec(math));
        d.setEnglish(dec(english));
        d.setPhysics(dec(physics));
        d.setChemistry(dec(chemistry));
        d.setBiology(dec(biology));
        d.setAbsentSubjects("");
        return d;
    }

    private static BigDecimal dec(Integer v)
    {
        return v == null ? null : BigDecimal.valueOf(v);
    }

    /** 六科均为 each 分的完整记录，仅用于排名测试（不做满分校验，满分校验在录入时已覆盖） */
    private StuScoreDetail full(long id, String no, String grade, String cls, int each)
    {
        return row(id, no, grade, cls, each, each, each, each, each, each);
    }

    private static List<String> numbers(List<StuScoreRank> ranks)
    {
        return ranks.stream().map(r -> r.getStudentNo() + ":" + r.getRank()).toList();
    }

    // ---------- 排名计算 ----------

    @Test
    void totalIsSumOfSixSubjectsAndNullWhenIncomplete()
    {
        StuScoreDetail complete = row(1, "001", "高一", "1班", 100, 100, 100, 50, 50, 50);
        assertEquals(0, new BigDecimal("450").compareTo(StuScoreRanker.total(complete)));

        StuScoreDetail incomplete = row(2, "002", "高一", "1班", 100, 100, 100, null, 50, 50);
        assertNull(StuScoreRanker.total(incomplete));
        assertFalse(StuScoreRanker.isComplete(incomplete));
    }

    @Test
    void tiedScoresShareRankAndSkipNextRank()
    {
        // 总分：500、480、480、470 -> 名次 1、2、2、4
        List<StuScoreDetail> rows = List.of(
                row(1, "A", "高一", "1班", 150, 150, 150, 50, 0, 0),   // 500 （无 0 在 chemistry 不影响）
                row(2, "B", "高一", "1班", 150, 150, 150, 30, 0, 0),   // 480
                row(3, "C", "高一", "1班", 150, 150, 150, 30, 0, 0),   // 480
                row(4, "D", "高一", "1班", 150, 150, 150, 20, 0, 0));  // 470
        List<StuScoreRank> ranks = StuScoreRanker.rank(rows, StuScoreRanker::gradeKey, null);
        assertEquals(List.of("A:1", "B:2", "C:2", "D:4"), numbers(ranks));
    }

    @Test
    void incompleteRecordsAreNotRanked()
    {
        List<StuScoreDetail> rows = List.of(
                full(1, "A", "高一", "1班", 100),
                row(2, "B", "高一", "1班", 150, null, 150, 100, 100, 100));
        List<StuScoreRank> ranks = StuScoreRanker.rank(rows, StuScoreRanker::gradeKey, null);
        assertEquals(List.of("A:1"), numbers(ranks));
    }

    @Test
    void classRankingSeparatesSameClassNameAcrossGrades()
    {
        // 高一1班 与 高二1班 是两个班级，各自排名
        List<StuScoreDetail> rows = List.of(
                full(1, "A", "高一", "1班", 90),
                full(2, "B", "高一", "1班", 80),
                full(3, "C", "高二", "1班", 100));
        List<StuScoreRank> ranks = StuScoreRanker.rank(rows, StuScoreRanker::classKey, null);
        Map<String, Integer> byNo = Map.of(
                ranks.get(0).getStudentNo(), ranks.get(0).getRank(),
                ranks.get(1).getStudentNo(), ranks.get(1).getRank(),
                ranks.get(2).getStudentNo(), ranks.get(2).getRank());
        assertEquals(1, byNo.get("A"));
        assertEquals(2, byNo.get("B"));
        assertEquals(1, byNo.get("C"));
    }

    @Test
    void singleSubjectRankingUsesOnlyThatSubject()
    {
        // 语文：A 最高；总分：B 最高
        StuScoreDetail a = row(1, "A", "高一", "1班", 150, 50, 50, 50, 50, 50);
        StuScoreDetail b = row(2, "B", "高一", "1班", 100, 150, 150, 100, 100, 100);
        List<StuScoreRank> chinese = StuScoreRanker.rank(List.of(a, b), StuScoreRanker::gradeKey,
                StuScoreSubject.CHINESE);
        assertEquals(List.of("A:1", "B:2"), numbers(chinese));
        assertEquals(0, new BigDecimal("150").compareTo(chinese.get(0).getRankValue()));

        List<StuScoreRank> total = StuScoreRanker.rank(List.of(a, b), StuScoreRanker::gradeKey, null);
        assertEquals(List.of("B:1", "A:2"), numbers(total));
    }

    @Test
    void rankByStudentMapsStudentToRank()
    {
        List<StuScoreDetail> rows = List.of(full(1, "A", "高一", "1班", 90), full(2, "B", "高一", "1班", 90));
        Map<Long, Integer> ranks = StuScoreRanker.rankByStudent(rows, StuScoreRanker::gradeKey, null);
        assertEquals(1, ranks.get(1L));
        assertEquals(1, ranks.get(2L));
    }

    // ---------- 服务层：排名接口 ----------

    @Test
    void rankingByGradeAndClassUsesRankingScope()
    {
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高一", "1班", 90),
                full(2, "B", "高一", "2班", 80),
                full(3, "C", "高一", "2班", 100)));

        List<StuScoreRank> grade = service.selectScoreRanking(1L, "GRADE", null, "高一", null);
        assertEquals(List.of("C:1", "A:2", "B:3"), numbers(grade));

        List<StuScoreRank> cls = service.selectScoreRanking(1L, "CLASS", null, "高一", "2班");
        assertEquals(List.of("C:1", "B:2"), numbers(cls));
    }

    @Test
    void teacherSeesFullRankingForAssignedGradesButOnlyOwnClassRanking()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L, Set.of(
                StuScope.classKey("高二", "2班"), StuScope.classKey("高三", "1班"))));
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高二", "1班", 100),
                full(2, "B", "高二", "2班", 90),
                full(3, "C", "高三", "2班", 80),
                full(4, "D", "高一", "1班", 110)));

        assertEquals(List.of("A:1", "B:2"), numbers(service.selectScoreRanking(1L, "GRADE", "TOTAL", "高二", null)));
        assertEquals(List.of("C:1"), numbers(service.selectScoreRanking(1L, "GRADE", "TOTAL", "高三", null)));
        assertEquals(List.of("B:1"), numbers(service.selectScoreRanking(1L, "CLASS", "TOTAL", "高二", "2班")));
        assertEquals("没有权限查看该班级排名", assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "CLASS", "TOTAL", "高二", "1班")).getMessage());
        assertEquals("没有权限查看该年级排名", assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "GRADE", "TOTAL", "高一", null)).getMessage());
    }

    @Test
    void teacherWithoutAssignedClassesCannotViewRankings()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L, Set.of()));
        assertThrows(ServiceException.class, () -> service.selectScoreRanking(1L, "GRADE", "TOTAL", "高二", null));
    }

    @Test
    void rankingNeverMixesGrades()
    {
        // 高二的 B 总分更高，但不能影响高一的排名；高一内只有 A 一人
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高一", "1班", 90),
                full(2, "B", "高二", "1班", 100)));

        assertEquals(List.of("A:1"), numbers(service.selectScoreRanking(1L, "GRADE", "TOTAL", "高一", null)));
        assertEquals(List.of("B:1"), numbers(service.selectScoreRanking(1L, "GRADE", "TOTAL", "高二", null)));
        assertEquals(List.of("A:1"), numbers(service.selectScoreRanking(1L, "CLASS", "TOTAL", "高一", "1班")));
    }

    @Test
    void classRankingOnlyRanksSelectedClass()
    {
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高一", "1班", 90),
                full(2, "B", "高一", "2班", 100),
                full(3, "C", "高一", "2班", 80)));
        assertEquals(List.of("A:1"), numbers(service.selectScoreRanking(1L, "CLASS", null, "高一", "1班")));
        assertEquals(List.of("B:1", "C:2"), numbers(service.selectScoreRanking(1L, "CLASS", null, "高一", "2班")));
    }

    @Test
    void classRankingRequiresClass()
    {
        ServiceException noClass = assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "CLASS", null, "高一", null));
        assertEquals("班级排名需要选择班级", noClass.getMessage());
    }

    @Test
    void rankingRequiresGrade()
    {
        ServiceException noGrade = assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "GRADE", null, null, null));
        assertEquals("请选择年级", noGrade.getMessage());
    }

    @Test
    void rankingRejectsInvalidParameters()
    {
        ServiceException noExam = assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(null, "GRADE", null, "高一", null));
        assertEquals("请选择考试", noExam.getMessage());

        ServiceException badScope = assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "SCHOOL", null, "高一", null));
        assertEquals("排名范围不正确", badScope.getMessage());

        ServiceException badSubject = assertThrows(ServiceException.class,
                () -> service.selectScoreRanking(1L, "GRADE", "物理", "高一", null));
        assertEquals("排名科目不正确", badSubject.getMessage());
    }

    // ---------- 服务层：成绩单 ----------

    @Test
    void transcriptShowsRanksPerExamAndNoRankForIncompleteRecord()
    {
        StuScoreDetail mine1 = full(1, "A", "高一", "1班", 90);
        mine1.setExamId(1L);
        StuScoreDetail mine2 = row(1, "A", "高一", "1班", 100, null, 100, 100, 100, 100);
        mine2.setExamId(2L);
        when(stuScoreMapper.selectScoreDetailByStudent(1L)).thenReturn(List.of(mine1, mine2));

        // 考试 1：A 第一、B 第二；考试 2 中 A 不完整
        StuScoreDetail b1 = full(2, "B", "高一", "1班", 80);
        b1.setExamId(1L);
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(mine1, b1));
        when(stuScoreMapper.selectScoreDetailByExam(2L)).thenReturn(List.of(mine2));

        List<StuTranscriptItem> transcript = service.selectTranscript(1L);
        assertEquals(2, transcript.size());

        StuTranscriptItem first = transcript.get(0);
        assertTrue(first.isComplete());
        assertEquals(1, first.getGradeRank());
        assertEquals(1, first.getClassRank());
        assertEquals(0, new BigDecimal("540").compareTo(first.getTotal()));

        StuTranscriptItem second = transcript.get(1);
        assertFalse(second.isComplete());
        assertNull(second.getTotal());
        assertNull(second.getGradeRank());
        assertNull(second.getClassRank());
    }

    @Test
    void transcriptRequiresStudent()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.selectTranscript(null));
        assertEquals("请选择学生", ex.getMessage());
    }

    @Test
    void transcriptForStudentWithoutScoresIsEmpty()
    {
        when(stuScoreMapper.selectScoreDetailByStudent(anyLong())).thenReturn(List.of());
        assertTrue(service.selectTranscript(9L).isEmpty());
    }

    // ---------- 班级平均分 ----------

    @Test
    void classStatAveragesOnlyCompleteRecordsPerClass()
    {
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高一", "1班", 100),                    // 总分 600
                full(2, "B", "高一", "1班", 80),                     // 总分 480
                row(3, "C", "高一", "1班", 150, null, 150, 100, 100, 100), // 不完整，排除
                full(4, "D", "高一", "2班", 90)));                   // 总分 540

        List<com.ruoyi.system.domain.StuClassStat> stats = service.selectClassStat(1L);
        assertEquals(2, stats.size());

        com.ruoyi.system.domain.StuClassStat first = stats.get(0);
        assertEquals("1班", first.getClassName());
        assertEquals(2, first.getCount());
        assertEquals(0, new BigDecimal("540").compareTo(first.getAvgTotal()));
        assertEquals(0, new BigDecimal("90.00").compareTo(first.getAvgChinese()));

        assertEquals("2班", stats.get(1).getClassName());
        assertEquals(1, stats.get(1).getCount());
    }

    @Test
    void classStatForTeacherOnlyCountsOwnClasses()
    {
        org.mockito.Mockito.when(stuScopeService.currentScope()).thenReturn(
                com.ruoyi.system.domain.StuScope.teacher(7L,
                        java.util.Set.of(com.ruoyi.system.domain.StuScope.classKey("高一", "2班"))));
        when(stuScoreMapper.selectScoreDetailByExam(1L)).thenReturn(List.of(
                full(1, "A", "高一", "1班", 100),
                full(2, "B", "高一", "2班", 80)));

        List<com.ruoyi.system.domain.StuClassStat> stats = service.selectClassStat(1L);
        assertEquals(1, stats.size());
        assertEquals("2班", stats.get(0).getClassName());
    }
}
