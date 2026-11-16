package com.ruoyi.system.service.impl;

import static com.ruoyi.system.service.impl.StuTestFixtures.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;

/**
 * 学生 Excel 导入：逐行校验、学号重复处理、导入不绑定账号、仅管理员可导入
 */
@ExtendWith(MockitoExtension.class)
class StuStudentImportTest
{
    @Mock
    private StuStudentMapper stuStudentMapper;

    @Mock
    private IStuScopeService stuScopeService;

    @InjectMocks
    private StuStudentServiceImpl service;

    private StuStudent row(String no, String name, String grade, String cls)
    {
        StuStudent s = new StuStudent();
        s.setStudentNo(no);
        s.setStudentName(name);
        s.setGrade(grade);
        s.setClassName(cls);
        return s;
    }

    @Test
    void rejectsInvalidGradeAndClassDuringImport()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        String message = service.importStudents(List.of(row("1", "甲", "高四", "1班"), row("2", "乙", "高一", "13班")), false);
        assertTrue(message.contains("年级不存在"));
        assertTrue(message.contains("班级不存在"));
        verify(stuStudentMapper, never()).insertStuStudent(any());
    }

    @Test
    void nonAdminCannotImport()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.teacher(7L, java.util.Set.of()));
        assertThrows(ServiceException.class,
                () -> service.importStudents(List.of(row("1", "甲", "高一", "1班")), false));
        verify(stuStudentMapper, never()).insertStuStudent(any());
    }

    @Test
    void newRowsAreInsertedAndExistingRowsSkippedWithoutUpdateFlag()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        when(stuStudentMapper.selectStuStudentList(any())).thenAnswer(inv -> {
            StuStudent query = inv.getArgument(0);
            if (!"20261101".equals(query.getStudentNo()))
            {
                return List.of();
            }
            StuStudent existing = student(1L, "高一", "1班", null);
            existing.setStudentNo("20261101");
            return List.of(existing);
        });
        // 学号 20261101 已存在，不更新则跳过
        StuStudent existing = row("20261101", "甲", "高一", "1班");
        StuStudent fresh = row("20261102", "乙", "高一", "1班");

        String message = service.importStudents(List.of(existing, fresh), false);

        verify(stuStudentMapper).insertStuStudent(fresh);
        verify(stuStudentMapper, never()).updateStuStudent(any());
        assertTrue(message.contains("新增 1 条"));
        assertTrue(message.contains("跳过 1 条"));
    }

    @Test
    void updateFlagUpdatesExistingAndKeepsBoundAccount()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        StuStudent current = student(9L, "高一", "1班", 30L);
        current.setStudentNo("20261101");
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of(current));

        StuStudent excel = row("20261101", "甲改", "高一", "2班");
        service.importStudents(List.of(excel), true);

        assertEquals(9L, excel.getId());
        assertEquals(30L, excel.getUserId(), "更新时保留已绑定的账号");
        verify(stuStudentMapper).updateStuStudent(excel);
    }

    @Test
    void importNeverBindsAccountFromSpreadsheet()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of());
        StuStudent withAccount = row("20261103", "丙", "高二", "1班");
        withAccount.setUserId(99L);

        service.importStudents(List.of(withAccount), false);

        assertNull(withAccount.getUserId());
        verify(stuStudentMapper).insertStuStudent(withAccount);
    }

    @Test
    void invalidAndDuplicateRowsAreReportedNotSaved()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        when(stuStudentMapper.selectStuStudentList(any())).thenReturn(List.of());

        String message = service.importStudents(List.of(
                row("20261201", "", "高一", "1班"),       // 姓名为空
                row("20261202", "丁", "高一", "1班"),
                row("20261202", "戊", "高一", "1班")),   // 文件内重复
                false);

        verify(stuStudentMapper, org.mockito.Mockito.times(1)).insertStuStudent(any());
        assertTrue(message.contains("学号、姓名、年级、班级名称均不能为空"));
        assertTrue(message.contains("文件中学号重复"));
        assertTrue(message.contains("失败 2 条"));
        assertFalse(message.isEmpty());
    }

    @Test
    void emptyFileIsRejected()
    {
        when(stuScopeService.currentScope()).thenReturn(StuScope.all(1L));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.importStudents(List.of(), false));
        assertEquals("导入文件中没有数据", ex.getMessage());
    }
}
