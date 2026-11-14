package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.service.IStuExamService;

/**
 * 考试信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuExamServiceImpl implements IStuExamService 
{
    @Autowired
    private StuExamMapper stuExamMapper;

    @Autowired
    private StuScoreMapper stuScoreMapper;

    @Autowired
    private StuReviewMapper stuReviewMapper;

    /**
     * 查询考试信息
     * 
     * @param id 考试信息主键
     * @return 考试信息
     */
    @Override
    public StuExam selectStuExamById(Long id)
    {
        return stuExamMapper.selectStuExamById(id);
    }

    /**
     * 查询考试信息列表
     * 
     * @param stuExam 考试信息
     * @return 考试信息
     */
    @Override
    public List<StuExam> selectStuExamList(StuExam stuExam)
    {
        return stuExamMapper.selectStuExamList(stuExam);
    }

    /**
     * 新增考试信息
     * 
     * @param stuExam 考试信息
     * @return 结果
     */
    @Override
    public int insertStuExam(StuExam stuExam)
    {
        return stuExamMapper.insertStuExam(stuExam);
    }

    /**
     * 修改考试信息
     * 
     * @param stuExam 考试信息
     * @return 结果
     */
    @Override
    public int updateStuExam(StuExam stuExam)
    {
        return stuExamMapper.updateStuExam(stuExam);
    }

    /**
     * 批量删除考试信息
     * 
     * @param ids 需要删除的考试信息主键
     * @return 结果
     */
    @Override
    public int deleteStuExamByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            assertDeletable(id);
        }
        return stuExamMapper.deleteStuExamByIds(ids);
    }

    /**
     * 删除考试信息信息
     * 
     * @param id 考试信息主键
     * @return 结果
     */
    @Override
    public int deleteStuExamById(Long id)
    {
        assertDeletable(id);
        return stuExamMapper.deleteStuExamById(id);
    }

    /**
     * 已有成绩或查卷记录的考试不能删除，避免留下无效引用
     */
    private void assertDeletable(Long examId)
    {
        StuExam exam = stuExamMapper.selectStuExamById(examId);
        String name = exam == null ? String.valueOf(examId) : exam.getExamName();
        StuScore scoreQuery = new StuScore();
        scoreQuery.setExamId(examId);
        if (!stuScoreMapper.selectStuScoreList(scoreQuery).isEmpty())
        {
            throw new ServiceException("考试「" + name + "」已有成绩记录，不能删除");
        }
        StuReview reviewQuery = new StuReview();
        reviewQuery.setExamId(examId);
        if (!stuReviewMapper.selectStuReviewList(reviewQuery).isEmpty())
        {
            throw new ServiceException("考试「" + name + "」已有查卷记录，不能删除");
        }
    }
}
