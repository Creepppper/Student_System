package com.ruoyi.system.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuClassTeacherMapper;
import com.ruoyi.system.mapper.StuStudentMapper;

@Service
public class StuMyProfileService
{
    @Autowired private IStuScopeService scopeService;
    @Autowired private StuStudentMapper studentMapper;
    @Autowired private StuClassTeacherMapper classTeacherMapper;

    public Map<String, Object> getMyProfile()
    {
        StuScope scope = scopeService.currentScope();
        if (scope.getMode() != StuScope.Mode.STUDENT)
        {
            throw new ServiceException("当前账号不是学生账号");
        }
        if (scope.getStudentId() == null)
        {
            return null;
        }
        StuStudent student = studentMapper.selectStuStudentByUserId(scope.getUserId());
        if (student == null || !scope.getStudentId().equals(student.getId()))
        {
            return null;
        }
        StuClassTeacher query = new StuClassTeacher();
        query.setGrade(student.getGrade());
        query.setClassName(student.getClassName());
        List<String> teachers = classTeacherMapper.selectStuClassTeacherList(query).stream()
                .map(StuClassTeacher::getNickName).filter(Objects::nonNull)
                .filter(name -> !name.isBlank()).distinct().collect(Collectors.toList());
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("studentNo", student.getStudentNo());
        profile.put("studentName", student.getStudentName());
        profile.put("grade", student.getGrade());
        profile.put("className", student.getClassName());
        profile.put("teachers", teachers);
        return profile;
    }
}
