package com.ruoyi.system.domain;

/** 历次考试平均成绩及其在校内的预估名次。 */
public class StuPrediction extends StuScoreDetail
{
    private Integer examCount;
    private Integer gradeRank;
    private Integer classRank;

    public Integer getExamCount() { return examCount; }
    public void setExamCount(Integer examCount) { this.examCount = examCount; }
    public Integer getGradeRank() { return gradeRank; }
    public void setGradeRank(Integer gradeRank) { this.gradeRank = gradeRank; }
    public Integer getClassRank() { return classRank; }
    public void setClassRank(Integer classRank) { this.classRank = classRank; }
}
