package com.ruoyi.system.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * 班主任对查卷申请的处理动作，以及允许的状态流转。
 *
 * 状态流转规则（默认值，修改时只需改这里并同步测试）：
 * <pre>
 *   待处理 --受理--> 已受理 --完成--> 已完成
 *   待处理 --驳回--> 已驳回
 *   已受理 --驳回--> 已驳回
 * </pre>
 * 已驳回、已完成为终态。驳回和完成必须填写处理意见。
 *
 * @author ruoyi
 */
public enum StuReviewAction
{
    ACCEPT("受理", EnumSet.of(StuReviewStatus.PENDING), StuReviewStatus.ACCEPTED, false),
    REJECT("驳回", EnumSet.of(StuReviewStatus.PENDING, StuReviewStatus.ACCEPTED), StuReviewStatus.REJECTED, true),
    COMPLETE("完成", EnumSet.of(StuReviewStatus.ACCEPTED), StuReviewStatus.COMPLETED, true);

    private final String label;
    private final Set<StuReviewStatus> allowedFrom;
    private final StuReviewStatus target;
    private final boolean replyRequired;

    StuReviewAction(String label, Set<StuReviewStatus> allowedFrom, StuReviewStatus target, boolean replyRequired)
    {
        this.label = label;
        this.allowedFrom = allowedFrom;
        this.target = target;
        this.replyRequired = replyRequired;
    }

    public String getLabel()
    {
        return label;
    }

    public StuReviewStatus getTarget()
    {
        return target;
    }

    public boolean isReplyRequired()
    {
        return replyRequired;
    }

    /**
     * 判断当前状态是否允许执行本动作
     */
    public boolean canApplyTo(StuReviewStatus current)
    {
        return allowedFrom.contains(current);
    }
}
