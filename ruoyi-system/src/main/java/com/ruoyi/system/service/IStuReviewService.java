package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StuReview;

/**
 * 查卷申请Service接口
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
public interface IStuReviewService 
{
    /**
     * 查询查卷申请
     * 
     * @param id 查卷申请主键
     * @return 查卷申请
     */
    public StuReview selectStuReviewById(Long id);

    /**
     * 查询查卷申请列表
     * 
     * @param stuReview 查卷申请
     * @return 查卷申请集合
     */
    public List<StuReview> selectStuReviewList(StuReview stuReview);

    /**
     * 新增查卷申请
     * 
     * @param stuReview 查卷申请
     * @return 结果
     */
    public int insertStuReview(StuReview stuReview);

    /**
     * 修改查卷申请
     * 
     * @param stuReview 查卷申请
     * @return 结果
     */
    public int updateStuReview(StuReview stuReview);

    /**
     * 批量删除查卷申请
     * 
     * @param ids 需要删除的查卷申请主键集合
     * @return 结果
     */
    public int deleteStuReviewByIds(Long[] ids);

    /**
     * 删除查卷申请信息
     * 
     * @param id 查卷申请主键
     * @return 结果
     */
    public int deleteStuReviewById(Long id);
}
