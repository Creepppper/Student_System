package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;

/**
 * 考试删除保护：有成绩或查卷记录的考试不能删除
 */
@ExtendWith(MockitoExtension.class)
class StuExamDeleteTest
{
    @Mock
    private StuExamMapper stuExamMapper;

    @Mock
    private StuScoreMapper stuScoreMapper;

    @Mock
    private StuReviewMapper stuReviewMapper;

    @InjectMocks
    private StuExamServiceImpl examService;

    private StuExam exam(String name)
    {
        StuExam exam = new StuExam();
        exam.setId(2L);
        exam.setExamName(name);
        return exam;
    }

    @Test
    void examWithScoresCannotBeDeleted()
    {
        when(stuExamMapper.selectStuExamById(2L)).thenReturn(exam("期中"));
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(new StuScore()));

        ServiceException ex = assertThrows(ServiceException.class, () -> examService.deleteStuExamById(2L));
        assertEquals("考试「期中」已有成绩记录，不能删除", ex.getMessage());
        verify(stuExamMapper, never()).deleteStuExamById(anyLong());
    }

    @Test
    void examWithReviewsCannotBeDeletedInBatch()
    {
        when(stuExamMapper.selectStuExamById(2L)).thenReturn(exam("期末"));
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of());
        when(stuReviewMapper.selectStuReviewList(any())).thenReturn(List.of(new StuReview()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> examService.deleteStuExamByIds(new Long[] { 2L }));
        assertEquals("考试「期末」已有查卷记录，不能删除", ex.getMessage());
        verify(stuExamMapper, never()).deleteStuExamByIds(any());
    }
}
