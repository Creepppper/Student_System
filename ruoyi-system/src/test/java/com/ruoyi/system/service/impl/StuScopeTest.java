package com.ruoyi.system.service.impl;

import static com.ruoyi.system.service.impl.StuTestFixtures.login;
import static com.ruoyi.system.service.impl.StuTestFixtures.logout;
import static com.ruoyi.system.service.impl.StuTestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;

/**
 * 数据范围：范围判断，以及按当前登录账号的角色解析范围
 */
@ExtendWith(MockitoExtension.class)
class StuScopeTest
{
    @Mock
    private StuClassTeacherMapper stuClassTeacherMapper;

    @Mock
    private StuStudentMapper stuStudentMapper;

    @InjectMocks
    private StuScopeServiceImpl scopeService;

    @AfterEach
    void tearDown()
    {
        logout();
    }

    @Test
    void teacherScopeOnlyAllowsOwnClasses()
    {
        StuScope scope = StuScope.teacher(7L, Set.of(StuScope.classKey("高一", "1班")));
        assertTrue(scope.allowsClass("高一", "1班"));
        assertFalse(scope.allowsClass("高一", "2班"));
        assertFalse(scope.allowsClass("高二", "1班"), "同名班级不同年级不能混用");
        assertTrue(scope.allowsStudent(student(1L, "高一", "1班", null)));
        assertFalse(scope.allowsStudent(student(2L, "高一", "2班", null)));
    }

    @Test
    void studentScopeOnlyAllowsOwnRecord()
    {
        StuScope scope = StuScope.student(9L, 3L);
        assertTrue(scope.allowsStudent(student(3L, "高一", "1班", 9L)));
        assertFalse(scope.allowsStudent(student(4L, "高一", "1班", 10L)));
        assertFalse(StuScope.student(9L, null).allowsStudent(student(3L, "高一", "1班", 9L)));
    }

    @Test
    void noneScopeDeniesEverything()
    {
        StuScope scope = StuScope.none(5L);
        assertFalse(scope.allowsClass("高一", "1班"));
        assertFalse(scope.allowsStudent(student(1L, "高一", "1班", null)));
    }

    @Test
    void allScopeAllowsEverythingIncludingMissingRecord()
    {
        StuScope scope = StuScope.all(1L);
        assertTrue(scope.allowsStudent(student(1L, "高一", "1班", null)));
        assertTrue(scope.allowsStudent(null), "管理员对遗留的无效引用也要能访问");
    }

    @Test
    void applyToWritesModeAndUser()
    {
        Map<String, Object> params = new HashMap<>();
        StuScope.student(9L, 3L).applyTo(params);
        assertEquals("STUDENT", params.get("scopeMode"));
        assertEquals(9L, params.get("scopeUserId"));
    }

    @Test
    void adminGetsAllScope()
    {
        login(1L);
        assertEquals(StuScope.Mode.ALL, scopeService.currentScope().getMode());
    }

    @Test
    void teacherGetsOwnClassesFromMapping()
    {
        login(7L, "teacher");
        StuClassTeacher mapping = new StuClassTeacher();
        mapping.setGrade("高一");
        mapping.setClassName("1班");
        when(stuClassTeacherMapper.selectStuClassTeacherByUserId(7L)).thenReturn(List.of(mapping));

        StuScope scope = scopeService.currentScope();
        assertEquals(StuScope.Mode.TEACHER, scope.getMode());
        assertTrue(scope.allowsClass("高一", "1班"));
        assertFalse(scope.allowsClass("高一", "2班"));
    }

    @Test
    void studentGetsBoundRecord()
    {
        login(9L, "student");
        when(stuStudentMapper.selectStuStudentByUserId(9L)).thenReturn(student(3L, "高一", "1班", 9L));

        StuScope scope = scopeService.currentScope();
        assertEquals(StuScope.Mode.STUDENT, scope.getMode());
        assertEquals(3L, scope.getStudentId());
    }

    @Test
    void unboundStudentHasNoRecord()
    {
        login(9L, "student");
        when(stuStudentMapper.selectStuStudentByUserId(9L)).thenReturn((StuStudent) null);
        assertNull(scopeService.currentScope().getStudentId());
    }

    @Test
    void userWithoutBusinessRoleGetsNone()
    {
        login(20L, "common");
        assertEquals(StuScope.Mode.NONE, scopeService.currentScope().getMode());
    }
}
