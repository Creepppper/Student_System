package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StuClassTeacher;

/**
 * 班主任负责班级Service接口
 *
 * @author ruoyi
 */
public interface IStuClassTeacherService
{
    /** 查询负责班级列表 */
    public List<StuClassTeacher> selectStuClassTeacherList(StuClassTeacher query);

    /** 分配负责班级（校验账号角色、班级存在、不重复） */
    public int insertStuClassTeacher(StuClassTeacher stuClassTeacher);

    /** 取消负责班级 */
    public int deleteStuClassTeacherByIds(Long[] ids);
}
