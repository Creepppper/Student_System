package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuStudent;
import com.ruoyi.system.domain.SchoolClassNames;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IStuScopeService;
import com.ruoyi.system.service.IStuStudentService;

/**
 * 学生信息Service业务层处理
 *
 * 1. 列表按当前用户的数据范围过滤（班主任只看负责班级，学生只看本人）；
 * 2. 绑定账号时校验：账号存在且启用、拥有“学生”角色、未被其他学生绑定；
 * 3. 有成绩或查卷记录的学生不能删除。
 *
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuStudentServiceImpl implements IStuStudentService
{
    @Autowired
    private StuStudentMapper stuStudentMapper;

    @Autowired
    private StuScoreMapper stuScoreMapper;

    @Autowired
    private StuReviewMapper stuReviewMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private IStuScopeService stuScopeService;

    /**
     * 查询学生信息（超出数据范围时返回 null）
     *
     * @param id 学生信息主键
     * @return 学生信息
     */
    @Override
    public StuStudent selectStuStudentById(Long id)
    {
        StuStudent student = stuStudentMapper.selectStuStudentById(id);
        return stuScopeService.currentScope().allowsStudent(student) ? student : null;
    }

    /**
     * 查询学生信息列表（按数据范围过滤）
     *
     * @param stuStudent 学生信息
     * @return 学生信息集合
     */
    @Override
    public List<StuStudent> selectStuStudentList(StuStudent stuStudent)
    {
        stuScopeService.currentScope().applyTo(stuStudent.getParams());
        return stuStudentMapper.selectStuStudentList(stuStudent);
    }

    /**
     * 新增学生信息（仅管理员）
     *
     * @param stuStudent 学生信息
     * @return 结果
     */
    @Override
    public int insertStuStudent(StuStudent stuStudent)
    {
        if (!stuScopeService.currentScope().isAll())
        {
            throw new ServiceException("没有权限新增学生");
        }
        SchoolClassNames.validate(stuStudent.getGrade(), stuStudent.getClassName());
        validateUserBinding(stuStudent);
        return stuStudentMapper.insertStuStudent(stuStudent);
    }

    /**
     * 修改学生信息
     *
     * @param stuStudent 学生信息
     * @return 结果
     */
    @Override
    public int updateStuStudent(StuStudent stuStudent)
    {
        assertAccessible(stuStudent.getId());
        SchoolClassNames.validate(stuStudent.getGrade(), stuStudent.getClassName());
        validateUserBinding(stuStudent);
        return stuStudentMapper.updateStuStudent(stuStudent);
    }

    /**
     * 批量删除学生信息
     *
     * @param ids 需要删除的学生信息主键
     * @return 结果
     */
    @Override
    public int deleteStuStudentByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            assertDeletable(id);
        }
        return stuStudentMapper.deleteStuStudentByIds(ids);
    }

    /**
     * 删除学生信息
     *
     * @param id 学生信息主键
     * @return 结果
     */
    @Override
    public int deleteStuStudentById(Long id)
    {
        assertDeletable(id);
        return stuStudentMapper.deleteStuStudentById(id);
    }

    /**
     * 批量导入学生：逐行校验并写入，单行失败不影响其他行，返回汇总说明
     */
    @Override
    public String importStudents(List<StuStudent> students, boolean updateSupport)
    {
        if (!stuScopeService.currentScope().isAll())
        {
            throw new ServiceException("没有权限导入学生");
        }
        if (students == null || students.isEmpty())
        {
            throw new ServiceException("导入文件中没有数据");
        }
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        List<String> errors = new java.util.ArrayList<>();
        java.util.Set<String> seenInFile = new java.util.HashSet<>();
        int row = 1;
        for (StuStudent student : students)
        {
            row++;
            String no = student.getStudentNo() == null ? "" : student.getStudentNo().trim();
            String label = "第" + row + "行（学号" + (no.isEmpty() ? "空" : no) + "）";
            if (no.isEmpty() || isBlank(student.getStudentName()) || isBlank(student.getGrade()) || isBlank(student.getClassName()))
            {
                errors.add(label + "：学号、姓名、年级、班级名称均不能为空");
                continue;
            }
            if (!seenInFile.add(no))
            {
                errors.add(label + "：文件中学号重复");
                continue;
            }
            student.setStudentNo(no);
            student.setStudentName(student.getStudentName().trim());
            try
            {
                SchoolClassNames.validate(student.getGrade(), student.getClassName());
            }
            catch (ServiceException ex)
            {
                errors.add(label + "：" + ex.getMessage());
                continue;
            }
            // 导入不绑定账号：Excel 中的账号ID不可信，统一清空
            student.setUserId(null);

            StuStudent query = new StuStudent();
            query.setStudentNo(no);
            List<StuStudent> exists = stuStudentMapper.selectStuStudentList(query);
            StuStudent current = null;
            for (StuStudent candidate : exists)
            {
                if (no.equals(candidate.getStudentNo()))
                {
                    current = candidate;
                    break;
                }
            }
            try
            {
                if (current == null)
                {
                    stuStudentMapper.insertStuStudent(student);
                    inserted++;
                }
                else if (updateSupport)
                {
                    student.setId(current.getId());
                    student.setUserId(current.getUserId());
                    stuStudentMapper.updateStuStudent(student);
                    updated++;
                }
                else
                {
                    skipped++;
                }
            }
            catch (RuntimeException ex)
            {
                errors.add(label + "：保存失败");
            }
        }
        StringBuilder message = new StringBuilder();
        message.append("导入完成：新增 ").append(inserted).append(" 条，更新 ").append(updated)
                .append(" 条，跳过 ").append(skipped).append(" 条，失败 ").append(errors.size()).append(" 条。");
        for (String error : errors)
        {
            message.append("<br/>").append(error);
        }
        return message.toString();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 检查账号是否已绑定学生，用于删除系统用户前的引用保护
     *
     * @param userIds 系统用户ID
     */
    @Override
    public void checkUserNotBound(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            StuStudent bound = stuStudentMapper.selectStuStudentByUserId(userId);
            if (bound != null)
            {
                throw new ServiceException("该账号已绑定学生「" + bound.getStudentName() + "」，请先在学生信息中解除绑定");
            }
        }
    }

    private void assertAccessible(Long id)
    {
        StuStudent existing = stuStudentMapper.selectStuStudentById(id);
        if (existing == null)
        {
            throw new ServiceException("学生不存在");
        }
        if (!stuScopeService.currentScope().allowsStudent(existing))
        {
            throw new ServiceException("没有权限操作该学生");
        }
    }

    private void assertDeletable(Long id)
    {
        assertAccessible(id);
        StuStudent student = stuStudentMapper.selectStuStudentById(id);
        StuScore scoreQuery = new StuScore();
        scoreQuery.setStudentId(id);
        if (!stuScoreMapper.selectStuScoreList(scoreQuery).isEmpty())
        {
            throw new ServiceException("学生「" + student.getStudentName() + "」已有成绩记录，不能删除");
        }
        StuReview reviewQuery = new StuReview();
        reviewQuery.setStudentId(id);
        if (!stuReviewMapper.selectStuReviewList(reviewQuery).isEmpty())
        {
            throw new ServiceException("学生「" + student.getStudentName() + "」已有查卷记录，不能删除");
        }
    }

    /**
     * 校验绑定的系统账号：存在、启用、拥有学生角色，且未被其他学生绑定
     */
    private void validateUserBinding(StuStudent stuStudent)
    {
        Long userId = stuStudent.getUserId();
        if (userId == null)
        {
            return;
        }
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("所选账号不存在");
        }
        if (!"0".equals(user.getStatus()))
        {
            throw new ServiceException("所选账号已停用，不能绑定");
        }
        boolean isStudent = false;
        for (SysRole role : sysRoleMapper.selectRolePermissionByUserId(userId))
        {
            if (StuScopeServiceImpl.ROLE_STUDENT.equals(role.getRoleKey()))
            {
                isStudent = true;
                break;
            }
        }
        if (!isStudent)
        {
            throw new ServiceException("请先为该账号分配“学生”角色");
        }
        StuStudent bound = stuStudentMapper.selectStuStudentByUserId(userId);
        if (bound != null && !bound.getId().equals(stuStudent.getId()))
        {
            throw new ServiceException("该账号已绑定学生「" + bound.getStudentName() + "」");
        }
    }
}
