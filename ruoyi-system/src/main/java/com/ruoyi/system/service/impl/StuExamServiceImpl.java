package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.domain.StuExam;
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
        return stuExamMapper.deleteStuExamById(id);
    }
}
