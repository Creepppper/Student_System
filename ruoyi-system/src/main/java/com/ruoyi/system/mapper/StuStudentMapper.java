package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.StuStudent;

/**
 * 学生信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface StuStudentMapper 
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
     * 删除学生信息
     * 
     * @param id 学生信息主键
     * @return 结果
     */
    public int deleteStuStudentById(Long id);

    /**
     * 批量删除学生信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStuStudentByIds(Long[] ids);

    /**
     * 按绑定的若依用户ID查询学生（一个账号最多绑定一个学生）
     *
     * @param userId 若依用户ID
     * @return 学生，未绑定时为 null
     */
    public StuStudent selectStuStudentByUserId(Long userId);
}
