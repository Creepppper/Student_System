package com.ruoyi.system.domain;

import java.math.BigDecimal;

/**
 * 排名结果（查询结果，不落库）。
 *
 * rank 为名次（同分并列，如 1、1、3）；rankValue 为排名依据的分数（总分或单科）。
 *
 * @author ruoyi
 */
public class StuScoreRank extends StuScoreDetail
{
    /** 名次 */
    private Integer rank;

    /** 排名依据的分数 */
    private BigDecimal rankValue;

    public Integer getRank()
    {
        return rank;
    }

    public void setRank(Integer rank)
    {
        this.rank = rank;
    }

    public BigDecimal getRankValue()
    {
        return rankValue;
    }

    public void setRankValue(BigDecimal rankValue)
    {
        this.rankValue = rankValue;
    }
}
