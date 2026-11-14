package com.ruoyi.system.domain;

import com.ruoyi.common.exception.ServiceException;

/**
 * 查卷申请状态，与 stu_review.status 及字典 stu_review_status 的字典值一致。
 *
 * @author ruoyi
 */
public enum StuReviewStatus
{
    PENDING("0", "待处理"),
    ACCEPTED("1", "已受理"),
    REJECTED("2", "已驳回"),
    COMPLETED("3", "已完成");

    private final String code;
    private final String label;

    StuReviewStatus(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    /**
     * 按数据库中的状态码查找状态
     *
     * @param code 状态码
     * @return 状态
     * @throws ServiceException 状态码不存在时
     */
    public static StuReviewStatus fromCode(String code)
    {
        for (StuReviewStatus status : values())
        {
            if (status.code.equals(code))
            {
                return status;
            }
        }
        throw new ServiceException("查卷申请状态异常：" + code);
    }
}
