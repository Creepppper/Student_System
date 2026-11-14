package com.ruoyi.system.domain;

import java.math.BigDecimal;

/**
 * 学生成绩科目及满分定义。
 *
 * 科目代码与 stu_score.absent_subjects 字段保持一致，枚举声明顺序即为缺考代码的规范排序。
 *
 * @author ruoyi
 */
public enum StuScoreSubject
{
    CHINESE("chinese", "语文", 150),
    MATH("math", "数学", 150),
    ENGLISH("english", "英语", 150),
    PHYSICS("physics", "物理", 100),
    CHEMISTRY("chemistry", "化学", 100),
    BIOLOGY("biology", "生物", 100);

    /** 缺考科目代码之间的分隔符（不含空格，便于 FIND_IN_SET 查询） */
    public static final String SEPARATOR = ",";

    private final String code;
    private final String label;
    private final BigDecimal fullScore;

    StuScoreSubject(String code, String label, int fullScore)
    {
        this.code = code;
        this.label = label;
        this.fullScore = BigDecimal.valueOf(fullScore);
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public BigDecimal getFullScore()
    {
        return fullScore;
    }

    /**
     * 按代码查找科目
     *
     * @param code 科目代码（大小写不敏感）
     * @return 科目，找不到时返回 null
     */
    public static StuScoreSubject fromCode(String code)
    {
        for (StuScoreSubject subject : values())
        {
            if (subject.code.equalsIgnoreCase(code))
            {
                return subject;
            }
        }
        return null;
    }
}
