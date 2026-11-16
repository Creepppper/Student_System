package com.ruoyi.system.domain;

import java.util.Set;
import com.ruoyi.common.exception.ServiceException;

/** 年级、班级的标准名称。 */
public final class SchoolClassNames
{
    private static final Set<String> GRADES = Set.of("高一", "高二", "高三");

    private SchoolClassNames() { }

    public static void validate(String grade, String className)
    {
        if (!GRADES.contains(grade == null ? "" : grade))
        {
            throw new ServiceException("年级不存在，请选择高一、高二或高三");
        }
        if (className == null || !className.matches("(?:[1-9]|1[0-2])班"))
        {
            throw new ServiceException("班级不存在，请选择1班至12班");
        }
    }
}
