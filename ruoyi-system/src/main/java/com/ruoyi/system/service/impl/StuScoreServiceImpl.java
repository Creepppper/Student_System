package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuScoreDetail;
import com.ruoyi.system.domain.StuScoreRank;
import com.ruoyi.system.domain.StuScoreSubject;
import com.ruoyi.system.domain.StuTranscriptItem;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuScoreService;

/**
 * 学生成绩Service业务层处理
 *
 * 新增、修改时统一做以下校验（以后端为准，前端校验只是提示）：
 * 1. 学生、考试必须存在；同一学生同一考试只能有一条成绩；
 * 2. 每科成绩在 0 到满分之间，最多两位小数；NULL 表示未录入；
 * 3. 缺考科目代码只能是六科代码，去重后按固定顺序存储；缺考科目的成绩统一记为 0。
 *
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuScoreServiceImpl implements IStuScoreService
{
    @Autowired
    private StuScoreMapper stuScoreMapper;

    @Autowired
    private StuStudentMapper stuStudentMapper;

    @Autowired
    private StuExamMapper stuExamMapper;

    /**
     * 查询学生成绩
     *
     * @param id 学生成绩主键
     * @return 学生成绩
     */
    @Override
    public StuScore selectStuScoreById(Long id)
    {
        return stuScoreMapper.selectStuScoreById(id);
    }

    /**
     * 查询学生成绩列表
     *
     * @param stuScore 学生成绩
     * @return 学生成绩
     */
    @Override
    public List<StuScore> selectStuScoreList(StuScore stuScore)
    {
        return stuScoreMapper.selectStuScoreList(stuScore);
    }

    /**
     * 新增学生成绩
     *
     * @param stuScore 学生成绩
     * @return 结果
     */
    @Override
    public int insertStuScore(StuScore stuScore)
    {
        validateStuScore(stuScore, null);
        return stuScoreMapper.insertStuScore(stuScore);
    }

    /**
     * 修改学生成绩
     *
     * @param stuScore 学生成绩
     * @return 结果
     */
    @Override
    public int updateStuScore(StuScore stuScore)
    {
        if (stuScore.getId() == null)
        {
            throw new ServiceException("成绩ID不能为空");
        }
        validateStuScore(stuScore, stuScore.getId());
        return stuScoreMapper.updateStuScore(stuScore);
    }

    /**
     * 批量删除学生成绩
     *
     * @param ids 需要删除的学生成绩主键
     * @return 结果
     */
    @Override
    public int deleteStuScoreByIds(Long[] ids)
    {
        return stuScoreMapper.deleteStuScoreByIds(ids);
    }

    /**
     * 删除学生成绩信息
     *
     * @param id 学生成绩主键
     * @return 结果
     */
    @Override
    public int deleteStuScoreById(Long id)
    {
        return stuScoreMapper.deleteStuScoreById(id);
    }

    /**
     * 查询排名：只包含六科均已录入的学生；先按分组计算名次，再按筛选条件过滤
     */
    @Override
    public List<StuScoreRank> selectScoreRanking(Long examId, String scope, String subjectCode, String grade,
            String className)
    {
        if (examId == null)
        {
            throw new ServiceException("请选择考试");
        }
        if (grade == null || grade.isBlank())
        {
            throw new ServiceException("请选择年级");
        }
        boolean classScope = "CLASS".equals(scope);
        if (classScope && (className == null || className.isBlank()))
        {
            throw new ServiceException("班级排名需要选择班级");
        }
        if (!"GRADE".equals(scope) && !"CLASS".equals(scope))
        {
            throw new ServiceException("排名范围不正确");
        }
        StuScoreSubject subject = null;
        if (subjectCode != null && !subjectCode.isBlank() && !"TOTAL".equalsIgnoreCase(subjectCode))
        {
            subject = StuScoreSubject.fromCode(subjectCode.trim());
            if (subject == null)
            {
                throw new ServiceException("排名科目不正确");
            }
        }

        // 先按年级（班级排名时还包括班级）过滤再排名，保证不同年级、班级的学生不会放在同一个排名中
        final String selectedGrade = grade.trim();
        final String selectedClass = classScope ? className.trim() : null;
        List<StuScoreDetail> rows = new ArrayList<>();
        for (StuScoreDetail detail : loadExamDetails(examId))
        {
            if (!selectedGrade.equals(detail.getGrade()))
            {
                continue;
            }
            if (selectedClass != null && !selectedClass.equals(detail.getClassName()))
            {
                continue;
            }
            rows.add(detail);
        }
        Function<StuScoreDetail, String> groupKey = "GRADE".equals(scope)
                ? StuScoreRanker::gradeKey
                : StuScoreRanker::classKey;

        List<StuScoreRank> ranked = StuScoreRanker.rank(rows, groupKey, subject);
        List<StuScoreRank> result = new ArrayList<>();
        for (StuScoreRank item : ranked)
        {
            result.add(item);
        }
        return result;
    }

    /**
     * 查询成绩单：每次考试的成绩，附年级名次和班级名次（各次考试分别排名）
     */
    @Override
    public List<StuTranscriptItem> selectTranscript(Long studentId)
    {
        if (studentId == null)
        {
            throw new ServiceException("请选择学生");
        }
        List<StuScoreDetail> mine = stuScoreMapper.selectScoreDetailByStudent(studentId);
        Map<Long, Map<Long, Integer>> gradeRanks = new LinkedHashMap<>();
        Map<Long, Map<Long, Integer>> classRanks = new LinkedHashMap<>();

        List<StuTranscriptItem> result = new ArrayList<>();
        for (StuScoreDetail detail : mine)
        {
            prepare(detail);
            Long examId = detail.getExamId();
            if (!gradeRanks.containsKey(examId))
            {
                List<StuScoreDetail> examRows = loadExamDetails(examId);
                gradeRanks.put(examId, StuScoreRanker.rankByStudent(examRows, StuScoreRanker::gradeKey, null));
                classRanks.put(examId, StuScoreRanker.rankByStudent(examRows, StuScoreRanker::classKey, null));
            }
            StuTranscriptItem item = new StuTranscriptItem();
            copy(detail, item);
            if (detail.isComplete())
            {
                item.setGradeRank(gradeRanks.get(examId).get(studentId));
                item.setClassRank(classRanks.get(examId).get(studentId));
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 查询某次考试的全部成绩明细，并计算总分和完整性
     */
    private List<StuScoreDetail> loadExamDetails(Long examId)
    {
        List<StuScoreDetail> rows = stuScoreMapper.selectScoreDetailByExam(examId);
        rows.forEach(this::prepare);
        return rows;
    }

    private void prepare(StuScoreDetail detail)
    {
        detail.setTotal(StuScoreRanker.total(detail));
        detail.setComplete(detail.getTotal() != null);
    }

    private void copy(StuScoreDetail from, StuScoreDetail to)
    {
        to.setScoreId(from.getScoreId());
        to.setExamId(from.getExamId());
        to.setExamName(from.getExamName());
        to.setExamTime(from.getExamTime());
        to.setStudentId(from.getStudentId());
        to.setStudentNo(from.getStudentNo());
        to.setStudentName(from.getStudentName());
        to.setGrade(from.getGrade());
        to.setClassName(from.getClassName());
        to.setChinese(from.getChinese());
        to.setMath(from.getMath());
        to.setEnglish(from.getEnglish());
        to.setPhysics(from.getPhysics());
        to.setChemistry(from.getChemistry());
        to.setBiology(from.getBiology());
        to.setAbsentSubjects(from.getAbsentSubjects());
        to.setTotal(from.getTotal());
        to.setComplete(from.isComplete());
    }

    /**
     * 校验成绩并规范化缺考科目代码（会直接修改传入对象）
     *
     * @param stuScore 待保存的成绩
     * @param currentId 当前记录ID，新增时为 null；用于重复检查时排除自身
     */
    private void validateStuScore(StuScore stuScore, Long currentId)
    {
        if (stuScore.getStudentId() == null)
        {
            throw new ServiceException("请选择学生");
        }
        if (stuScore.getExamId() == null)
        {
            throw new ServiceException("请选择考试");
        }

        StuStudent student = stuStudentMapper.selectStuStudentById(stuScore.getStudentId());
        if (student == null)
        {
            throw new ServiceException("所选学生不存在");
        }
        StuExam exam = stuExamMapper.selectStuExamById(stuScore.getExamId());
        if (exam == null)
        {
            throw new ServiceException("所选考试不存在");
        }

        StuScore query = new StuScore();
        query.setStudentId(stuScore.getStudentId());
        query.setExamId(stuScore.getExamId());
        for (StuScore exists : stuScoreMapper.selectStuScoreList(query))
        {
            if (!Objects.equals(exists.getId(), currentId))
            {
                throw new ServiceException("该学生在此考试中已有成绩记录，请直接修改");
            }
        }

        Set<StuScoreSubject> absent = parseAbsentSubjects(stuScore.getAbsentSubjects());
        stuScore.setAbsentSubjects(absent.stream()
                .map(StuScoreSubject::getCode)
                .collect(Collectors.joining(StuScoreSubject.SEPARATOR)));

        for (StuScoreSubject subject : StuScoreSubject.values())
        {
            BigDecimal value = getScore(stuScore, subject);
            if (absent.contains(subject))
            {
                if (value != null && value.signum() != 0)
                {
                    throw new ServiceException(subject.getLabel() + "为缺考科目，成绩必须为0或留空");
                }
                // 缺考按约定记为 0
                setScore(stuScore, subject, BigDecimal.ZERO);
                continue;
            }
            if (value == null)
            {
                continue;
            }
            if (value.signum() < 0 || value.compareTo(subject.getFullScore()) > 0)
            {
                throw new ServiceException(subject.getLabel() + "成绩必须在0到"
                        + subject.getFullScore().stripTrailingZeros().toPlainString() + "之间");
            }
            if (value.stripTrailingZeros().scale() > 2)
            {
                throw new ServiceException(subject.getLabel() + "成绩最多保留两位小数");
            }
        }
    }

    /**
     * 解析缺考科目代码，返回按科目顺序排列的集合
     *
     * @param raw 原始字符串，如 "physics,chinese"，允许空格，空串表示无缺考
     */
    private Set<StuScoreSubject> parseAbsentSubjects(String raw)
    {
        Set<StuScoreSubject> result = EnumSet.noneOf(StuScoreSubject.class);
        if (raw == null || raw.isBlank())
        {
            return result;
        }
        for (String token : raw.split(StuScoreSubject.SEPARATOR, -1))
        {
            StuScoreSubject subject = StuScoreSubject.fromCode(token.trim());
            if (subject == null)
            {
                throw new ServiceException("缺考科目代码不正确：" + token.trim());
            }
            result.add(subject);
        }
        return result;
    }

    private BigDecimal getScore(StuScore stuScore, StuScoreSubject subject)
    {
        switch (subject)
        {
            case CHINESE: return stuScore.getChinese();
            case MATH: return stuScore.getMath();
            case ENGLISH: return stuScore.getEnglish();
            case PHYSICS: return stuScore.getPhysics();
            case CHEMISTRY: return stuScore.getChemistry();
            case BIOLOGY: return stuScore.getBiology();
            default: throw new IllegalArgumentException("未知科目：" + subject);
        }
    }

    private void setScore(StuScore stuScore, StuScoreSubject subject, BigDecimal value)
    {
        switch (subject)
        {
            case CHINESE: stuScore.setChinese(value); break;
            case MATH: stuScore.setMath(value); break;
            case ENGLISH: stuScore.setEnglish(value); break;
            case PHYSICS: stuScore.setPhysics(value); break;
            case CHEMISTRY: stuScore.setChemistry(value); break;
            case BIOLOGY: stuScore.setBiology(value); break;
            default: throw new IllegalArgumentException("未知科目：" + subject);
        }
    }
}
