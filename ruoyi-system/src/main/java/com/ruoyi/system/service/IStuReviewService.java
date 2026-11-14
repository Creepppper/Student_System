package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuReviewAction;

/**
 * 查卷申请Service接口
 *
 * @author ruoyi
 * @date 2026-10-31
 */
public interface IStuReviewService
{
    /**
     * 班主任处理查卷申请（受理、驳回、完成），按状态流转规则校验
     *
     * @param id 查卷申请主键
     * @param action 处理动作
     * @param reply 处理意见，驳回和完成时必填
     * @return 结果
     */
    public int handleStuReview(Long id, StuReviewAction action, String reply);

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
