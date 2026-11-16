package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuPrediction;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;

class StuPredictionServiceTest
{
    private final IStuScopeService scope = mock(IStuScopeService.class);
    private final StuStudentMapper students = mock(StuStudentMapper.class);
    private final StuScoreMapper scores = mock(StuScoreMapper.class);
    private final StuPredictionService service = new StuPredictionService(scope, students, scores);

    private StuPrediction prediction(long id, String cls, int value)
    {
        StuPrediction p = new StuPrediction();
        p.setStudentId(id); p.setStudentNo("S" + id); p.setGrade("高二"); p.setClassName(cls);
        BigDecimal n = BigDecimal.valueOf(value);
        p.setChinese(n); p.setMath(n); p.setEnglish(n); p.setPhysics(n); p.setChemistry(n); p.setBiology(n);
        p.setExamCount(2);
        return p;
    }

    @Test
    void returnsOnlyCurrentStudentWithGradeAndClassRanks()
    {
        when(scope.currentScope()).thenReturn(StuScope.student(9L, 1L));
        when(students.selectStuStudentById(1L)).thenReturn(StuTestFixtures.student(1L, "高二", "2班", 9L));
        when(scores.selectPredictionByGrade("高二")).thenReturn(List.of(
                prediction(1, "2班", 80), prediction(2, "1班", 100), prediction(3, "2班", 90)));
        StuPrediction result = service.myPrediction();
        assertEquals(1L, result.getStudentId());
        assertEquals(3, result.getGradeRank());
        assertEquals(2, result.getClassRank());
        assertEquals(new BigDecimal("480"), result.getTotal());
    }

    @Test
    void emptyScoresAndUnboundAccountAreHandled()
    {
        when(scope.currentScope()).thenReturn(StuScope.student(9L, 1L));
        when(students.selectStuStudentById(1L)).thenReturn(StuTestFixtures.student(1L, "高二", "2班", 9L));
        when(scores.selectPredictionByGrade("高二")).thenReturn(List.of());
        assertNull(service.myPrediction());
        when(scope.currentScope()).thenReturn(StuScope.student(9L, null));
        assertThrows(ServiceException.class, service::myPrediction);
    }

    @Test
    void nonStudentCannotRequestPrediction()
    {
        when(scope.currentScope()).thenReturn(StuScope.all(1L));
        assertThrows(ServiceException.class, service::myPrediction);
        verifyNoInteractions(students, scores);
    }
}
