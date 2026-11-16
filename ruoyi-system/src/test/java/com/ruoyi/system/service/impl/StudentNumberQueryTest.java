package com.ruoyi.system.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.system.domain.StuReview;
import com.ruoyi.system.domain.StuScore;

class StudentNumberQueryTest
{
    private BoundSql query(String mapper, String statement, BaseEntity input) throws Exception
    {
        Configuration config = new Configuration();
        config.getTypeAliasRegistry().registerAliases("com.ruoyi.system.domain");
        String resource = "mapper/system/" + mapper + ".xml";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(stream, config, resource, config.getSqlFragments()).parse();
        }
        return config.getMappedStatement("com.ruoyi.system.mapper." + mapper + "." + statement).getBoundSql(input);
    }

    @Test
    void scoreQueryCombinesStudentNumberAndTeacherScope() throws Exception
    {
        StuScore score = new StuScore();
        score.setStudentNo("20261101");
        score.getParams().put("scopeMode", "TEACHER");
        score.getParams().put("scopeUserId", 7L);
        BoundSql sql = query("StuScoreMapper", "selectStuScoreList", score);
        assertTrue(sql.getSql().contains("sn.student_no = ?"));
        assertTrue(sql.getSql().contains("t.user_id = ?"));
        assertTrue(sql.getParameterMappings().stream().anyMatch(p -> p.getProperty().equals("studentNo")));
        score.setStudentNo("");
        assertFalse(query("StuScoreMapper", "selectStuScoreList", score).getSql().contains("sn.student_no"));
    }

    @Test
    void reviewQueryCombinesStudentNumberAndTeacherScope() throws Exception
    {
        StuReview review = new StuReview();
        review.setStudentNo("20261101");
        review.getParams().put("scopeMode", "TEACHER");
        review.getParams().put("scopeUserId", 7L);
        BoundSql sql = query("StuReviewMapper", "selectStuReviewList", review);
        assertTrue(sql.getSql().contains("sn.student_no = ?"));
        assertTrue(sql.getSql().contains("t.user_id = ?"));
        assertTrue(sql.getParameterMappings().stream().anyMatch(p -> p.getProperty().equals("studentNo")));
        review.setStudentNo(null);
        assertFalse(query("StuReviewMapper", "selectStuReviewList", review).getSql().contains("sn.student_no"));
    }
}
