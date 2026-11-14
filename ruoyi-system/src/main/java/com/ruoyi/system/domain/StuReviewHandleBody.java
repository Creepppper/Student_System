package com.ruoyi.system.domain;

/**
 * 查卷申请处理请求体：PUT /system/review/{id}/handle
 *
 * @author ruoyi
 */
public class StuReviewHandleBody
{
    /** 处理动作：ACCEPT / REJECT / COMPLETE */
    private StuReviewAction action;

    /** 班主任处理意见，驳回和完成时必填 */
    private String reply;

    public StuReviewAction getAction()
    {
        return action;
    }

    public void setAction(StuReviewAction action)
    {
        this.action = action;
    }

    public String getReply()
    {
        return reply;
    }

    public void setReply(String reply)
    {
        this.reply = reply;
    }
}
