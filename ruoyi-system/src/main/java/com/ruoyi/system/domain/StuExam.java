package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试信息对象 stu_exam
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public class StuExam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long id;

    /** 考试名称（如月考、期中、期末） */
    @Excel(name = "考试名称", readConverterExp = "如=月考、期中、期末")
    private String examName;

    /** 考试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "考试日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date examTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setExamName(String examName) 
    {
        this.examName = examName;
    }

    public String getExamName() 
    {
        return examName;
    }

    public void setExamTime(Date examTime) 
    {
        this.examTime = examTime;
    }

    public Date getExamTime() 
    {
        return examTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("examName", getExamName())
            .append("examTime", getExamTime())
            .toString();
    }
}
