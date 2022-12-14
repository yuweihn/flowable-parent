package com.wei.system.service.survey;

import java.util.List;
import com.wei.system.domain.survey.SurveyPaper;

/**
 * 调查问卷模板Service接口
 * 
 * @author 2y
 * @date 2021-03-21
 */
public interface ISurveyPaperService 
{
    /**
     * 查询调查问卷模板
     * 
     * @param id 调查问卷模板ID
     * @return 调查问卷模板
     */
    public SurveyPaper selectSurveyPaperById(Long id);

    /**
     * 查询调查问卷模板列表
     * 
     * @param surveyPaper 调查问卷模板
     * @return 调查问卷模板集合
     */
    public List<SurveyPaper> selectSurveyPaperList(SurveyPaper surveyPaper);

    /**
     * 新增调查问卷模板
     * 
     * @param surveyPaper 调查问卷模板
     * @return 结果
     */
    public int insertSurveyPaper(SurveyPaper surveyPaper);

    /**
     * 修改调查问卷模板
     * 
     * @param surveyPaper 调查问卷模板
     * @return 结果
     */
    public int updateSurveyPaper(SurveyPaper surveyPaper);

    /**
     * 批量删除调查问卷模板
     * 
     * @param ids 需要删除的调查问卷模板ID
     * @return 结果
     */
    public int deleteSurveyPaperByIds(Long[] ids);

    /**
     * 删除调查问卷模板信息
     * 
     * @param id 调查问卷模板ID
     * @return 结果
     */
    public int deleteSurveyPaperById(Long id);

    /**
     * 查询所有问卷信息
     * @return
     */
    List<SurveyPaper> selectPaperAll();

    /**
     * 问卷详情
     * @param id
     * @return
     */
    SurveyPaper selectSurveyPaperDetail(Long id);
}
