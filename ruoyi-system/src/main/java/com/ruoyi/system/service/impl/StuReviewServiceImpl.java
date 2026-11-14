package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuReviewAction;
import com.ruoyi.system.domain.StuReviewStatus;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.domain.StuScoreSubject;
import com.ruoyi.system.mapper.StuExamMapper;
import com.ruoyi.system.mapper.StuReviewMapper;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.mapper.StuStudentMapper;
import com.ruoyi.system.service.IStuReviewService;

/**
 * 查卷申请Service业务层处理
 *
 * 状态只能通过 {@link #handleStuReview} 修改：
 * 1. 新增时状态强制为“待处理”，忽略客户端传入的状态和处理意见；
 * 2. 通用修改只允许待处理的申请，且保留原状态和原处理意见；
 * 3. 状态流转按 {@link StuReviewAction} 校验，并通过条件更新防止并发覆盖。
 *
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuReviewServiceImpl implements IStuReviewService
{
    /** 处理意见字段长度上限，与 stu_review.reply varchar(500) 一致 */
    private static final int REPLY_MAX_LENGTH = 500;

    @Autowired
    private StuReviewMapper stuReviewMapper;

    @Autowired
    private StuStudentMapper stuStudentMapper;

    @Autowired
    private StuExamMapper stuExamMapper;

    @Autowired
    private StuScoreMapper stuScoreMapper;

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
     * 新增查卷申请（状态固定为待处理，不接受处理意见）
     *
     * @param stuReview 查卷申请
     * @return 结果
     */
    @Override
    public int insertStuReview(StuReview stuReview)
    {
        validateReference(stuReview);
        stuReview.setStatus(StuReviewStatus.PENDING.getCode());
        stuReview.setReply(null);
        stuReview.setCreateTime(DateUtils.getNowDate());
        return stuReviewMapper.insertStuReview(stuReview);
    }

    /**
     * 修改查卷申请：仅允许修改待处理的申请，状态和处理意见保持不变
     *
     * @param stuReview 查卷申请
     * @return 结果
     */
    @Override
    public int updateStuReview(StuReview stuReview)
    {
        StuReview existing = stuReviewMapper.selectStuReviewById(stuReview.getId());
        if (existing == null)
        {
            throw new ServiceException("查卷申请不存在");
        }
        if (!StuReviewStatus.PENDING.getCode().equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态为「" + StuReviewStatus.fromCode(existing.getStatus()).getLabel()
                    + "」，只有待处理的申请才能修改");
        }
        validateReference(stuReview);
        stuReview.setStatus(existing.getStatus());
        stuReview.setReply(existing.getReply());
        stuReview.setUpdateTime(DateUtils.getNowDate());
        return stuReviewMapper.updateStuReview(stuReview);
    }

    /**
     * 校验申请引用的学生、考试、科目，并规范化科目代码（会直接修改传入对象）。
     * 规则：学生和考试必须存在；科目必须是六科之一；该学生在该考试中必须有成绩记录，
     * 否则没有可查的卷子。
     */
    private void validateReference(StuReview review)
    {
        if (review.getStudentId() == null)
        {
            throw new ServiceException("请选择申请学生");
        }
        if (review.getExamId() == null)
        {
            throw new ServiceException("请选择考试");
        }
        if (review.getReason() == null || review.getReason().trim().isEmpty())
        {
            throw new ServiceException("申请理由不能为空");
        }
        if (stuStudentMapper.selectStuStudentById(review.getStudentId()) == null)
        {
            throw new ServiceException("所选学生不存在");
        }
        if (stuExamMapper.selectStuExamById(review.getExamId()) == null)
        {
            throw new ServiceException("所选考试不存在");
        }
        String rawSubject = review.getSubject() == null ? null : review.getSubject().trim();
        StuScoreSubject subject = StuScoreSubject.fromCode(rawSubject);
        if (subject == null)
        {
            throw new ServiceException("查卷科目不正确，请选择语文、数学、英语、物理、化学、生物之一");
        }
        review.setSubject(subject.getCode());

        StuScore query = new StuScore();
        query.setStudentId(review.getStudentId());
        query.setExamId(review.getExamId());
        if (stuScoreMapper.selectStuScoreList(query).isEmpty())
        {
            throw new ServiceException("该学生在此考试中没有成绩记录，不能申请查卷");
        }
    }

    /**
     * 班主任处理查卷申请
     *
     * @param id 查卷申请主键
     * @param action 处理动作
     * @param reply 处理意见
     * @return 结果
     */
    @Override
    public int handleStuReview(Long id, StuReviewAction action, String reply)
    {
        if (id == null)
        {
            throw new ServiceException("查卷申请ID不能为空");
        }
        if (action == null)
        {
            throw new ServiceException("请选择处理动作");
        }

        StuReview review = stuReviewMapper.selectStuReviewById(id);
        if (review == null)
        {
            throw new ServiceException("查卷申请不存在");
        }
        StuReviewStatus current = StuReviewStatus.fromCode(review.getStatus());
        if (!action.canApplyTo(current))
        {
            throw new ServiceException("当前状态为「" + current.getLabel() + "」，不能执行「" + action.getLabel() + "」");
        }

        String trimmed = reply == null ? null : reply.trim();
        if (trimmed != null && trimmed.isEmpty())
        {
            trimmed = null;
        }
        if (action.isReplyRequired() && trimmed == null)
        {
            throw new ServiceException("执行「" + action.getLabel() + "」时必须填写处理意见");
        }
        if (trimmed != null && trimmed.length() > REPLY_MAX_LENGTH)
        {
            throw new ServiceException("处理意见不能超过" + REPLY_MAX_LENGTH + "个字");
        }
        // 受理时可不填意见，此时保留原有意见
        String finalReply = trimmed != null ? trimmed : review.getReply();

        int rows = stuReviewMapper.updateStatusIfCurrent(id, current.getCode(),
                action.getTarget().getCode(), finalReply);
        if (rows == 0)
        {
            throw new ServiceException("申请状态已被他人修改，请刷新后重试");
        }
        return rows;
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
