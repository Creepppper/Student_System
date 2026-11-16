package com.ruoyi.common.core.domain.model;

/**
 * 用户注册对象
 * 
 * @author ruoyi
 */
public class RegisterBody extends LoginBody
{
    private String roleKey;

    public String getRoleKey() { return roleKey; }

    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
}
