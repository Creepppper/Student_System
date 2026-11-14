package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.StuStudent;

/**
 * 数据范围相关测试的公共构造方法
 */
final class StuTestFixtures
{
    private StuTestFixtures()
    {
    }

    static StuStudent student(Long id, String grade, String className, Long userId)
    {
        StuStudent s = new StuStudent();
        s.setId(id);
        s.setStudentNo("2026" + id);
        s.setStudentName("学生" + id);
        s.setGrade(grade);
        s.setClassName(className);
        s.setUserId(userId);
        return s;
    }

    static SysUser activeUser(long userId)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus("0");
        return user;
    }

    static SysRole role(String key)
    {
        SysRole role = new SysRole();
        role.setRoleKey(key);
        return role;
    }

    /** 模拟以某个账号登录，供 SecurityUtils 读取当前用户与角色 */
    static void login(long userId, String... roleKeys)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        List<SysRole> roles = new ArrayList<>();
        for (String key : roleKeys)
        {
            roles.add(role(key));
        }
        user.setRoles(roles);
        LoginUser loginUser = new LoginUser(userId, null, user, Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, java.util.Collections.emptyList()));
    }

    static void logout()
    {
        SecurityContextHolder.clearContext();
    }
}
