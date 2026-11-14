package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StuStudent;

/**
 * 学生信息Service接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface IStuStudentService 
{
    /**
     * 查询学生信息
     * 
     * @param id 学生信息主键
     * @return 学生信息
     */
    public StuStudent selectStuStudentById(Long id);

    /**
     * 查询学生信息列表
     * 
     * @param stuStudent 学生信息
     * @return 学生信息集合
     */
    public List<StuStudent> selectStuStudentList(StuStudent stuStudent);

    /**
     * 新增学生信息
     * 
     * @param stuStudent 学生信息
     * @return 结果
     */
    public int insertStuStudent(StuStudent stuStudent);

    /**
     * 修改学生信息
     * 
     * @param stuStudent 学生信息
     * @return 结果
     */
    public int updateStuStudent(StuStudent stuStudent);

    /**
     * 批量删除学生信息
     * 
     * @param ids 需要删除的学生信息主键集合
     * @return 结果
     */
    public int deleteStuStudentByIds(Long[] ids);

    /**
     * 删除学生信息信息
     * 
     * @param id 学生信息主键
     * @return 结果
     */
    public int deleteStuStudentById(Long id);

    /**
     * 批量导入学生（仅管理员）。学号已存在时：updateSupport 为 true 则更新，否则跳过。
     * 导入不设置账号绑定，绑定账号请在学生信息中单独设置。
     *
     * @param students 从 Excel 读取的学生列表
     * @param updateSupport 学号已存在时是否更新
     * @return 导入结果说明（成功、更新、跳过、失败的数量及原因）
     */
    public String importStudents(List<StuStudent> students, boolean updateSupport);

    /**
     * 检查系统账号是否已绑定学生；已绑定时抛出异常，用于删除系统用户前的引用保护
     *
     * @param userIds 系统用户ID
     */
    public void checkUserNotBound(Long[] userIds);
}
