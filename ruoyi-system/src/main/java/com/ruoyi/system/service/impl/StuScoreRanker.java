package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import com.ruoyi.system.domain.StuScoreDetail;
import com.ruoyi.system.domain.StuScoreRank;
import com.ruoyi.system.domain.StuScoreSubject;

/**
 * 成绩排名计算（纯内存计算，不落库）。
 *
 * 规则：
 * 1. 只有六科均已录入的记录参与排名，未录入的记录不排名；
 * 2. 同分并列，名次跳过（如 1、1、3），即 RANK() 语义；
 * 3. 排名依据为总分（subject 为 null）或单科成绩。
 *
 * @author ruoyi
 */
final class StuScoreRanker
{
    private StuScoreRanker()
    {
    }

    /**
     * 计算总分；六科未全部录入时返回 null
     */
    static BigDecimal total(StuScoreDetail detail)
    {
        BigDecimal sum = BigDecimal.ZERO;
        for (StuScoreSubject subject : StuScoreSubject.values())
        {
            BigDecimal value = score(detail, subject);
            if (value == null)
            {
                return null;
            }
            sum = sum.add(value);
        }
        return sum;
    }

    /**
     * 六科是否均已录入（缺考科目按约定记为 0，视为已录入）
     */
    static boolean isComplete(StuScoreDetail detail)
    {
        return total(detail) != null;
    }

    /**
     * 按分组计算名次。
     *
     * @param rows 同一次考试的成绩明细（可包含不完整记录，会被忽略）
     * @param groupKey 分组规则，如年级、年级+班级
     * @param subject 排名科目，为 null 时按总分排名
     * @return 排名结果，按分组、名次、学号排序
     */
    static List<StuScoreRank> rank(List<StuScoreDetail> rows, Function<StuScoreDetail, String> groupKey,
            StuScoreSubject subject)
    {
        Map<String, List<StuScoreRank>> groups = new TreeMap<>();
        for (StuScoreDetail row : rows)
        {
            BigDecimal value = metric(row, subject);
            if (value == null)
            {
                continue;
            }
            StuScoreRank item = toRank(row, value);
            groups.computeIfAbsent(groupKey.apply(row), k -> new ArrayList<>()).add(item);
        }

        List<StuScoreRank> result = new ArrayList<>();
        for (List<StuScoreRank> group : groups.values())
        {
            group.sort(Comparator.comparing(StuScoreRank::getRankValue).reversed()
                    .thenComparing(StuScoreRank::getStudentNo, Comparator.nullsLast(Comparator.naturalOrder())));
            for (int i = 0; i < group.size(); i++)
            {
                StuScoreRank current = group.get(i);
                if (i > 0 && current.getRankValue().compareTo(group.get(i - 1).getRankValue()) == 0)
                {
                    current.setRank(group.get(i - 1).getRank());
                }
                else
                {
                    current.setRank(i + 1);
                }
            }
            result.addAll(group);
        }
        return result;
    }

    /**
     * 计算每个学生在分组内的名次，用于成绩单。未参与排名的学生不在返回结果中。
     *
     * @return 学生ID -> 名次
     */
    static Map<Long, Integer> rankByStudent(List<StuScoreDetail> rows, Function<StuScoreDetail, String> groupKey,
            StuScoreSubject subject)
    {
        Map<Long, Integer> ranks = new HashMap<>();
        for (StuScoreRank item : rank(rows, groupKey, subject))
        {
            ranks.put(item.getStudentId(), item.getRank());
        }
        return ranks;
    }

    /**
     * 分组键：年级
     */
    static String gradeKey(StuScoreDetail row)
    {
        return nullToEmpty(row.getGrade());
    }

    /**
     * 分组键：年级 + 班级（班级名在不同年级间可能重复，因此需要组合）
     */
    static String classKey(StuScoreDetail row)
    {
        return nullToEmpty(row.getGrade()) + "\u0001" + nullToEmpty(row.getClassName());
    }

    private static String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }

    private static BigDecimal metric(StuScoreDetail row, StuScoreSubject subject)
    {
        if (!isComplete(row))
        {
            return null;
        }
        return subject == null ? total(row) : score(row, subject);
    }

    private static StuScoreRank toRank(StuScoreDetail row, BigDecimal value)
    {
        StuScoreRank item = new StuScoreRank();
        item.setScoreId(row.getScoreId());
        item.setExamId(row.getExamId());
        item.setExamName(row.getExamName());
        item.setExamTime(row.getExamTime());
        item.setStudentId(row.getStudentId());
        item.setStudentNo(row.getStudentNo());
        item.setStudentName(row.getStudentName());
        item.setGrade(row.getGrade());
        item.setClassName(row.getClassName());
        item.setChinese(row.getChinese());
        item.setMath(row.getMath());
        item.setEnglish(row.getEnglish());
        item.setPhysics(row.getPhysics());
        item.setChemistry(row.getChemistry());
        item.setBiology(row.getBiology());
        item.setAbsentSubjects(row.getAbsentSubjects());
        item.setTotal(total(row));
        item.setComplete(true);
        item.setRankValue(value);
        return item;
    }

    static BigDecimal score(StuScoreDetail detail, StuScoreSubject subject)
    {
        switch (subject)
        {
            case CHINESE: return detail.getChinese();
            case MATH: return detail.getMath();
            case ENGLISH: return detail.getEnglish();
            case PHYSICS: return detail.getPhysics();
            case CHEMISTRY: return detail.getChemistry();
            case BIOLOGY: return detail.getBiology();
            default: throw new IllegalArgumentException("未知科目：" + subject);
        }
    }
}
