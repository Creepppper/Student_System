package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生信息对象 stu_student
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public class StuStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long id;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 年级（如高一、高二、高三） */
    @Excel(name = "年级", readConverterExp = "如=高一、高二、高三")
    private String grade;

    /** 班级名称（如1班、2班） */
    @Excel(name = "班级名称", readConverterExp = "如=1班、2班")
    private String className;

    /** 关联若依用户ID（sys_user.user_id），NULL表示未绑定 */
    @Excel(name = "关联若依用户ID", readConverterExp = "s=ys_user.user_id")
    private Long userId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setStudentNo(String studentNo) 
    {
        this.studentNo = studentNo;
    }

    public String getStudentNo() 
    {
        return studentNo;
    }

    public void setStudentName(String studentName) 
    {
        this.studentName = studentName;
    }

    public String getStudentName() 
    {
        return studentName;
    }

    public void setGrade(String grade) 
    {
        this.grade = grade;
    }

    public String getGrade() 
    {
        return grade;
    }

    public void setClassName(String className) 
    {
        this.className = className;
    }

    public String getClassName() 
    {
        return className;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("studentNo", getStudentNo())
            .append("studentName", getStudentName())
            .append("grade", getGrade())
            .append("className", getClassName())
            .append("userId", getUserId())
            .toString();
    }
}
