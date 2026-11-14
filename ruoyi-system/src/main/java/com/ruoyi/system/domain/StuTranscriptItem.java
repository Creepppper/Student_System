package com.ruoyi.system.domain;

/**
 * 成绩单条目：某次考试的成绩，附带该次考试中的年级名次和班级名次（同分并列）。
 * 成绩未完整（有未录入科目）时名次为 null。
 *
 * @author ruoyi
 */
public class StuTranscriptItem extends StuScoreDetail
{
    /** 年级名次 */
    private Integer gradeRank;

    /** 班级名次 */
    private Integer classRank;

    public Integer getGradeRank()
    {
        return gradeRank;
    }

    public void setGradeRank(Integer gradeRank)
    {
        this.gradeRank = gradeRank;
    }

    public Integer getClassRank()
    {
        return classRank;
    }

    public void setClassRank(Integer classRank)
    {
        this.classRank = classRank;
    }
}
