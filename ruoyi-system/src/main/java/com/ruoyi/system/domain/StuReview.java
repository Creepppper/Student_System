package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 查卷申请对象 stu_review
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public class StuReview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 查卷申请ID */
    private Long id;

    /** 申请学生ID（stu_student.id） */
    @Excel(name = "申请学生ID", readConverterExp = "s=tu_student.id")
    private Long studentId;

    /** 考试ID（stu_exam.id） */
    @Excel(name = "考试ID", readConverterExp = "s=tu_exam.id")
    private Long examId;

    /** 查卷科目代码（chinese/math/english/physics/chemistry/biology） */
    @Excel(name = "查卷科目代码", readConverterExp = "c=hinese/math/english/physics/chemistry/biology")
    private String subject;

    /** 申请理由 */
    @Excel(name = "申请理由")
    private String reason;

    /** 状态（0待处理 1已受理 2已驳回 3已完成） */
    @Excel(name = "状态", readConverterExp = "0=待处理,1=已受理,2=已驳回,3=已完成")
    private String status;

    /** 班主任处理意见 */
    @Excel(name = "班主任处理意见")
    private String reply;

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

    public void setSubject(String subject) 
    {
        this.subject = subject;
    }

    public String getSubject() 
    {
        return subject;
    }

    public void setReason(String reason) 
    {
        this.reason = reason;
    }

    public String getReason() 
    {
        return reason;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setReply(String reply) 
    {
        this.reply = reply;
    }

    public String getReply() 
    {
        return reply;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("studentId", getStudentId())
            .append("examId", getExamId())
            .append("subject", getSubject())
            .append("reason", getReason())
            .append("status", getStatus())
            .append("reply", getReply())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
