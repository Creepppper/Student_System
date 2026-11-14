package com.ruoyi.system.domain;

import java.math.BigDecimal;

/**
 * 班级成绩统计（查询结果，不落库）：某次考试中一个班级的平均分。
 * 只统计六科均已录入的学生；count 为参与统计的人数。
 *
 * @author ruoyi
 */
public class StuClassStat
{
    /** 年级 */
    private String grade;

    /** 班级 */
    private String className;

    /** 参与统计人数 */
    private Integer count;

    /** 平均总分 */
    private BigDecimal avgTotal;

    /** 各科平均分（缺考计0分） */
    private BigDecimal avgChinese;
    private BigDecimal avgMath;
    private BigDecimal avgEnglish;
    private BigDecimal avgPhysics;
    private BigDecimal avgChemistry;
    private BigDecimal avgBiology;

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getAvgTotal() { return avgTotal; }
    public void setAvgTotal(BigDecimal avgTotal) { this.avgTotal = avgTotal; }
    public BigDecimal getAvgChinese() { return avgChinese; }
    public void setAvgChinese(BigDecimal avgChinese) { this.avgChinese = avgChinese; }
    public BigDecimal getAvgMath() { return avgMath; }
    public void setAvgMath(BigDecimal avgMath) { this.avgMath = avgMath; }
    public BigDecimal getAvgEnglish() { return avgEnglish; }
    public void setAvgEnglish(BigDecimal avgEnglish) { this.avgEnglish = avgEnglish; }
    public BigDecimal getAvgPhysics() { return avgPhysics; }
    public void setAvgPhysics(BigDecimal avgPhysics) { this.avgPhysics = avgPhysics; }
    public BigDecimal getAvgChemistry() { return avgChemistry; }
    public void setAvgChemistry(BigDecimal avgChemistry) { this.avgChemistry = avgChemistry; }
    public BigDecimal getAvgBiology() { return avgBiology; }
    public void setAvgBiology(BigDecimal avgBiology) { this.avgBiology = avgBiology; }
}
