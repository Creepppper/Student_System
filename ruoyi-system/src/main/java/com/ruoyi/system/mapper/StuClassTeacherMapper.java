package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.StuClassTeacher;

/**
 * 班主任负责班级Mapper接口
 *
 * @author ruoyi
 */
public interface StuClassTeacherMapper
{
    /** 查询负责班级列表（可按账号、年级、班级筛选） */
    public List<StuClassTeacher> selectStuClassTeacherList(StuClassTeacher query);

    /** 查询某个账号负责的全部班级 */
    public List<StuClassTeacher> selectStuClassTeacherByUserId(Long userId);

    /** 查询某个账号是否已负责某班级 */
    public StuClassTeacher selectStuClassTeacherByUserGradeClass(StuClassTeacher query);

    /** 查询指定年级班级已有的班主任分配。 */
    public StuClassTeacher selectStuClassTeacherByGradeClass(StuClassTeacher query);

    /** 删除账号时释放其班级分配。 */
    public int deleteStuClassTeacherByUserIds(Long[] userIds);

    /** 新增 */
    public int insertStuClassTeacher(StuClassTeacher stuClassTeacher);

    /** 批量删除 */
    public int deleteStuClassTeacherByIds(Long[] ids);

    /** 查询单条 */
    public StuClassTeacher selectStuClassTeacherById(Long id);
}
