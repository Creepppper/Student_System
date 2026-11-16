package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuPrediction;
import com.ruoyi.system.domain.StuScope;
import com.ruoyi.system.domain.StuScoreDetail;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScopeService;

@Service
public class StuPredictionService
{
    private final IStuScopeService scopeService;
    private final StuStudentMapper studentMapper;
    private final StuScoreMapper scoreMapper;

    public StuPredictionService(IStuScopeService scopeService, StuStudentMapper studentMapper, StuScoreMapper scoreMapper)
    {
        this.scopeService = scopeService;
        this.studentMapper = studentMapper;
        this.scoreMapper = scoreMapper;
    }

    /** 仅返回当前学生；其他学生的数据只用于计算校内名次。 */
    public StuPrediction myPrediction()
    {
        StuScope scope = scopeService.currentScope();
        if (scope.getMode() != StuScope.Mode.STUDENT)
        {
            throw new ServiceException("仅学生账号可以查看高考预估");
        }
        if (scope.getStudentId() == null)
        {
            throw new ServiceException("当前账号未绑定学生，请联系管理员");
        }
        StuStudent student = studentMapper.selectStuStudentById(scope.getStudentId());
        if (student == null || student.getGrade() == null || student.getClassName() == null)
        {
            throw new ServiceException("学生年级或班级信息不完整，请联系管理员");
        }
        List<StuPrediction> predictions = scoreMapper.selectPredictionByGrade(student.getGrade());
        List<StuScoreDetail> rows = new ArrayList<>(predictions);
        for (StuPrediction prediction : predictions)
        {
            if (scope.getStudentId().equals(prediction.getStudentId()))
            {
                prediction.setTotal(StuScoreRanker.total(prediction));
                prediction.setGradeRank(StuScoreRanker.rankByStudent(rows, StuScoreRanker::gradeKey, null).get(scope.getStudentId()));
                prediction.setClassRank(StuScoreRanker.rankByStudent(rows, StuScoreRanker::classKey, null).get(scope.getStudentId()));
                return prediction;
            }
        }
        return null;
    }
}
