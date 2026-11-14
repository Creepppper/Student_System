package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.StuExam;

/**
 * 考试信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface StuExamMapper 
{
    /**
     * 查询考试信息
     * 
     * @param id 考试信息主键
     * @return 考试信息
     */
    public StuExam selectStuExamById(Long id);

    /**
     * 查询考试信息列表
     * 
     * @param stuExam 考试信息
     * @return 考试信息集合
     */
    public List<StuExam> selectStuExamList(StuExam stuExam);

    /**
     * 新增考试信息
     * 
     * @param stuExam 考试信息
     * @return 结果
     */
    public int insertStuExam(StuExam stuExam);

    /**
     * 修改考试信息
     * 
     * @param stuExam 考试信息
     * @return 结果
     */
    public int updateStuExam(StuExam stuExam);

    /**
     * 删除考试信息
     * 
     * @param id 考试信息主键
     * @return 结果
     */
    public int deleteStuExamById(Long id);

    /**
     * 批量删除考试信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStuExamByIds(Long[] ids);
}
