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
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuReviewHandleBody;
import com.ruoyi.system.service.IStuReviewService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 查卷申请Controller
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/review")
public class StuReviewController extends BaseController
{
    @Autowired
    private IStuReviewService stuReviewService;

    /**
     * 查询查卷申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:review:list')")
    @GetMapping("/list")
    public TableDataInfo list(StuReview stuReview)
    {
        startPage();
        List<StuReview> list = stuReviewService.selectStuReviewList(stuReview);
        return getDataTable(list);
    }

    /**
     * 导出查卷申请列表
     */
    @PreAuthorize("@ss.hasPermi('system:review:export')")
    @Log(title = "查卷申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StuReview stuReview)
    {
        List<StuReview> list = stuReviewService.selectStuReviewList(stuReview);
        ExcelUtil<StuReview> util = new ExcelUtil<StuReview>(StuReview.class);
        util.exportExcel(response, list, "查卷申请数据");
    }

    /**
     * 获取查卷申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:review:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(stuReviewService.selectStuReviewById(id));
    }

    /**
     * 新增查卷申请
     */
    @PreAuthorize("@ss.hasPermi('system:review:add')")
    @Log(title = "查卷申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StuReview stuReview)
    {
        return toAjax(stuReviewService.insertStuReview(stuReview));
    }

    /**
     * 修改查卷申请
     */
    @PreAuthorize("@ss.hasPermi('system:review:edit')")
    @Log(title = "查卷申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StuReview stuReview)
    {
        return toAjax(stuReviewService.updateStuReview(stuReview));
    }

    /**
     * 处理查卷申请（受理 / 驳回 / 完成）
     */
    @PreAuthorize("@ss.hasPermi('system:review:handle')")
    @Log(title = "查卷申请处理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/handle")
    public AjaxResult handle(@PathVariable("id") Long id, @RequestBody StuReviewHandleBody body)
    {
        return toAjax(stuReviewService.handleStuReview(id, body.getAction(), body.getReply()));
    }


    /**
     * 删除查卷申请
     */
    @PreAuthorize("@ss.hasPermi('system:review:remove')")
    @Log(title = "查卷申请", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stuReviewService.deleteStuReviewByIds(ids));
    }
}
