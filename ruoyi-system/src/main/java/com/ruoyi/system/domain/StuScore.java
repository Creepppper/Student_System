package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生成绩对象 stu_score
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public class StuScore extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩ID */
    private Long id;

    /** 学生ID（stu_student.id） */
    @Excel(name = "学生ID", readConverterExp = "s=tu_student.id")
    private Long studentId;

    /** 列表查询用学号，不作为成绩表字段保存。 */
    private String studentNo;

    public String getStudentNo() { return studentNo; }

    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    /** 考试ID（stu_exam.id） */
    @Excel(name = "考试ID", readConverterExp = "s=tu_exam.id")
    private Long examId;

    /** 语文成绩（满分150；NULL未录入，缺考为0） */
    @Excel(name = "语文成绩", readConverterExp = "满=分150；NULL未录入，缺考为0")
    private BigDecimal chinese;

    /** 数学成绩（满分150；NULL未录入，缺考为0） */
    @Excel(name = "数学成绩", readConverterExp = "满=分150；NULL未录入，缺考为0")
    private BigDecimal math;

    /** 英语成绩（满分150；NULL未录入，缺考为0） */
    @Excel(name = "英语成绩", readConverterExp = "满=分150；NULL未录入，缺考为0")
    private BigDecimal english;

    /** 物理成绩（满分100；NULL未录入，缺考为0） */
    @Excel(name = "物理成绩", readConverterExp = "满=分100；NULL未录入，缺考为0")
    private BigDecimal physics;

    /** 化学成绩（满分100；NULL未录入，缺考为0） */
    @Excel(name = "化学成绩", readConverterExp = "满=分100；NULL未录入，缺考为0")
    private BigDecimal chemistry;

    /** 生物成绩（满分100；NULL未录入，缺考为0） */
    @Excel(name = "生物成绩", readConverterExp = "满=分100；NULL未录入，缺考为0")
    private BigDecimal biology;

    /** 缺考科目代码，多个用英文逗号分隔（chinese,math,english,physics,chemistry,biology） */
    @Excel(name = "缺考科目代码，多个用英文逗号分隔", readConverterExp = "c=hinese,math,english,physics,chemistry,biology")
    private String absentSubjects;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setExamId(Long examId) 
    {
        this.examId = examId;
    }

    public Long getExamId() 
    {
        return examId;
    }

    public void setChinese(BigDecimal chinese) 
    {
        this.chinese = chinese;
    }

    public BigDecimal getChinese() 
    {
        return chinese;
    }

    public void setMath(BigDecimal math) 
    {
        this.math = math;
    }

    public BigDecimal getMath() 
    {
        return math;
    }

    public void setEnglish(BigDecimal english) 
    {
        this.english = english;
    }

    public BigDecimal getEnglish() 
    {
        return english;
    }

    public void setPhysics(BigDecimal physics) 
    {
        this.physics = physics;
    }

    public BigDecimal getPhysics() 
    {
        return physics;
    }

    public void setChemistry(BigDecimal chemistry) 
    {
        this.chemistry = chemistry;
    }

    public BigDecimal getChemistry() 
    {
        return chemistry;
    }

    public void setBiology(BigDecimal biology) 
    {
        this.biology = biology;
    }

    public BigDecimal getBiology() 
    {
        return biology;
    }

    public void setAbsentSubjects(String absentSubjects) 
    {
        this.absentSubjects = absentSubjects;
    }

    public String getAbsentSubjects() 
    {
        return absentSubjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("studentId", getStudentId())
            .append("examId", getExamId())
            .append("chinese", getChinese())
            .append("math", getMath())
            .append("english", getEnglish())
            .append("physics", getPhysics())
            .append("chemistry", getChemistry())
            .append("biology", getBiology())
            .append("absentSubjects", getAbsentSubjects())
            .toString();
    }
}
