package com.wei.flowable.service;


import com.wei.common.core.domain.AjaxResult;
import com.wei.flowable.domain.dto.FlowProcDefDto;
import com.yuweix.kuafu.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author XuanXuan
 * @date 2021-04-03 14:41
 */
public interface IFlowDefinitionService {
    boolean exist(String processDefinitionKey);


    int queryProcDefCount(String deploymentId, String category);
    /**
     * 流程定义列表
     *
     * @param pageNo  当前页码
     * @param pageSize 每页条数
     * @return 流程定义分页列表数据
     */
    List<FlowProcDefDto> queryProcDefList(String deploymentId, String category, int pageNo, int pageSize);

    FlowProcDefDto getProcDefInfoByDeployId(String deployId);

    /**
     * 导入流程文件
     *
     * @param name
     * @param category
     * @param in
     */
    void importFile(String name, String category, InputStream in);

    /**
     * 读取xml
     * @param deployId
     * @return
     */
    AjaxResult readXml(String deployId) throws IOException;

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId
     * @param variables
     * @return
     */

    Response<Integer, Void> startProcessInstanceById(String procDefId, Map<String, Object> variables);


    /**
     * 激活或挂起流程定义
     *
     * @param state    状态
     * @param deployId 流程部署ID
     */
    void updateState(Integer state, String deployId);

    /**
     * 删除流程定义
     *
     * @param deployIds 流程部署ID act_ge_bytearray 表中 deployment_id值
     */
    void delete(String[] deployIds);

    /**
     * 读取图片文件
     * @param deployId
     * @return
     */
    InputStream readImage(String deployId);

    /**
     * 根据deployId查询procDefId
     */
    String getProcDefIdByDeployId(String deployId);
}
