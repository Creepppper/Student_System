package com.ruoyi.system.service.impl;

import static com.ruoyi.system.service.impl.StuTestFixtures.activeUser;
import static com.ruoyi.system.service.impl.StuTestFixtures.role;
import static com.ruoyi.system.service.impl.StuTestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * 班主任负责班级的分配校验
 */
@ExtendWith(MockitoExtension.class)
class StuClassTeacherServiceTest
{
    @Mock
    private StuClassTeacherMapper classTeacherMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @InjectMocks
    private StuClassTeacherServiceImpl service;

    private StuClassTeacher assignment(long userId, String grade, String className)
    {
        StuClassTeacher item = new StuClassTeacher();
        item.setUserId(userId);
        item.setGrade(grade);
        item.setClassName(className);
        return item;
    }

    @Test
    void assignmentRequiresTeacherRole()
    {
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("student")));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.insertStuClassTeacher(assignment(7L, "高一", "1班")));
        assertEquals("请先为该账号分配“班主任”角色", ex.getMessage());
    }

    @Test
    void assignmentRequiresExistingClass()
    {
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("teacher")));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(student(1L, "高一", "2班", null)));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.insertStuClassTeacher(assignment(7L, "高一", "1班")));
        assertEquals("该年级班级暂无学生，请检查年级和班级是否填写正确", ex.getMessage());
    }

    @Test
    void sameClassNameInAnotherGradeDoesNotCount()
    {
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("teacher")));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(student(1L, "高二", "1班", null)));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.insertStuClassTeacher(assignment(7L, "高一", "1班")));
        assertEquals("该年级班级暂无学生，请检查年级和班级是否填写正确", ex.getMessage());
    }

    @Test
    void assignmentRejectsDuplicate()
    {
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("teacher")));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(student(1L, "高一", "1班", null)));
        when(classTeacherMapper.selectStuClassTeacherByUserGradeClass(any()))
                .thenReturn(assignment(7L, "高一", "1班"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.insertStuClassTeacher(assignment(7L, "高一", "1班")));
        assertEquals("该班主任已负责此班级", ex.getMessage());
    }

    @Test
    void classAlreadyAssignedToAnotherTeacherIsRejected()
    {
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("teacher")));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(student(1L, "高一", "1班", null)));
        when(classTeacherMapper.selectStuClassTeacherByGradeClass(any())).thenReturn(assignment(8L, "高一", "1班"));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.insertStuClassTeacher(assignment(7L, "高一", "1班")));
        assertEquals("该年级班级已有班主任，请先取消原分配", ex.getMessage());
        verify(classTeacherMapper, org.mockito.Mockito.never()).insertStuClassTeacher(any());
    }

    @Test
    void validAssignmentIsInserted()
    {
        StuClassTeacher input = assignment(7L, "高一", "1班");
        when(sysUserMapper.selectUserById(7L)).thenReturn(activeUser(7L));
        when(sysRoleMapper.selectRolePermissionByUserId(7L)).thenReturn(List.of(role("teacher")));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(student(1L, "高一", "1班", null)));

        service.insertStuClassTeacher(input);
        verify(classTeacherMapper).insertStuClassTeacher(input);
    }
}
