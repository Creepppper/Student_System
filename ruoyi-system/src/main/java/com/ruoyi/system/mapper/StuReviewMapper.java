package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.StuReview;

/**
 * 查卷申请Mapper接口
 *
 * @author ruoyi
 * @date 2026-10-31
 */
public interface StuReviewMapper
{
    /**
     * 按条件更新状态：仅当当前状态仍为 fromStatus 时才会更新，用于防止并发覆盖
     *
     * @return 影响行数，0 表示状态已被其他操作修改
     */
    public int updateStatusIfCurrent(@Param("id") Long id, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("reply") String reply);

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
     * 删除查卷申请
     * 
     * @param id 查卷申请主键
     * @return 结果
     */
    public int deleteStuReviewById(Long id);

    /**
     * 批量删除查卷申请
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStuReviewByIds(Long[] ids);
}
