package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.domain.SchoolClassNames;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IStuClassTeacherService;

/**
 * 班主任负责班级Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class StuClassTeacherServiceImpl implements IStuClassTeacherService
{
    @Autowired
    private StuClassTeacherMapper stuClassTeacherMapper;

    @Autowired
    private StuStudentMapper stuStudentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<StuClassTeacher> selectStuClassTeacherList(StuClassTeacher query)
    {
        return stuClassTeacherMapper.selectStuClassTeacherList(query);
    }

    @Override
    public int insertStuClassTeacher(StuClassTeacher stuClassTeacher)
    {
        if (stuClassTeacher.getUserId() == null)
        {
            throw new ServiceException("请选择班主任账号");
        }
        if (isBlank(stuClassTeacher.getGrade()) || isBlank(stuClassTeacher.getClassName()))
        {
            throw new ServiceException("请选择年级和班级");
        }
        SchoolClassNames.validate(stuClassTeacher.getGrade(), stuClassTeacher.getClassName());
        SysUser user = sysUserMapper.selectUserById(stuClassTeacher.getUserId());
        if (user == null)
        {
            throw new ServiceException("所选账号不存在");
        }
        boolean isTeacher = false;
        for (SysRole role : sysRoleMapper.selectRolePermissionByUserId(user.getUserId()))
        {
            if (StuScopeServiceImpl.ROLE_TEACHER.equals(role.getRoleKey()))
            {
                isTeacher = true;
                break;
            }
        }
        if (!isTeacher)
        {
            throw new ServiceException("请先为该账号分配“班主任”角色");
        }
        if (!hasStudentsInClass(stuClassTeacher.getGrade(), stuClassTeacher.getClassName()))
        {
            throw new ServiceException("该年级班级暂无学生，请检查年级和班级是否填写正确");
        }
        if (stuClassTeacherMapper.selectStuClassTeacherByUserGradeClass(stuClassTeacher) != null)
        {
            throw new ServiceException("该班主任已负责此班级");
        }
        return stuClassTeacherMapper.insertStuClassTeacher(stuClassTeacher);
    }

    @Override
    public int deleteStuClassTeacherByIds(Long[] ids)
    {
        return stuClassTeacherMapper.deleteStuClassTeacherByIds(ids);
    }

    private boolean hasStudentsInClass(String grade, String className)
    {
        StuStudent query = new StuStudent();
        query.setGrade(grade);
        for (StuStudent student : stuStudentMapper.selectStuStudentList(query))
        {
            if (className.equals(student.getClassName()) && grade.equals(student.getGrade()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
