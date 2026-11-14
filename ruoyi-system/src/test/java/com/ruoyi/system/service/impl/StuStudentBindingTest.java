package com.ruoyi.system.service.impl;

import static com.ruoyi.system.service.impl.StuTestFixtures.activeUser;
import static com.ruoyi.system.service.impl.StuTestFixtures.role;
import static com.ruoyi.system.service.impl.StuTestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IStuScopeService;

/**
 * 学生信息：账号绑定校验、删除保护、班主任范围下的读写限制
 */
@ExtendWith(MockitoExtension.class)
class StuStudentBindingTest
{
    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private StuScoreMapper stuScoreMapper;

    @Mock
    private StuReviewMapper stuReviewMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private IStuScopeService stuScopeService;

    @InjectMocks
    private StuStudentServiceImpl studentService;

    @BeforeEach
    void allowAllByDefault()
    {
        lenient().when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
    }

    @Test
    void bindingRequiresStudentRole()
    {
        StuStudent input = student(null, "高一", "1班", 30L);
        when(sysUserMapper.selectUserById(30L)).thenReturn(activeUser(30L));
        when(sysRoleMapper.selectRolePermissionByUserId(30L)).thenReturn(List.of(role("teacher")));

        ServiceException ex = assertThrows(ServiceException.class, () -> studentService.insertStuStudent(input));
        assertEquals("请先为该账号分配“学生”角色", ex.getMessage());
    }

    @Test
    void bindingRejectsAccountAlreadyBoundToAnotherStudent()
    {
        StuStudent input = student(null, "高一", "1班", 30L);
        when(sysUserMapper.selectUserById(30L)).thenReturn(activeUser(30L));
        when(sysRoleMapper.selectRolePermissionByUserId(30L)).thenReturn(List.of(role("student")));
        when(stuStudentMapper.selectStuStudentByUserId(30L)).thenReturn(student(5L, "高一", "2班", 30L));

        ServiceException ex = assertThrows(ServiceException.class, () -> studentService.insertStuStudent(input));
        assertEquals("该账号已绑定学生「学生5」", ex.getMessage());
    }

    @Test
    void bindingAllowsSameStudentWhenUpdatingSelf()
    {
        StuStudent input = student(5L, "高一", "2班", 30L);
        when(sysUserMapper.selectUserById(30L)).thenReturn(activeUser(30L));
        when(sysRoleMapper.selectRolePermissionByUserId(30L)).thenReturn(List.of(role("student")));
        when(stuStudentMapper.selectStuStudentByUserId(30L)).thenReturn(student(5L, "高一", "2班", 30L));
        when(stuStudentMapper.selectStuStudentById(5L)).thenReturn(student(5L, "高一", "2班", 30L));

        studentService.updateStuStudent(input);
        verify(stuStudentMapper).updateStuStudent(input);
    }

    @Test
    void bindingRejectsDisabledAccount()
    {
        StuStudent input = student(null, "高一", "1班", 30L);
        var disabled = activeUser(30L);
        disabled.setStatus("1");
        when(sysUserMapper.selectUserById(30L)).thenReturn(disabled);

        ServiceException ex = assertThrows(ServiceException.class, () -> studentService.insertStuStudent(input));
        assertEquals("所选账号已停用，不能绑定", ex.getMessage());
    }

    @Test
    void studentWithScoresCannotBeDeleted()
    {
        when(stuStudentMapper.selectStuStudentById(3L)).thenReturn(student(3L, "高一", "1班", null));
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of(new StuScore()));

        ServiceException ex = assertThrows(ServiceException.class, () -> studentService.deleteStuStudentById(3L));
        assertEquals("学生「学生3」已有成绩记录，不能删除", ex.getMessage());
        verify(stuStudentMapper, never()).deleteStuStudentById(anyLong());
    }

    @Test
    void studentWithReviewsCannotBeDeleted()
    {
        when(stuStudentMapper.selectStuStudentById(3L)).thenReturn(student(3L, "高一", "1班", null));
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of());
        when(stuReviewMapper.selectStuReviewList(any())).thenReturn(List.of(new StuReview()));

        ServiceException ex = assertThrows(ServiceException.class, () -> studentService.deleteStuStudentById(3L));
        assertEquals("学生「学生3」已有查卷记录，不能删除", ex.getMessage());
    }

    @Test
    void studentWithoutRecordsCanBeDeleted()
    {
        when(stuStudentMapper.selectStuStudentById(3L)).thenReturn(student(3L, "高一", "1班", null));
        when(stuScoreMapper.selectStuScoreList(any())).thenReturn(List.of());
        when(stuReviewMapper.selectStuReviewList(any())).thenReturn(List.of());

        studentService.deleteStuStudentById(3L);
        verify(stuStudentMapper).deleteStuStudentById(3L);
    }

    @Test
    void checkUserNotBoundBlocksBoundAccount()
    {
        when(stuStudentMapper.selectStuStudentByUserId(30L)).thenReturn(student(5L, "高一", "2班", 30L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> studentService.checkUserNotBound(new Long[] { 30L }));
        assertEquals("该账号已绑定学生「学生5」，请先在学生信息中解除绑定", ex.getMessage());
    }

    @Test
    void teacherCannotReadStudentOutsideClass()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L,
                Set.of(StuScope.classKey("高一", "1班"))));
        when(stuStudentMapper.selectStuStudentById(4L)).thenReturn(student(4L, "高一", "2班", null));

        assertNull(studentService.selectStuStudentById(4L));
    }

    @Test
    void studentListIsScopedByPassingParams()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.student(9L, 3L));
        StuStudent query = new StuStudent();
        studentService.selectStuStudentList(query);
        assertEquals("STUDENT", query.getParams().get("scopeMode"));
        assertEquals(9L, query.getParams().get("scopeUserId"));
    }
}
