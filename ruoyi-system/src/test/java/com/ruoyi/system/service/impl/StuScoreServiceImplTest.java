package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;

/**
 * 学生成绩 Service 校验规则测试
 */
@ExtendWith(MockitoExtension.class)
class StuScoreServiceImplTest
{
    @Mock
    private StuScoreMapper stuScoreMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private StuExamMapper stuExamMapper;

    @InjectMocks
    private StuScoreServiceImpl service;

    @BeforeEach
    void setUp()
    {
        // 默认学生、考试存在，且该学生在该考试中尚无成绩
        // lenient：部分用例会在查询前失败，不使用这些默认桩
        lenient().when(stuStudentMapper.selectStuStudentById(1L)).thenReturn(new StuStudent());
        lenient().when(stuExamMapper.selectStuExamById(2L)).thenReturn(new StuExam());
    }

    private StuScore validScore()
    {
        StuScore score = new StuScore();
        score.setStudentId(1L);
        score.setExamId(2L);
        score.setChinese(new BigDecimal("120"));
        score.setMath(new BigDecimal("130"));
        score.setEnglish(new BigDecimal("140"));
        score.setPhysics(new BigDecimal("90"));
        score.setChemistry(new BigDecimal("80"));
        score.setBiology(new BigDecimal("70"));
        score.setAbsentSubjects("");
        return score;
    }

    private void assertRejected(StuScore score, String expectedMessage)
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertStuScore(score));
        assertEquals(expectedMessage, ex.getMessage());
        verify(stuScoreMapper, never()).insertStuScore(any());
    }

    @Test
    void validScoreIsInserted()
    {
        StuScore score = validScore();
        service.insertStuScore(score);
        verify(stuScoreMapper).insertStuScore(score);
    }

    @Test
    void emptyAbsentSubjectsAndNullScoresAreAllowed()
    {
        StuScore score = new StuScore();
        score.setStudentId(1L);
        score.setExamId(2L);
        score.setAbsentSubjects(null);
        service.insertStuScore(score);
        assertEquals("", score.getAbsentSubjects());
        assertNull(score.getChinese());
        verify(stuScoreMapper).insertStuScore(score);
    }

    @Test
    void scoreAboveFullMarkIsRejected()
    {
        StuScore score = validScore();
        score.setChinese(new BigDecimal("151"));
        assertRejected(score, "语文成绩必须在0到150之间");

        StuScore physics = validScore();
        physics.setPhysics(new BigDecimal("101"));
        assertRejected(physics, "物理成绩必须在0到100之间");
    }

    @Test
    void fullMarkIsAccepted()
    {
        StuScore score = validScore();
        score.setMath(new BigDecimal("150"));
        score.setBiology(new BigDecimal("100"));
        service.insertStuScore(score);
        verify(stuScoreMapper).insertStuScore(score);
    }

    @Test
    void negativeScoreIsRejected()
    {
        StuScore score = validScore();
        score.setEnglish(new BigDecimal("-1"));
        assertRejected(score, "英语成绩必须在0到150之间");
    }

    @Test
    void moreThanTwoDecimalsIsRejected()
    {
        StuScore score = validScore();
        score.setMath(new BigDecimal("80.123"));
        assertRejected(score, "数学成绩最多保留两位小数");
    }

    @Test
    void twoDecimalsIsAccepted()
    {
        StuScore score = validScore();
        score.setMath(new BigDecimal("80.55"));
        service.insertStuScore(score);
        verify(stuScoreMapper).insertStuScore(score);
    }

    @Test
    void absentSubjectWithNonZeroScoreIsRejected()
    {
        StuScore score = validScore();
        score.setAbsentSubjects("physics");
        assertRejected(score, "物理为缺考科目，成绩必须为0或留空");
    }

    @Test
    void absentSubjectIsNormalizedAndScoreRecordedAsZero()
    {
        StuScore score = validScore();
        score.setPhysics(null);
        score.setChinese(null);
        score.setAbsentSubjects(" physics , chinese ");
        service.insertStuScore(score);

        assertEquals("chinese,physics", score.getAbsentSubjects());
        assertEquals(0, BigDecimal.ZERO.compareTo(score.getPhysics()));
        assertEquals(0, BigDecimal.ZERO.compareTo(score.getChinese()));
        verify(stuScoreMapper).insertStuScore(score);
    }

    @Test
    void duplicateAbsentCodesAreCollapsed()
    {
        StuScore score = validScore();
        score.setChinese(null);
        score.setAbsentSubjects("chinese,CHINESE");
        service.insertStuScore(score);
        assertEquals("chinese", score.getAbsentSubjects());
    }

    @Test
    void unknownAbsentCodeIsRejected()
    {
        StuScore score = validScore();
        score.setAbsentSubjects("history");
        assertRejected(score, "缺考科目代码不正确：history");
    }

    @Test
    void missingStudentOrExamIsRejected()
    {
        StuScore noStudent = validScore();
        noStudent.setStudentId(null);
        assertRejected(noStudent, "请选择学生");

        StuScore noExam = validScore();
        noExam.setExamId(null);
        assertRejected(noExam, "请选择考试");
    }

    @Test
    void nonexistentStudentOrExamIsRejected()
    {
        StuScore badStudent = validScore();
        badStudent.setStudentId(99L);
        assertRejected(badStudent, "所选学生不存在");

        StuScore badExam = validScore();
        badExam.setExamId(99L);
        assertRejected(badExam, "所选考试不存在");
    }

    @Test
    void duplicateStudentAndExamIsRejectedOnInsert()
    {
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(existing(10L)));
        assertRejected(validScore(), "该学生在此考试中已有成绩记录，请直接修改");
    }

    @Test
    void updateToOwnRecordIsAllowed()
    {
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(existing(10L)));
        StuScore score = validScore();
        score.setId(10L);
        service.updateStuScore(score);
        verify(stuScoreMapper).updateStuScore(score);
    }

    @Test
    void updateToAnotherStudentsRecordIsRejected()
    {
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(existing(10L)));
        StuScore score = validScore();
        score.setId(11L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateStuScore(score));
        assertEquals("该学生在此考试中已有成绩记录，请直接修改", ex.getMessage());
        verify(stuScoreMapper, never()).updateStuScore(any());
    }

    @Test
    void updateWithoutIdIsRejected()
    {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStuScore(validScore()));
        assertEquals("成绩ID不能为空", ex.getMessage());
    }

    @Test
    void updateCanClearAbsentSubjects()
    {
        StuScore score = validScore();
        score.setId(10L);
        score.setAbsentSubjects(null);
        service.updateStuScore(score);
        assertEquals("", score.getAbsentSubjects());
        verify(stuScoreMapper).updateStuScore(score);
    }

    private StuScore existing(Long id)
    {
        StuScore score = new StuScore();
        score.setId(id);
        score.setStudentId(1L);
        score.setExamId(2L);
        return score;
    }
}
