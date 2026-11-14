package com.ruoyi.system.service.impl;

import static com.ruoyi.system.service.impl.StuTestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;

/**
 * 查卷申请：学生端提交（只能为自己）与班主任范围内的写入限制
 */
@ExtendWith(MockitoExtension.class)
class StuReviewApplyTest
{
    @Mock
    private StuReviewMapper stuReviewMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private StuExamMapper stuExamMapper;

    @Mock
    private StuScoreMapper stuScoreMapper;

    @Mock
    private IStuScopeService stuScopeService;

    @InjectMocks
    private StuReviewServiceImpl reviewService;

    @BeforeEach
    void referenceExists()
    {
        lenient().when(stuStudentMapper.selectStuStudentById(anyLong()))
                .thenAnswer(invocation -> student(invocation.getArgument(0), "高一", "1班", 9L));
        lenient().when(stuExamMapper.selectStuExamById(anyLong())).thenReturn(new StuExam());
        lenient().when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(new StuScore()));
    }

    private StuReview input(Long studentId, String subject)
    {
        StuReview review = new StuReview();
        review.setStudentId(studentId);
        review.setExamId(1L);
        review.setSubject(subject);
        review.setReason("请复核");
        return review;
    }

    @Test
    void studentWithoutBindingCannotApply()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.student(9L, null));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.applyReview(input(99L, "math")));
        assertEquals("当前账号未绑定学生，请联系管理员", ex.getMessage());
    }

    @Test
    void nonStudentCannotUseStudentApply()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.applyReview(input(99L, "math")));
        assertEquals("当前账号不是学生账号", ex.getMessage());
    }

    @Test
    void applyForcesOwnStudentIdAndRejectsDuplicateSubject()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.student(9L, 3L));
        StuReview pending = new StuReview();
        pending.setStatus("0");
        when(stuReviewMapper.selectStuReviewList(any())).thenReturn(List.of(pending));

        StuReview review = input(99L, "math");
        ServiceException ex = assertThrows(ServiceException.class, () -> reviewService.applyReview(review));
        assertEquals("该科目已有待处理或已受理的查卷申请，请勿重复提交", ex.getMessage());
        assertEquals(3L, review.getStudentId(), "学生ID必须来自登录账号，不能使用客户端传入的值");
    }

    @Test
    void applySucceedsAndNormalizesSubject()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.student(9L, 3L));
        when(stuReviewMapper.selectStuReviewList(any())).thenReturn(List.of());

        StuReview review = input(99L, "Math");
        reviewService.applyReview(review);

        assertEquals(3L, review.getStudentId());
        assertEquals("math", review.getSubject());
        assertEquals("0", review.getStatus());
        verify(stuReviewMapper).insertStuReview(review);
    }

    @Test
    void teacherCannotInsertReviewForStudentOutsideClass()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L,
                Set.of(StuScope.classKey("高二", "1班"))));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.insertStuReview(input(3L, "math")));
        assertEquals("没有权限为该学生提交查卷", ex.getMessage());
    }

    @Test
    void teacherCannotHandleReviewOutsideClass()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L,
                Set.of(StuScope.classKey("高二", "1班"))));
        StuReview existing = new StuReview();
        existing.setId(1L);
        existing.setStudentId(3L);
        existing.setStatus("0");
        when(stuReviewMapper.selectStuReviewById(1L)).thenReturn(existing);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> reviewService.handleStuReview(1L, com.ruoyi.system.domain.StuReviewAction.ACCEPT, null));
        assertEquals("没有权限操作该查卷申请", ex.getMessage());
    }
}
