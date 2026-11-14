package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.StuClassTeacher;
import com.ruoyi.system.service.IStuClassTeacherService;

/**
 * 班主任负责班级Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/classteacher")
public class StuClassTeacherController extends BaseController
{
    @Autowired
    private IStuClassTeacherService stuClassTeacherService;

    /**
     * 查询班主任负责班级列表
     */
    @PreAuthorize("@ss.hasPermi('system:classteacher:list')")
    @GetMapping("/list")
    public TableDataInfo list(StuClassTeacher stuClassTeacher)
    {
        startPage();
        List<StuClassTeacher> list = stuClassTeacherService.selectStuClassTeacherList(stuClassTeacher);
        return getDataTable(list);
    }

    /**
     * 分配班主任负责的班级
     */
    @PreAuthorize("@ss.hasPermi('system:classteacher:add')")
    @Log(title = "班主任负责班级", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StuClassTeacher stuClassTeacher)
    {
        return toAjax(stuClassTeacherService.insertStuClassTeacher(stuClassTeacher));
    }

    /**
     * 取消班主任负责的班级
     */
    @PreAuthorize("@ss.hasPermi('system:classteacher:remove')")
    @Log(title = "班主任负责班级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stuClassTeacherService.deleteStuClassTeacherByIds(ids));
    }
}
