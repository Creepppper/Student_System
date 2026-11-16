package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;
import com.ruoyi.system.service.StuMyProfileService;

@ExtendWith(MockitoExtension.class)
class StuMyProfileServiceTest
{
    @Mock IStuScopeService scopeService;
    @Mock StuStudentMapper studentMapper;
    @Mock StuClassTeacherMapper classTeacherMapper;
    @InjectMocks StuMyProfileService service;

    @Test void nonStudentIsRejected()
    {
        when(scopeService.currentScope()).thenReturn(StuScope.all(1L));
        assertThrows(ServiceException.class, service::getMyProfile);
        verifyNoInteractions(studentMapper, classTeacherMapper);
    }

    @Test void unboundStudentGetsNoProfile()
    {
        when(scopeService.currentScope()).thenReturn(StuScope.student(20L, null));
        assertNull(service.getMyProfile());
        verifyNoInteractions(studentMapper, classTeacherMapper);
    }

    @Test void profileUsesOnlyBoundStudentAndTheirGradeAndClass()
    {
        when(scopeService.currentScope()).thenReturn(StuScope.student(20L, 3L));
        StuStudent student = new StuStudent();
        student.setId(3L); student.setStudentNo("A01"); student.setStudentName("小明");
        student.setGrade("高二"); student.setClassName("3班");
        when(studentMapper.selectStuStudentByUserId(20L)).thenReturn(student);
        StuClassTeacher teacher = new StuClassTeacher(); teacher.setNickName("张老师");
        when(classTeacherMapper.selectStuClassTeacherList(any())).thenAnswer(invocation -> {
            StuClassTeacher query = invocation.getArgument(0);
            assertEquals("高二", query.getGrade()); assertEquals("3班", query.getClassName());
            return List.of(teacher);
        });
        Map<String, Object> profile = service.getMyProfile();
        assertEquals("A01", profile.get("studentNo"));
        assertEquals(List.of("张老师"), profile.get("teachers"));
        assertEquals(5, profile.size());
    }

    @Test void classWithoutTeacherReturnsEmptyList()
    {
        when(scopeService.currentScope()).thenReturn(StuScope.student(20L, 3L));
        StuStudent student = new StuStudent(); student.setId(3L); student.setGrade("高一"); student.setClassName("1班");
        when(studentMapper.selectStuStudentByUserId(20L)).thenReturn(student);
        when(classTeacherMapper.selectStuClassTeacherList(any())).thenReturn(List.of());
        assertEquals(List.of(), service.getMyProfile().get("teachers"));
    }
}
