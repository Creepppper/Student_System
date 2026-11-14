package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.StuExam;
import com.ruoyi.system.service.IStuExamService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 考试信息Controller
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/exam")
public class StuExamController extends BaseController
{
    @Autowired
    private IStuExamService stuExamService;

    /**
     * 查询考试信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(StuExam stuExam)
    {
        startPage();
        List<StuExam> list = stuExamService.selectStuExamList(stuExam);
        return getDataTable(list);
    }

    /**
     * 导出考试信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:exam:export')")
    @Log(title = "考试信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StuExam stuExam)
    {
        List<StuExam> list = stuExamService.selectStuExamList(stuExam);
        ExcelUtil<StuExam> util = new ExcelUtil<StuExam>(StuExam.class);
        util.exportExcel(response, list, "考试信息数据");
    }

    /**
     * 获取考试信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(stuExamService.selectStuExamById(id));
    }

    /**
     * 新增考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:add')")
    @Log(title = "考试信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StuExam stuExam)
    {
        return toAjax(stuExamService.insertStuExam(stuExam));
    }

    /**
     * 修改考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:edit')")
    @Log(title = "考试信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StuExam stuExam)
    {
        return toAjax(stuExamService.updateStuExam(stuExam));
    }

    /**
     * 删除考试信息
     */
    @PreAuthorize("@ss.hasPermi('system:exam:remove')")
    @Log(title = "考试信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stuExamService.deleteStuExamByIds(ids));
    }
}
