package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuScoreDetail;

/**
 * 学生成绩Mapper接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface StuScoreMapper 
{
    /**
     * 查询学生成绩
     * 
     * @param id 学生成绩主键
     * @return 学生成绩
     */
    public StuScore selectStuScoreById(Long id);

    /**
     * 查询学生成绩列表
     * 
     * @param stuScore 学生成绩
     * @return 学生成绩集合
     */
    public List<StuScore> selectStuScoreList(StuScore stuScore);

    /**
     * 新增学生成绩
     * 
     * @param stuScore 学生成绩
     * @return 结果
     */
    public int insertStuScore(StuScore stuScore);

    /**
     * 修改学生成绩
     * 
     * @param stuScore 学生成绩
     * @return 结果
     */
    public int updateStuScore(StuScore stuScore);

    /**
     * 删除学生成绩
     * 
     * @param id 学生成绩主键
     * @return 结果
     */
    public int deleteStuScoreById(Long id);

    /**
     * 批量删除学生成绩
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStuScoreByIds(Long[] ids);

    /**
     * 查询某次考试的全部成绩明细（含学生基本信息和考试信息）
     *
     * @param examId 考试ID
     * @return 成绩明细
     */
    public List<StuScoreDetail> selectScoreDetailByExam(Long examId);

    /**
     * 查询某个学生的全部成绩明细，按考试日期升序
     *
     * @param studentId 学生ID
     * @return 成绩明细
     */
    public List<StuScoreDetail> selectScoreDetailByStudent(Long studentId);
}
