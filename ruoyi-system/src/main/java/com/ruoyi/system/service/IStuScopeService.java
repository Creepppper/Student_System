package com.ruoyi.system.service;

import com.ruoyi.system.domain.StuScope;

/**
 * 业务数据范围服务：根据当前登录账号的角色计算可访问的学生、班级范围
 *
 * @author ruoyi
 */
public interface IStuScopeService
{
    /**
     * 当前登录用户的数据范围
     *
     * @return 数据范围；管理员为 ALL，班主任为 TEACHER，学生为 STUDENT，其他为 NONE
     */
    public StuScope currentScope();
}
