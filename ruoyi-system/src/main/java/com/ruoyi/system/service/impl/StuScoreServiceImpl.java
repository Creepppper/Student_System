package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.StuScoreMapper;
import com.ruoyi.system.domain.StuScore;
import com.ruoyi.system.service.IStuScoreService;

/**
 * 学生成绩Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-10-31
 */
@Service
public class StuScoreServiceImpl implements IStuScoreService 
{
    @Autowired
    private StuScoreMapper stuScoreMapper;

    /**
     * 查询学生成绩
     * 
     * @param id 学生成绩主键
     * @return 学生成绩
     */
    @Override
    public StuScore selectStuScoreById(Long id)
    {
        return stuScoreMapper.selectStuScoreById(id);
    }

    /**
     * 查询学生成绩列表
     * 
     * @param stuScore 学生成绩
     * @return 学生成绩
     */
    @Override
    public List<StuScore> selectStuScoreList(StuScore stuScore)
    {
        return stuScoreMapper.selectStuScoreList(stuScore);
    }

    /**
     * 新增学生成绩
     * 
     * @param stuScore 学生成绩
     * @return 结果
     */
    @Override
    public int insertStuScore(StuScore stuScore)
    {
        return stuScoreMapper.insertStuScore(stuScore);
    }

    /**
     * 修改学生成绩
     * 
     * @param stuScore 学生成绩
     * @return 结果
     */
    @Override
    public int updateStuScore(StuScore stuScore)
    {
        return stuScoreMapper.updateStuScore(stuScore);
    }

    /**
     * 批量删除学生成绩
     * 
     * @param ids 需要删除的学生成绩主键
     * @return 结果
     */
    @Override
    public int deleteStuScoreByIds(Long[] ids)
    {
        return stuScoreMapper.deleteStuScoreByIds(ids);
    }

    /**
     * 删除学生成绩信息
     * 
     * @param id 学生成绩主键
     * @return 结果
     */
    @Override
    public int deleteStuScoreById(Long id)
    {
        return stuScoreMapper.deleteStuScoreById(id);
    }
}
