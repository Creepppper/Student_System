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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.service.IStuScoreService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学生成绩Controller
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@RestController
@RequestMapping("/system/score")
public class StuScoreController extends BaseController
{
    @Autowired
    private IStuScoreService stuScoreService;

    /**
     * 查询学生成绩列表
     */
    @PreAuthorize("@ss.hasPermi('system:score:list')")
    @GetMapping("/list")
    public TableDataInfo list(StuScore stuScore)
    {
        startPage();
        List<StuScore> list = stuScoreService.selectStuScoreList(stuScore);
        return getDataTable(list);
    }

    /**
     * 查询考试排名（年级排名或班级排名，按总分或单科）
     */
    @PreAuthorize("@ss.hasPermi('system:score:stat')")
    @GetMapping("/ranking")
    public AjaxResult ranking(@RequestParam Long examId,
            @RequestParam String scope,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className)
    {
        return success(stuScoreService.selectScoreRanking(examId, scope, subject, grade, className));
    }

    /**
     * 查询学生成绩单（历次考试，含年级名次和班级名次）
     */
    @PreAuthorize("@ss.hasPermi('system:score:stat')")
    @GetMapping("/transcript/{studentId}")
    public AjaxResult transcript(@PathVariable("studentId") Long studentId)
    {
        return success(stuScoreService.selectTranscript(studentId));
    }

    /**
     * 导出学生成绩列表
     */
    @PreAuthorize("@ss.hasPermi('system:score:export')")
    @Log(title = "学生成绩", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StuScore stuScore)
    {
        List<StuScore> list = stuScoreService.selectStuScoreList(stuScore);
        ExcelUtil<StuScore> util = new ExcelUtil<StuScore>(StuScore.class);
        util.exportExcel(response, list, "学生成绩数据");
    }

    /**
     * 获取学生成绩详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:score:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(stuScoreService.selectStuScoreById(id));
    }

    /**
     * 新增学生成绩
     */
    @PreAuthorize("@ss.hasPermi('system:score:add')")
    @Log(title = "学生成绩", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StuScore stuScore)
    {
        return toAjax(stuScoreService.insertStuScore(stuScore));
    }

    /**
     * 修改学生成绩
     */
    @PreAuthorize("@ss.hasPermi('system:score:edit')")
    @Log(title = "学生成绩", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StuScore stuScore)
    {
        return toAjax(stuScoreService.updateStuScore(stuScore));
    }

    /**
     * 删除学生成绩
     */
    @PreAuthorize("@ss.hasPermi('system:score:remove')")
    @Log(title = "学生成绩", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stuScoreService.deleteStuScoreByIds(ids));
    }
}
