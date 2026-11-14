package com.ruoyi.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;

/**
 * 业务数据范围服务实现
 *
 * 角色优先级：超级管理员 / admin 角色 > 班主任(teacher) > 学生(student) > 无。
 *
 * @author ruoyi
 */
@Service
public class StuScopeServiceImpl implements IStuScopeService
{
    /** 班主任角色标识（sys_role.role_key） */
    public static final String ROLE_TEACHER = "teacher";

    /** 学生角色标识（sys_role.role_key） */
    public static final String ROLE_STUDENT = "student";

    @Autowired
    private StuClassTeacherMapper stuClassTeacherMapper;

    @Autowired
    private StuStudentMapper stuStudentMapper;

    @Override
    public StuScope currentScope()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        Set<String> roleKeys = roleKeys(loginUser);

        if (SecurityUtils.isAdmin(userId) || roleKeys.contains("admin"))
        {
            return StuScope.all(userId);
        }
        if (roleKeys.contains(ROLE_TEACHER))
        {
            Set<String> classKeys = new HashSet<>();
            List<StuClassTeacher> classes = stuClassTeacherMapper.selectStuClassTeacherByUserId(userId);
            for (StuClassTeacher item : classes)
            {
                classKeys.add(StuScope.classKey(item.getGrade(), item.getClassName()));
            }
            return StuScope.teacher(userId, classKeys);
        }
        if (roleKeys.contains(ROLE_STUDENT))
        {
            StuStudent student = stuStudentMapper.selectStuStudentByUserId(userId);
            return StuScope.student(userId, student == null ? null : student.getId());
        }
        return StuScope.none(userId);
    }

    private Set<String> roleKeys(LoginUser loginUser)
    {
        if (loginUser.getUser() == null || loginUser.getUser().getRoles() == null)
        {
            return new HashSet<>();
        }
        List<SysRole> roles = loginUser.getUser().getRoles();
        return roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
    }
}
