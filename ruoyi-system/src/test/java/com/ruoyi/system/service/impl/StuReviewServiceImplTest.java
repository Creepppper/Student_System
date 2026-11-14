package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuReviewAction;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;

/**
 * 查卷申请校验与状态流转测试
 */
@ExtendWith(MockitoExtension.class)
class StuReviewServiceImplTest
{
    @Mock
    private StuReviewMapper stuReviewMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private StuExamMapper stuExamMapper;

    @Mock
    private StuScoreMapper stuScoreMapper;

    @InjectMocks
    private StuReviewServiceImpl service;

    @BeforeEach
    void setUp()
    {
        // 默认：学生 1、考试 1 存在，且学生 1 在考试 1 中有成绩
        lenient().when(stuStudentMapper.selectStuStudentById(1L)).thenReturn(new StuStudent());
        lenient().when(stuExamMapper.selectStuExamById(1L)).thenReturn(new StuExam());
        lenient().when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(new StuScore()));
    }

    private StuReview review(String status, String reply)
    {
        StuReview review = new StuReview();
        review.setId(1L);
        review.setStatus(status);
        review.setReply(reply);
        return review;
    }

    private StuReview validApplication()
    {
        StuReview review = new StuReview();
        review.setStudentId(1L);
        review.setExamId(1L);
        review.setSubject("math");
        review.setReason("请复核数学成绩");
        return review;
    }

    private void mockCurrent(String status, String reply)
    {
        when(stuReviewMapper.selectStuReviewById(1L)).thenReturn(review(status, reply));
    }

    private void assertHandleRejected(StuReviewAction action, String reply, String expectedMessage)
    {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleStuReview(1L, action, reply));
        assertEquals(expectedMessage, ex.getMessage());
        verify(stuReviewMapper, never()).updateStatusIfCurrent(anyLong(), any(), any(), any());
    }

    private void assertInsertRejected(StuReview input, String expectedMessage)
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.insertStuReview(input));
        assertEquals(expectedMessage, ex.getMessage());
        verify(stuReviewMapper, never()).insertStuReview(any());
    }

    // ---------- 状态流转：合法 ----------

    @Test
    void acceptPendingWithBlankReplyKeepsExistingReply()
    {
        mockCurrent("0", null);
        when(stuReviewMapper.updateStatusIfCurrent(1L, "0", "1", null)).thenReturn(1);

        assertEquals(1, service.handleStuReview(1L, StuReviewAction.ACCEPT, "  "));
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "0", "1", null);
    }

    @Test
    void acceptWithoutReplyKeepsExistingReplyOnRecord()
    {
        mockCurrent("0", "旧意见");
        when(stuReviewMapper.updateStatusIfCurrent(1L, "0", "1", "旧意见")).thenReturn(1);

        service.handleStuReview(1L, StuReviewAction.ACCEPT, null);
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "0", "1", "旧意见");
    }

    @Test
    void acceptPendingWithReplySavesTrimmedReply()
    {
        mockCurrent("0", null);
        when(stuReviewMapper.updateStatusIfCurrent(eq(1L), eq("0"), eq("1"), eq("已收到，正在核查")))
                .thenReturn(1);

        service.handleStuReview(1L, StuReviewAction.ACCEPT, "  已收到，正在核查  ");
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "0", "1", "已收到，正在核查");
    }

    @Test
    void rejectPendingWithReplyIsAllowed()
    {
        mockCurrent("0", null);
        when(stuReviewMapper.updateStatusIfCurrent(1L, "0", "2", "超出申请期限")).thenReturn(1);

        service.handleStuReview(1L, StuReviewAction.REJECT, "超出申请期限");
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "0", "2", "超出申请期限");
    }

    @Test
    void rejectAcceptedIsAllowed()
    {
        mockCurrent("1", "已受理");
        when(stuReviewMapper.updateStatusIfCurrent(1L, "1", "2", "分数无误")).thenReturn(1);

        service.handleStuReview(1L, StuReviewAction.REJECT, "分数无误");
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "1", "2", "分数无误");
    }

    @Test
    void completeAcceptedWithReplyIsAllowed()
    {
        mockCurrent("1", "已受理");
        when(stuReviewMapper.updateStatusIfCurrent(1L, "1", "3", "已复核，成绩更正")).thenReturn(1);

        service.handleStuReview(1L, StuReviewAction.COMPLETE, "已复核，成绩更正");
        verify(stuReviewMapper).updateStatusIfCurrent(1L, "1", "3", "已复核，成绩更正");
    }

    // ---------- 状态流转：非法 ----------

    @Test
    void completePendingIsRejected()
    {
        mockCurrent("0", null);
        assertHandleRejected(StuReviewAction.COMPLETE, "完成", "当前状态为「待处理」，不能执行「完成」");
    }

    @Test
    void acceptAcceptedIsRejected()
    {
        mockCurrent("1", null);
        assertHandleRejected(StuReviewAction.ACCEPT, null, "当前状态为「已受理」，不能执行「受理」");
    }

    @Test
    void rejectedAndCompletedAreTerminal()
    {
        mockCurrent("2", "已驳回");
        assertHandleRejected(StuReviewAction.ACCEPT, null, "当前状态为「已驳回」，不能执行「受理」");
        assertHandleRejected(StuReviewAction.REJECT, "再次驳回", "当前状态为「已驳回」，不能执行「驳回」");

        mockCurrent("3", "已完成");
        assertHandleRejected(StuReviewAction.REJECT, "再次驳回", "当前状态为「已完成」，不能执行「驳回」");
        assertHandleRejected(StuReviewAction.COMPLETE, "再次完成", "当前状态为「已完成」，不能执行「完成」");
    }

    // ---------- 处理意见与参数 ----------

    @Test
    void rejectWithoutReplyIsRejected()
    {
        mockCurrent("0", null);
        assertHandleRejected(StuReviewAction.REJECT, "   ", "执行「驳回」时必须填写处理意见");
        assertHandleRejected(StuReviewAction.REJECT, null, "执行「驳回」时必须填写处理意见");
    }

    @Test
    void completeWithoutReplyIsRejected()
    {
        mockCurrent("1", "已受理");
        assertHandleRejected(StuReviewAction.COMPLETE, "", "执行「完成」时必须填写处理意见");
    }

    @Test
    void replyLongerThan500IsRejected()
    {
        mockCurrent("0", null);
        assertHandleRejected(StuReviewAction.REJECT, "字".repeat(501), "处理意见不能超过500个字");
    }

    @Test
    void missingIdOrActionIsRejected()
    {
        ServiceException noId = assertThrows(ServiceException.class,
                () -> service.handleStuReview(null, StuReviewAction.ACCEPT, null));
        assertEquals("查卷申请ID不能为空", noId.getMessage());

        ServiceException noAction = assertThrows(ServiceException.class,
                () -> service.handleStuReview(1L, null, null));
        assertEquals("请选择处理动作", noAction.getMessage());
    }

    @Test
    void nonexistentReviewIsRejected()
    {
        when(stuReviewMapper.selectStuReviewById(1L)).thenReturn(null);
        assertHandleRejected(StuReviewAction.ACCEPT, null, "查卷申请不存在");
    }

    @Test
    void concurrentChangeIsReportedWhenNoRowUpdated()
    {
        mockCurrent("0", null);
        when(stuReviewMapper.updateStatusIfCurrent(1L, "0", "1", null)).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleStuReview(1L, StuReviewAction.ACCEPT, null));
        assertEquals("申请状态已被他人修改，请刷新后重试", ex.getMessage());
    }

    // ---------- 新增：引用校验 ----------

    @Test
    void validApplicationIsInsertedAsPending()
    {
        StuReview input = validApplication();
        input.setStatus("3");
        input.setReply("伪造的意见");

        service.insertStuReview(input);

        assertEquals("0", input.getStatus());
        assertNull(input.getReply());
        verify(stuReviewMapper).insertStuReview(input);
    }

    @Test
    void nonexistentStudentIsRejected()
    {
        StuReview input = validApplication();
        input.setStudentId(99L);
        assertInsertRejected(input, "所选学生不存在");
    }

    @Test
    void nonexistentExamIsRejected()
    {
        StuReview input = validApplication();
        input.setExamId(99L);
        assertInsertRejected(input, "所选考试不存在");
    }

    @Test
    void missingStudentExamOrReasonIsRejected()
    {
        StuReview noStudent = validApplication();
        noStudent.setStudentId(null);
        assertInsertRejected(noStudent, "请选择申请学生");

        StuReview noExam = validApplication();
        noExam.setExamId(null);
        assertInsertRejected(noExam, "请选择考试");

        StuReview noReason = validApplication();
        noReason.setReason("   ");
        assertInsertRejected(noReason, "申请理由不能为空");
    }

    @Test
    void chineseOrUnknownSubjectIsRejected()
    {
        StuReview chinese = validApplication();
        chinese.setSubject("物理");
        assertInsertRejected(chinese, "查卷科目不正确，请选择语文、数学、英语、物理、化学、生物之一");

        StuReview unknown = validApplication();
        unknown.setSubject("history");
        assertInsertRejected(unknown, "查卷科目不正确，请选择语文、数学、英语、物理、化学、生物之一");

        StuReview missing = validApplication();
        missing.setSubject(null);
        assertInsertRejected(missing, "查卷科目不正确，请选择语文、数学、英语、物理、化学、生物之一");
    }

    @Test
    void subjectCodeIsNormalized()
    {
        StuReview input = validApplication();
        input.setSubject(" PHYSICS ");
        service.insertStuReview(input);
        assertEquals("physics", input.getSubject());
        verify(stuReviewMapper).insertStuReview(input);
    }

    @Test
    void studentWithoutScoreInExamIsRejected()
    {
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of());
        assertInsertRejected(validApplication(), "该学生在此考试中没有成绩记录，不能申请查卷");
    }

    // ---------- 修改：只能改待处理的申请，且同样校验引用 ----------

    @Test
    void updateKeepsStatusAndReplyFromDatabase()
    {
        mockCurrent("0", null);
        StuReview input = validApplication();
        input.setId(1L);
        input.setStatus("3");
        input.setReply("伪造的意见");
        input.setReason("修改后的理由");

        service.updateStuReview(input);

        assertEquals("0", input.getStatus());
        assertNull(input.getReply());
        assertEquals("修改后的理由", input.getReason());
        verify(stuReviewMapper).updateStuReview(input);
    }

    @Test
    void updateIsRejectedAfterHandled()
    {
        mockCurrent("1", "已受理");
        StuReview input = validApplication();
        input.setId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateStuReview(input));
        assertEquals("当前状态为「已受理」，只有待处理的申请才能修改", ex.getMessage());
        verify(stuReviewMapper, never()).updateStuReview(any());
    }

    @Test
    void updateToNonexistentStudentIsRejected()
    {
        mockCurrent("0", null);
        StuReview input = validApplication();
        input.setId(1L);
        input.setStudentId(99L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateStuReview(input));
        assertEquals("所选学生不存在", ex.getMessage());
        verify(stuReviewMapper, never()).updateStuReview(any());
    }
}
