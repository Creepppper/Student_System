package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 成绩明细（查询结果，不落库）：一次考试中一个学生的六科成绩及基本信息。
 *
 * total 和 complete 由服务层计算：complete 表示六科均已录入，只有完整记录才参与排名。
 *
 * @author ruoyi
 */
public class StuScoreDetail
{
    /** 成绩ID */
    private Long scoreId;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 考试日期 */
    private Date examTime;

    /** 学生ID */
    private Long studentId;

    /** 学号 */
    private String studentNo;

    /** 学生姓名 */
    private String studentName;

    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 语文成绩 */
    private java.math.BigDecimal chinese;

    /** 数学成绩 */
    private java.math.BigDecimal math;

    /** 英语成绩 */
    private java.math.BigDecimal english;

    /** 物理成绩 */
    private java.math.BigDecimal physics;

    /** 化学成绩 */
    private java.math.BigDecimal chemistry;

    /** 生物成绩 */
    private java.math.BigDecimal biology;

    /** 缺考科目代码（逗号分隔） */
    private String absentSubjects;

    /** 总分 */
    private java.math.BigDecimal total;

    /** 六科是否均已录入 */
    private boolean complete;

    public Long getScoreId()
    {
        return scoreId;
    }

    public void setScoreId(Long scoreId)
    {
        this.scoreId = scoreId;
    }

    public Long getExamId()
    {
        return examId;
    }

    public void setExamId(Long examId)
    {
        this.examId = examId;
    }

    public String getExamName()
    {
        return examName;
    }

    public void setExamName(String examName)
    {
        this.examName = examName;
    }

    public java.util.Date getExamTime()
    {
        return examTime;
    }

    public void setExamTime(Date examTime)
    {
        this.examTime = examTime;
    }

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
    }

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getGrade()
    {
        return grade;
    }

    public void setGrade(String grade)
    {
        this.grade = grade;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public java.math.BigDecimal getChinese()
    {
        return chinese;
    }

    public void setChinese(java.math.BigDecimal chinese)
    {
        this.chinese = chinese;
    }

    public java.math.BigDecimal getMath()
    {
        return math;
    }

    public void setMath(java.math.BigDecimal math)
    {
        this.math = math;
    }

    public java.math.BigDecimal getEnglish()
    {
        return english;
    }

    public void setEnglish(java.math.BigDecimal english)
    {
        this.english = english;
    }

    public java.math.BigDecimal getPhysics()
    {
        return physics;
    }

    public void setPhysics(java.math.BigDecimal physics)
    {
        this.physics = physics;
    }

    public java.math.BigDecimal getChemistry()
    {
        return chemistry;
    }

    public void setChemistry(java.math.BigDecimal chemistry)
    {
        this.chemistry = chemistry;
    }

    public java.math.BigDecimal getBiology()
    {
        return biology;
    }

    public void setBiology(java.math.BigDecimal biology)
    {
        this.biology = biology;
    }

    public String getAbsentSubjects()
    {
        return absentSubjects;
    }

    public void setAbsentSubjects(String absentSubjects)
    {
        this.absentSubjects = absentSubjects;
    }

    public java.math.BigDecimal getTotal()
    {
        return total;
    }

    public void setTotal(java.math.BigDecimal total)
    {
        this.total = total;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }
}
