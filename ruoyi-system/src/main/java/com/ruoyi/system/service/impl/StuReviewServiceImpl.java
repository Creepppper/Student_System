package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.service.IStuReviewService;

/**
 * 查卷申请Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuReviewServiceImpl implements IStuReviewService 
{
    @Autowired
    private StuReviewMapper stuReviewMapper;

    /**
     * 查询查卷申请
     * 
     * @param id 查卷申请主键
     * @return 查卷申请
     */
    @Override
    public StuReview selectStuReviewById(Long id)
    {
        return stuReviewMapper.selectStuReviewById(id);
    }

    /**
     * 查询查卷申请列表
     * 
     * @param stuReview 查卷申请
     * @return 查卷申请
     */
    @Override
    public List<StuReview> selectStuReviewList(StuReview stuReview)
    {
        return stuReviewMapper.selectStuReviewList(stuReview);
    }

    /**
     * 新增查卷申请
     * 
     * @param stuReview 查卷申请
     * @return 结果
     */
    @Override
    public int insertStuReview(StuReview stuReview)
    {
        stuReview.setCreateTime(DateUtils.getNowDate());
        return stuReviewMapper.insertStuReview(stuReview);
    }

    /**
     * 修改查卷申请
     * 
     * @param stuReview 查卷申请
     * @return 结果
     */
    @Override
    public int updateStuReview(StuReview stuReview)
    {
        stuReview.setUpdateTime(DateUtils.getNowDate());
        return stuReviewMapper.updateStuReview(stuReview);
    }

    /**
     * 批量删除查卷申请
     * 
     * @param ids 需要删除的查卷申请主键
     * @return 结果
     */
    @Override
    public int deleteStuReviewByIds(Long[] ids)
    {
        return stuReviewMapper.deleteStuReviewByIds(ids);
    }

    /**
     * 删除查卷申请信息
     * 
     * @param id 查卷申请主键
     * @return 结果
     */
    @Override
    public int deleteStuReviewById(Long id)
    {
        return stuReviewMapper.deleteStuReviewById(id);
    }
}
