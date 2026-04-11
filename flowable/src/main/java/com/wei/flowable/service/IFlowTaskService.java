package com.wei.flowable.service;


import com.wei.common.core.domain.AjaxResult;
import com.wei.flowable.domain.dto.FlowTaskDto;
import com.wei.flowable.domain.vo.FlowTaskVo;
import com.wei.flowable.domain.vo.UserTaskVo;
import com.yuweix.kuafu.core.Response;
import org.flowable.task.api.Task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author XuanXuan
 * @date 2021-04-03 14:42
 */
public interface IFlowTaskService {
    /**
     * 审批任务
     */
    Response<Integer, Void> complete(String taskId, String procInsId, String comment, Map<String, Object> variables);
    Response<Integer, Void> completeV2(String taskId, String comment, Map<String, Object> variables);

    /**
     * 驳回任务
     */
    void taskReject(String taskId, String comment);

    /**
     * 退回任务
     */
    void taskReturn(String taskId, String targetKey, String comment);

    /**
     * 获取所有可回退的节点
     * @return
     */
    Response<Integer, List<UserTaskVo>> findReturnTaskList(String taskId);
    /**
     * 获取可回退的节点(起始节点)
     * @return
     */
    Response<Integer, UserTaskVo> findReturnTask(String taskId);

    /**
     * 删除任务
     */
    void deleteTask(String taskId, String comment);

    /**
     * 认领/签收任务
     */
    void claim(String taskId, String userId);

    /**
     * 取消认领/签收任务
     */
    void unclaim(String taskId);

    /**
     * 委派任务
     */
    void delegateTask(String taskId, String assignee);


    /**
     * 转办任务
     */
    void assignTask(String taskId, String userId);

    int queryProcessCount(String procInsId, String startUserId, String category);
    /**
     * 我发起的流程
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<FlowTaskDto> queryProcessList(String procInsId, String startUserId, String category, int pageNo, int pageSize);

    /**
     * 取消申请
     * @return
     */
    Response<Integer, Void> stopProcess(String procInsId);

    /**
     * 撤回流程
     * @return
     */
    AjaxResult revokeProcess(String procInsId);


    int queryTodoCount(String procInsId, String category);
    /**
     * 代办任务列表
     *
     * @param pageNo  当前页码
     * @param pageSize 每页条数
     * @return
     */
    List<FlowTaskDto> queryTodoList(String procInsId, String category, int pageNo, int pageSize);


    int queryFinishedCount(String procInsId, String category);
    /**
     * 已办任务列表
     *
     * @param pageNo  当前页码
     * @param pageSize 每页条数
     * @return
     */
    List<FlowTaskDto> queryFinishedList(String procInsId, String category, int pageNo, int pageSize);

    /**
     * 流程历史流转记录
     * @param procInsId 流程实例Id
     * @return
     */
    Response<Integer, List<FlowTaskDto>> getFlowRecords(String procInsId);

    /**
     * 查询流程表单
     */
    Response<Integer, Map<String, Object>> getFlowForm(String deployId);

    /**
     * 根据任务ID查询挂载的表单信息
     *
     * @param taskId 任务Id
     * @return
     */
    Task getTaskForm(String taskId);

    /**
     * 获取流程过程图
     * @param processId
     * @return
     */
    InputStream diagram(String processId);

    /**
     * 获取流程执行过程
     * @param procInsId
     * @return
     */
    AjaxResult getFlowViewer(String procInsId);

    /**
     * 获取流程变量
     * @param taskId
     * @return
     */
    Response<Integer, Map<String, Object>> processVariables(String taskId);

    /**
     * 检查是否为起始节点
     * @param taskId
     * @return
     */
    Response<Integer, Boolean> checkIfProcessTaskStartNode(String taskId);

    /**
     * 重启流程实例
     * @param taskId
     * @param variables
     * @return
     */

    Response<Integer, Void> restartTask(String taskId, Map<String, Object> variables);

    /**
     * 获取下一节点
     * @return
     */
    AjaxResult getNextFlowNode(String taskId);

    /**
     * 获取下一节点企业账号信息
     * @return
     */
    List<String> getNextFlowNode(FlowTaskVo flowTaskVo,long deptUserId,long startUserId);

}
