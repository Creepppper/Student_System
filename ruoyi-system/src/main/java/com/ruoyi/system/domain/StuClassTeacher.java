package com.ruoyi.system.domain;

import java.util.Date;

/**
 * 班主任负责班级对象 stu_class_teacher
 *
 * 一条记录表示：某个班主任账号负责某个年级的某个班级。
 *
 * @author ruoyi
 */
public class StuClassTeacher
{
    /** 主键 */
    private Long id;

    /** 班主任账号（sys_user.user_id） */
    private Long userId;

    /** 账号登录名（关联查询） */
    private String userName;

    /** 账号昵称（关联查询） */
    private String nickName;

    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 创建时间 */
    private Date createTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
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

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
