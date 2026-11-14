package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuClassStat;
import com.ruoyi.system.domain.StuScoreRank;
import com.ruoyi.system.domain.StuTranscriptItem;

/**
 * 学生成绩Service接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface IStuScoreService 
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
     * 批量删除学生成绩
     * 
     * @param ids 需要删除的学生成绩主键集合
     * @return 结果
     */
    public int deleteStuScoreByIds(Long[] ids);

    /**
     * 删除学生成绩信息
     * 
     * @param id 学生成绩主键
     * @return 结果
     */
    public int deleteStuScoreById(Long id);

    /**
     * 查询某次考试的排名
     *
     * @param examId 考试ID
     * @param scope 排名范围：GRADE 年级排名，CLASS 班级排名
     * @param subjectCode 排名科目代码（chinese 等），为空表示总分
     * @param grade 年级（必选）：只在该年级内排名
     * @param className 班级（班级排名时必选）：只在该班级内排名
     * @return 排名结果
     */
    public List<StuScoreRank> selectScoreRanking(Long examId, String scope, String subjectCode, String grade,
            String className);

    /**
     * 查询学生成绩单：历次考试的六科成绩，以及各次考试的年级名次和班级名次
     *
     * @param studentId 学生ID
     * @return 成绩单条目，按考试日期升序
     */
    public List<StuTranscriptItem> selectTranscript(Long studentId);

    /**
     * 查询某次考试的班级平均分（只统计六科齐全的学生；班主任只看负责班级）
     *
     * @param examId 考试ID
     * @return 班级统计，按年级、班级排序
     */
    public List<StuClassStat> selectClassStat(Long examId);

    /**
     * 当前学生账号自己的成绩单（学生端“我的成绩”使用，不接受外部传入的学生ID）
     *
     * @return 成绩单条目，按考试日期升序
     */
    public List<StuTranscriptItem> selectMyTranscript();
}
