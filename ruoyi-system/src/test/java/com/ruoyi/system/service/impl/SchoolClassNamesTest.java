package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SchoolClassNames;

class SchoolClassNamesTest
{
    @Test
    void acceptsOnlyStandardGradesAndClasses()
    {
        for (String grade : new String[] {"高一", "高二", "高三"})
        {
            for (int number = 1; number <= 12; number++)
            {
                String className = number + "班";
                assertDoesNotThrow(() -> SchoolClassNames.validate(grade, className));
            }
        }
        for (String grade : new String[] {"高1", "高四", "高一 ", ""})
        {
            assertThrows(ServiceException.class, () -> SchoolClassNames.validate(grade, "1班"));
        }
        for (String className : new String[] {"0班", "13班", "01班", "1班 ", ""})
        {
            assertThrows(ServiceException.class, () -> SchoolClassNames.validate("高一", className));
        }
    }
}
