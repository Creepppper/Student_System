package com.ruoyi.system.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 当前登录用户的业务数据范围（不落库）。
 *
 * - ALL：管理员，可访问全部数据；
 * - TEACHER：班主任，只能访问其负责班级（grade + class_name）的学生、成绩、查卷；
 * - STUDENT：学生，只能访问绑定到本账号的学生数据；
 * - NONE：没有上述角色，不能访问任何业务数据。
 *
 * 数据范围同时用于 SQL 过滤（通过 params 传给 Mapper）和 Java 侧的逐条校验。
 *
 * @author ruoyi
 */
public class StuScope
{
    public enum Mode
    {
        ALL, TEACHER, STUDENT, NONE
    }

    private final Mode mode;
    private final Long userId;
    private final Long studentId;
    private final Set<String> classKeys;

    private StuScope(Mode mode, Long userId, Long studentId, Set<String> classKeys)
    {
        this.mode = mode;
        this.userId = userId;
        this.studentId = studentId;
        this.classKeys = classKeys;
    }

    public static StuScope all(Long userId)
    {
        return new StuScope(Mode.ALL, userId, null, Collections.emptySet());
    }

    public static StuScope none(Long userId)
    {
        return new StuScope(Mode.NONE, userId, null, Collections.emptySet());
    }

    public static StuScope teacher(Long userId, Set<String> classKeys)
    {
        return new StuScope(Mode.TEACHER, userId, null, new HashSet<>(classKeys));
    }

    /**
     * 学生范围；studentId 为 null 表示账号尚未绑定学生，此时查询结果为空。
     */
    public static StuScope student(Long userId, Long studentId)
    {
        return new StuScope(Mode.STUDENT, userId, studentId, Collections.emptySet());
    }

    /**
     * 班级键，用于判断某个班级是否在班主任范围内
     */
    public static String classKey(String grade, String className)
    {
        return (grade == null ? "" : grade) + "\u0001" + (className == null ? "" : className);
    }

    public Mode getMode()
    {
        return mode;
    }

    public Long getUserId()
    {
        return userId;
    }

    public Long getStudentId()
    {
        return studentId;
    }

    public Set<String> getClassKeys()
    {
        return Collections.unmodifiableSet(classKeys);
    }

    public boolean isAll()
    {
        return mode == Mode.ALL;
    }

    /**
     * 判断某个班级是否在范围内
     */
    public boolean allowsClass(String grade, String className)
    {
        switch (mode)
        {
            case ALL:
                return true;
            case TEACHER:
                return classKeys.contains(classKey(grade, className));
            default:
                return false;
        }
    }

    /** 管理员可查看所有年级；班主任可查看其负责班级所在的年级。 */
    public boolean allowsGrade(String grade)
    {
        if (mode == Mode.ALL)
        {
            return true;
        }
        if (mode != Mode.TEACHER || grade == null)
        {
            return false;
        }
        String prefix = grade + "\u0001";
        return classKeys.stream().anyMatch(key -> key.startsWith(prefix));
    }

    /**
     * 判断某个学生记录是否在范围内
     */
    public boolean allowsStudent(StuStudent student)
    {
        if (mode == Mode.ALL)
        {
            return true;
        }
        if (student == null)
        {
            return false;
        }
        switch (mode)
        {
            case TEACHER:
                return allowsClass(student.getGrade(), student.getClassName());
            case STUDENT:
                return studentId != null && studentId.equals(student.getId());
            default:
                return false;
        }
    }

    /**
     * 把范围写入 Mapper 参数，供 XML 中的 scopeMode / scopeUserId 条件使用
     */
    public void applyTo(Map<String, Object> params)
    {
        params.put("scopeMode", mode.name());
        params.put("scopeUserId", userId);
    }
}
