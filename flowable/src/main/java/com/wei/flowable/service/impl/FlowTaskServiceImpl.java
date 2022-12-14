package com.wei.flowable.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.wei.common.constant.HttpStatus;
import com.wei.common.constant.ProcessConstants;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.core.domain.entity.SysRole;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.flowable.common.enums.FlowComment;
import com.wei.common.exception.CustomException;
import com.wei.common.utils.SecurityUtils;
import com.wei.flowable.domain.dto.FlowCommentDto;
import com.wei.flowable.domain.dto.FlowNextDto;
import com.wei.flowable.domain.dto.FlowTaskDto;
import com.wei.flowable.domain.dto.FlowViewerDto;
import com.wei.flowable.domain.vo.FlowTaskVo;
import com.wei.flowable.domain.vo.UserTaskVo;
import com.wei.flowable.factory.FlowServiceFactory;
import com.wei.flowable.flow.CustomProcessDiagramGenerator;
import com.wei.flowable.flow.FindNextNodeUtil;
import com.wei.flowable.flow.FlowableUtils;
import com.wei.flowable.service.IFlowTaskService;
import com.wei.flowable.service.ISysDeployFormService;
import com.wei.system.domain.SysForm;
import com.wei.system.domain.SysUserRole;
import com.wei.system.mapper.SysUserRoleMapper;
import com.wei.system.service.ISysRoleService;
import com.wei.system.service.ISysUserService;
import com.wei.system.service.OrderService;
import com.wei.system.service.QiyeWeixinService;
import com.wei.system.strategy.ProcContext;
import com.wei.system.strategy.ProcStrategy;
import com.yuweix.tripod.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;

import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author XuanXuan
 * @date 2021-04-03
 **/
@Service
@Slf4j
public class FlowTaskServiceImpl extends FlowServiceFactory implements IFlowTaskService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private OrderService orderService;

    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private SysUserRoleMapper userRoleMapper;

    @Resource
    private ISysDeployFormService sysInstanceFormService;

    @Resource
    private QiyeWeixinService weixinService;


    /**
     * ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<Integer, Void> complete(String taskId, String procInsId, String comment, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            return new Response<>(HttpStatus.ERROR, "???????????????");
        }
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskId, procInsId, FlowComment.DELEGATE.getType(), comment);
            taskService.resolveTask(taskId, variables);
        } else {
            taskService.addComment(taskId, procInsId, FlowComment.NORMAL.getType(), comment);
            Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
            taskService.setAssignee(taskId, userId.toString());
            taskService.complete(taskId, variables);
        }
        return new Response<>(HttpStatus.SUCCESS, "????????????");
    }

    /**
     * ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<Integer, Void> completeV2(String taskId, String comment, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            return new Response<>(HttpStatus.ERROR, "???????????????");
        }

        String procInsId = task.getProcessInstanceId();
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskId, procInsId, FlowComment.DELEGATE.getType(), comment);
            taskService.resolveTask(taskId, variables);
            return new Response<>(HttpStatus.ERROR, "Pending...");
        }

        String procDefId = task.getProcessDefinitionId();
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
        if (procDef == null) {
            return new Response<>(HttpStatus.ERROR, "?????????????????????");
        }
        long deptUserId = Long.parseLong(variables.get(ProcessConstants.DEPT_LEADER).toString());
        long startUserId = Long.parseLong(variables.get(ProcessConstants.PROCESS_INITIATOR).toString());
        FlowTaskVo flowTaskVo = new FlowTaskVo();
        flowTaskVo.setTaskId(taskId);
        flowTaskVo.setInstanceId(procInsId);
        List<String> nextWeComNoList = getNextFlowNode(flowTaskVo, deptUserId, startUserId);
        String title = "?????????????????????" + variables.get("title") + "????????????????????????";
        weixinService.sendNewsMessage(nextWeComNoList, title);

        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        ProcStrategy procStrategy = ProcContext.build(procDef.getCategory());
        if (procStrategy != null) {
            procStrategy.update(procInsId, variables, sysUser.getUserId());
        }

        taskService.addComment(taskId, procInsId, FlowComment.NORMAL.getType(), comment);
        taskService.setAssignee(taskId, sysUser.getUserId().toString());
        taskService.complete(taskId, variables);

        return new Response<>(HttpStatus.SUCCESS, "????????????");
    }

    /**
     * ????????????
     */
    @Override
    public void taskReject(String taskId, String comment) {
        if (taskService.createTaskQuery().taskId(taskId).singleResult().isSuspended()) {
            throw new CustomException("????????????????????????");
        }
        // ???????????? task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // ????????????????????????
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // ????????????????????????
        Process process = repositoryService.getBpmnModel(procDef.getId()).getProcesses().get(0);
        // ??????????????????????????????????????????
        Collection<FlowElement> allElements = FlowableUtils.getAllElements(process.getFlowElements(), null);
        // ??????????????????????????????
        FlowElement source = null;
        if (!allElements.isEmpty()) {
            for (FlowElement flowElement : allElements) {
                // ?????????????????????
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    // ??????????????????
                    source = flowElement;
                }
            }
        } else {
            allElements = new ArrayList<>();
        }

        // ???????????????????????????????????? targetIds
        // ???????????????????????????????????????????????????
        // ?????????????????????????????????????????????
        List<UserTask> parentUserTaskList = FlowableUtils.iteratorFindParentUserTasks(source, null, null);
        if (parentUserTaskList == null || parentUserTaskList.size() == 0) {
            throw new CustomException("????????????????????????????????????????????????");
        }
        // ???????????? ID ????????? Key
        List<String> parentUserTaskKeyList = new ArrayList<>();
        parentUserTaskList.forEach(item -> parentUserTaskKeyList.add(item.getId()));
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        List<HistoricTaskInstance> hiTaskInsList = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).orderByHistoricTaskInstanceStartTime().asc().list();
        // ???????????????????????????????????????????????????
        List<String> lastHistoricTaskInstanceList = FlowableUtils.historicTaskInstanceClean(allElements, hiTaskInsList);
        // ????????????????????????????????????????????????????????????
        List<String> targetIds = new ArrayList<>();
        // ??????????????????????????????????????????????????????
        int number = 0;
        StringBuilder parentHistoricTaskKey = new StringBuilder();
        for (String historicTaskInstanceKey : lastHistoricTaskInstanceList) {
            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (parentHistoricTaskKey.toString().equals(historicTaskInstanceKey)) {
                continue;
            }
            parentHistoricTaskKey = new StringBuilder(historicTaskInstanceKey);
            if (historicTaskInstanceKey.equals(task.getTaskDefinitionKey())) {
                number++;
            }
            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // number == 1??????????????????????????????
            // number == 2??????????????????????????????????????????????????????
            if (number == 2) {
                break;
            }
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (parentUserTaskKeyList.contains(historicTaskInstanceKey)) {
                targetIds.add(historicTaskInstanceKey);
            }
        }


        // ?????????????????????????????????????????? currentIds
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        UserTask oneUserTask = parentUserTaskList.get(0);
        // ??????????????????????????????????????? Key???????????????????????????????????????????????????????????????????????????
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // ?????????????????????
        List<String> currentIds = new ArrayList<>();
        // ?????????????????????????????????????????? runTaskList ????????????????????????????????????
        List<UserTask> currentUserTaskList = FlowableUtils.iteratorFindChildUserTasks(oneUserTask, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));


        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (targetIds.size() > 1 && currentIds.size() > 1) {
            throw new CustomException("??????????????????????????????????????????");
        }

        // ?????????????????????????????????????????????ID???????????????????????????
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // ??????????????????
        currentTaskIds.forEach(item -> taskService.addComment(item, task.getProcessInstanceId(), FlowComment.REJECT.getType(), comment));

        try {
            // ???????????????????????? 1 ??????????????????????????????????????????????????????????????????????????????
            if (targetIds.size() > 1) {
                // 1 ??? ??????????????????currentIds ????????????(1)???targetIds ??????????????????(???)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId()).
                        moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds).changeState();
            }
            // ??????????????????????????????????????????????????????????????????????????????
            if (targetIds.size() == 1) {
                // 1 ??? 1 ??? ??? ??? 1 ?????????currentIds ??????????????????????????????(1??????)???targetIds.get(0) ??????????????????(1)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId())
                        .moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0)).changeState();
            }
        } catch (FlowableObjectNotFoundException e) {
            throw new CustomException("???????????????????????????????????????????????????");
        } catch (FlowableException e) {
            throw new CustomException("???????????????????????????");
        }
    }

    /**
     * ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReturn(String taskId, String targetKey, String comment) {
        if (taskService.createTaskQuery().taskId(taskId).singleResult().isSuspended()) {
            throw new CustomException("????????????????????????");
        }
        // ???????????? task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // ????????????????????????
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // ????????????????????????
        Process proc = repositoryService.getBpmnModel(procDef.getId()).getProcesses().get(0);
        // ??????????????????????????????????????????
        Collection<FlowElement> allElements = FlowableUtils.getAllElements(proc.getFlowElements(), null);
        // ??????????????????????????????
        FlowElement source = null;
        // ???????????????????????????
        FlowElement target = null;
        if (allElements != null) {
            for (FlowElement flowElement : allElements) {
                // ????????????????????????
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    source = flowElement;
                }
                // ?????????????????????
                if (flowElement.getId().equals(targetKey)) {
                    target = flowElement;
                }
            }
        }

        // ???????????????????????????
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ??????????????????????????????????????????????????????
        Boolean isSequential = FlowableUtils.iteratorCheckSequentialReferTarget(source, targetKey, null, null);
        if (!isSequential) {
            throw new CustomException("????????????????????????????????????????????????????????????????????????");
        }

        // ??????????????????????????????????????? Key???????????????????????????????????????????????????????????????????????????
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = runTaskList.stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList());

        // ?????????????????????????????????????????? allTaskList ????????????????????????????????????
        List<UserTask> needBackUserTaskList = FlowableUtils.iteratorFindChildUserTasks(target, runTaskKeyList, null, null);
        // ???????????????Key??????
        List<String> backTaskKeys = needBackUserTaskList.stream().map(UserTask::getId).collect(Collectors.toList());

        // ?????????????????????????????????????????????ID???????????????????????????
        List<String> backTaskIds = new ArrayList<>();
        backTaskKeys.forEach(backTaskKey -> runTaskList.forEach(aTask -> {
            if (backTaskKey.equals(aTask.getTaskDefinitionKey())) {
                backTaskIds.add(aTask.getId());
            }
        }));
        //??????????????????
        for (String backTaskId : backTaskIds) {
            taskService.addComment(backTaskId, task.getProcessInstanceId(), FlowComment.REBACK.getType(), comment);
        }

        try {
            // 1 ??? 1 ??? ??? ??? 1 ?????????curIds ??????????????????????????????(1??????)???targetKey ??????????????????(1)
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdsToSingleActivityId(backTaskKeys, targetKey).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new CustomException("???????????????????????????????????????????????????");
        } catch (FlowableException e) {
            throw new CustomException("??????????????????");
        }
    }


    /**
     * ??????????????????????????????
     * @return
     */
    @Override
    public Response<Integer, List<UserTaskVo>> findReturnTaskList(String taskId) {
        // ???????????? task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return new Response<>(HttpStatus.ERROR, "???????????????");
        }
        // ????????????????????????
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // ??????????????????????????????????????????????????????
        Process process = repositoryService.getBpmnModel(procDef.getId()).getProcesses().get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        // ??????????????????????????????
        UserTask source = null;
        if (flowElements != null) {
            for (FlowElement flowElement : flowElements) {
                // ?????????????????????
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    source = (UserTask) flowElement;
                }
            }
        }
        // ???????????????????????????
        List<List<UserTask>> roads = FlowableUtils.findRoad(source, null, null, null);
        // ????????????????????????
        Set<UserTaskVo> voSet = new LinkedHashSet<>();
        for (List<UserTask> roads2: roads) {
            for (UserTask ut: roads2) {
                voSet.add(UserTaskVo.builder().id(ut.getId()).name(ut.getName()).build());
            }
        }
        return new Response<>(HttpStatus.SUCCESS, "????????????", new ArrayList<>(voSet));
    }
    /**
     * ????????????????????????(????????????)
     * @return
     */
    @Override
    public Response<Integer, UserTaskVo> findReturnTask(String taskId) {
        Response<Integer, List<UserTaskVo>> taskListResp = this.findReturnTaskList(taskId);
        if (HttpStatus.SUCCESS != taskListResp.getCode()) {
            return Response.create(taskListResp.getCode(), taskListResp.getMsg());
        }
        List<UserTaskVo> voList = taskListResp.getData();
        if (voList == null || voList.size() <= 0) {
            return Response.create(HttpStatus.ERROR, "??????????????????");
        }

        TaskInfo taskIns = historyService.createHistoricTaskInstanceQuery().taskId(taskId).finished()
                .includeProcessVariables().singleResult();
        if (taskIns == null) {
            taskIns = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        }
        if (taskIns == null) {
            return new Response<>(HttpStatus.ERROR, "???????????????");
        }
        Map<String, Object> procVars = taskIns.getProcessVariables();
        if (procVars == null || procVars.isEmpty()) {
            return Response.create(HttpStatus.ERROR, "??????????????????");
        }
        String startTaskKey = (String) procVars.get(ProcessConstants.PROCESS_START_TASK_KEY);
        if (startTaskKey == null || "".equals(startTaskKey)) {
            return Response.create(HttpStatus.ERROR, "??????????????????");
        }

        for (UserTaskVo vo: voList) {
            if (startTaskKey.equals(vo.getId())) {
                return Response.create(HttpStatus.SUCCESS, "ok", vo);
            }
        }
        return Response.create(HttpStatus.ERROR, "??????????????????");
    }

    /**
     * ????????????
     */
    @Override
    public void deleteTask(String taskId, String comment) {
        taskService.deleteTask(taskId, comment);
    }

    /**
     * ??????/????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * ????????????/????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unclaim(String taskId) {
        taskService.unclaim(taskId);
    }

    /**
     * ????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(String taskId, String assignee) {
        taskService.delegateTask(taskId, assignee);
    }


    /**
     * ????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    @Override
    public int queryProcessCount(String procInsId, String startUserId, String category) {
        HistoricProcessInstanceQuery hiProcInsQuery = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .startedBy(startUserId)
                .processDefinitionCategory(category);
        return (int) hiProcInsQuery.count();
    }
    /**
     * ??????????????????
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public List<FlowTaskDto> queryProcessList(String procInsId, String startUserId, String category, int pageNo, int pageSize) {
        HistoricProcessInstanceQuery hiProcInsQuery = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .startedBy(startUserId)
                .processDefinitionCategory(category)
                .orderByProcessInstanceStartTime()
                .desc();
        List<HistoricProcessInstance> hiProcInsList = hiProcInsQuery.listPage((pageNo - 1) * pageSize, pageSize);
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : hiProcInsList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            flowTask.setCreateTime(hisIns.getStartTime());
            flowTask.setFinishTime(hisIns.getEndTime());
            flowTask.setProcInsId(hisIns.getId());

            // ????????????
            if (Objects.nonNull(hisIns.getEndTime())) {
                long time = hisIns.getEndTime().getTime() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            } else {
                long time = System.currentTimeMillis() - hisIns.getStartTime().getTime();
                flowTask.setDuration(getDate(time));
            }
            // ??????????????????
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(hisIns.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());

            //??????????????????
            ProcStrategy procStrategy = ProcContext.build(pd.getCategory());
            flowTask.setProcInsNo(procStrategy == null ? null : procStrategy.getProcNo(hisIns.getId()));
            flowTask.setTitle(procStrategy == null ? null : procStrategy.getProcTitle(hisIns.getId()));

            // ??????????????????????????????
            TaskInfo latestTask = null;
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).orderByTaskCreateTime().desc().listPage(0, 1);
            if (CollectionUtils.isNotEmpty(taskList)) {
                latestTask = taskList.get(0);
            } else {
                List<HistoricTaskInstance> hiTaskList = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(hisIns.getId()).orderByHistoricTaskInstanceEndTime().desc().listPage(0, 1);
                if (CollectionUtils.isNotEmpty(hiTaskList)) {
                    latestTask = hiTaskList.get(0);
                }
            }
            if (latestTask != null) {
                flowTask.setTaskId(latestTask.getId());
                flowTask.setTaskName(latestTask.getName());
                if (StringUtils.isNotBlank(latestTask.getAssignee())) {
                    SysUser sysUser = sysUserService.selectUserById(Long.parseLong(latestTask.getAssignee()));
                    if (sysUser != null) {
                        flowTask.setAssigneeId(sysUser.getUserId());
                        flowTask.setAssigneeName(sysUser.getNickName());
                        flowTask.setDeptName(sysUser.getDept().getDeptName());
                    }
                }
                List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(latestTask.getId());
                StringBuilder stringBuilder = new StringBuilder();
                for (HistoricIdentityLink identityLink : linksForTask) {
                    if ("candidate".equals(identityLink.getType())) {
                        if (StringUtils.isNotBlank(identityLink.getUserId())) {
                            SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                            if (sysUser != null) {
                                stringBuilder.append(sysUser.getNickName()).append(",");
                            }
                        }
                        if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                            SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                            if (sysRole != null) {
                                stringBuilder.append(sysRole.getRoleName()).append(",");
                            }
                        }
                    }
                }
                if (StringUtils.isNotBlank(stringBuilder)) {
                    flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                }
            }
            flowList.add(flowTask);
        }
        return flowList;
    }

    /**
     * ????????????
     * @return
     */
    @Override
    public Response<Integer, Void> stopProcess(String procInsId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procInsId).list();
        if (CollectionUtils.isEmpty(taskList)) {
            throw new CustomException("??????????????????????????????????????????????????????");
        }

        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInsId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (Objects.isNull(bpmnModel)) {
            return Response.create(HttpStatus.SUCCESS, "????????????");
        }

        Process process = bpmnModel.getMainProcess();
        List<EndEvent> endNodes = process.findFlowElementsOfType(EndEvent.class, false);
        if (CollectionUtils.isEmpty(endNodes)) {
            return Response.create(HttpStatus.SUCCESS, "????????????");
        }
        Authentication.setAuthenticatedUserId(loginUser.getUserId().toString());
//                taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.STOP.getType(),
//                        StringUtils.isBlank(flowTaskVo.getComment()) ? "????????????" : flowTaskVo.getComment());
        String endId = endNodes.get(0).getId();
        List<Execution> executions = runtimeService.createExecutionQuery().parentId(processInstance.getProcessInstanceId()).list();
        List<String> executionIds = new ArrayList<>();
        executions.forEach(execution -> executionIds.add(execution.getId()));
        runtimeService.createChangeActivityStateBuilder().moveExecutionsToSingleActivityId(executionIds, endId).changeState();
        return Response.create(HttpStatus.SUCCESS, "????????????");
    }

    /**
     * ????????????  ??????????????????
     * @return
     */
    @Override
    public AjaxResult revokeProcess(String procInsId) {
        Task task = taskService.createTaskQuery().processInstanceId(procInsId).singleResult();
        if (task == null) {
            throw new CustomException("????????????????????????????????????????????????");
        }

        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByTaskCreateTime()
                .asc()
                .list();
        String myTaskId = null;
        HistoricTaskInstance myTask = null;
        for (HistoricTaskInstance hti : htiList) {
            if (loginUser.getUserId().toString().equals(hti.getAssignee())) {
                myTaskId = hti.getId();
                myTask = hti;
                break;
            }
        }
        if (null == myTaskId) {
            throw new CustomException("?????????????????????????????????????????????");
        }

        String processDefinitionId = myTask.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        //??????
//      Map<String, VariableInstance> variables = runtimeService.getVariableInstances(currentTask.getExecutionId());
        String myActivityId = null;
        List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
                .executionId(myTask.getExecutionId()).finished().list();
        for (HistoricActivityInstance hai : haiList) {
            if (myTaskId.equals(hai.getTaskId())) {
                myActivityId = hai.getActivityId();
                break;
            }
        }
        FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(myActivityId);

        Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();
        FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityId);

        //?????????????????????
        List<SequenceFlow> oriSequenceFlows = new ArrayList<>(flowNode.getOutgoingFlows());


        return AjaxResult.success();
    }

    @Override
    public int queryTodoCount(String procInsId, String category) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        Long userId = sysUser.getUserId();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .or()
                .taskCandidateOrAssigned(String.valueOf(userId))
                .taskCandidateGroupIn(sysUser.getRoleIdList().stream().map(String::valueOf).collect(Collectors.toList()))
                .endOr();
        if (procInsId != null && !"".equals(procInsId)) {
            taskQuery.processInstanceId(procInsId);
        }
        if (category != null && !"".equals(category)) {
            taskQuery.processCategoryIn(Collections.singleton(category));
        }
        return (int) taskQuery.count();
    }
    /**
     * ??????????????????
     *
     * @param pageNo  ????????????
     * @param pageSize ????????????
     * @return ??????????????????
     */
    @Override
    public List<FlowTaskDto> queryTodoList(String procInsId, String category, int pageNo, int pageSize) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        Long userId = sysUser.getUserId();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .or()
                .taskAssignee(String.valueOf(userId))
                .taskCandidateUser(String.valueOf(userId))
                .taskCandidateGroupIn(sysUser.getRoleIdList().stream().map(String::valueOf).collect(Collectors.toList()))
                .endOr()
                .orderByTaskCreateTime().desc();
        if (procInsId != null && !"".equals(procInsId)) {
            taskQuery.processInstanceId(procInsId);
        }
        if (category != null && !"".equals(category)) {
            taskQuery.processCategoryIn(Collections.singleton(category));
        }
        List<Task> taskList = taskQuery.listPage((pageNo - 1) * pageSize, pageSize);
        List<FlowTaskDto> flowList = new ArrayList<>();
        for (Task task : taskList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            // ??????????????????
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            flowTask.setProcInsId(task.getProcessInstanceId());
            // ??????????????????
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());

            //??????????????????
            ProcStrategy procStrategy = ProcContext.build(pd.getCategory());
            flowTask.setProcInsNo(procStrategy == null ? null : procStrategy.getProcNo(task.getProcessInstanceId()));
            flowTask.setTitle(procStrategy == null ? null : procStrategy.getProcTitle(task.getProcessInstanceId()));

            // ?????????????????????
            HistoricProcessInstance hiProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(hiProcIns.getStartUserId()));
//            SysUser startUser = sysUserService.selectUserById(Long.parseLong(task.getAssignee()));
            if (startUser != null) {
                flowTask.setStartUserId(String.valueOf(startUser.getUserId()));
                flowTask.setStartUserName(startUser.getNickName());
                flowTask.setStartDeptName(startUser.getDept().getDeptName());
            }
            flowList.add(flowTask);
        }
        return flowList;
    }


    @Override
    public int queryFinishedCount(String procInsId, String category) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        Long userId = sysUser.getUserId();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .taskAssignee(userId.toString())
                .finished();
        if (procInsId != null && !"".equals(procInsId)) {
            taskInstanceQuery.processInstanceId(procInsId);
        }
        if (category != null && !"".equals(category)) {
            taskInstanceQuery.processCategoryIn(Collections.singleton(category));
        }
        return (int) taskInstanceQuery.count();
    }
    /**
     * ??????????????????
     *
     * @param pageNo  ????????????
     * @param pageSize ????????????
     * @return
     */
    @Override
    public List<FlowTaskDto> queryFinishedList(String procInsId, String category, int pageNo, int pageSize) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        Long userId = sysUser.getUserId();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .taskAssignee(userId.toString())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        if (procInsId != null && !"".equals(procInsId)) {
            taskInstanceQuery.processInstanceId(procInsId);
        }
        if (category != null && !"".equals(category)) {
            taskInstanceQuery.processCategoryIn(Collections.singleton(category));
        }
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.listPage((pageNo - 1) * pageSize, pageSize);
        List<FlowTaskDto> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            FlowTaskDto flowTask = new FlowTaskDto();
            // ??????????????????
            flowTask.setTaskId(histTask.getId());
            // ??????????????????
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(getDate(histTask.getDurationInMillis()));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());
            // ??????????????????
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setCategory(pd.getCategory());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            //??????????????????
            ProcStrategy procStrategy = ProcContext.build(pd.getCategory());
            flowTask.setProcInsNo(procStrategy == null ? null : procStrategy.getProcNo(histTask.getProcessInstanceId()));
            flowTask.setTitle(procStrategy == null ? null : procStrategy.getProcTitle(histTask.getProcessInstanceId()));

            // ?????????????????????
            HistoricProcessInstance hiProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            SysUser startUser = sysUserService.selectUserById(Long.parseLong(hiProcIns.getStartUserId()));
            if (startUser != null) {
                flowTask.setStartUserId(String.valueOf(startUser.getUserId()));
                flowTask.setStartUserName(startUser.getNickName());
                flowTask.setStartDeptName(startUser.getDept().getDeptName());
            }
            hisTaskList.add(flowTask);
        }
        return hisTaskList;
    }

    /**
     * ????????????????????????
     * @param procInsId ????????????Id
     * @return
     */
    @Override
    public Response<Integer, List<FlowTaskDto>> getFlowRecords(String procInsId) {
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .orderByHistoricActivityInstanceStartTime()
                .desc()
                .list();
        List<FlowTaskDto> hisFlowList = new ArrayList<>();
        for (HistoricActivityInstance histIns : list) {
            if (!StringUtils.isNotBlank(histIns.getTaskId())) {
                continue;
            }
            FlowTaskDto flowTask = new FlowTaskDto();
            flowTask.setTaskId(histIns.getTaskId());
            flowTask.setTaskName(histIns.getActivityName());
            flowTask.setCreateTime(histIns.getStartTime());
            flowTask.setFinishTime(histIns.getEndTime());
            if (StringUtils.isNotBlank(histIns.getAssignee())) {
                SysUser sysUser = sysUserService.selectUserById(Long.parseLong(histIns.getAssignee()));
                if (sysUser != null) {
                    flowTask.setAssigneeId(sysUser.getUserId());
                    flowTask.setAssigneeName(sysUser.getNickName());
                    flowTask.setDeptName(sysUser.getDept().getDeptName());
                }
            }
            // ??????????????????
            List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(histIns.getTaskId());
            StringBuilder stringBuilder = new StringBuilder();
            for (HistoricIdentityLink identityLink : linksForTask) {
                if ("candidate".equals(identityLink.getType())) {
                    if (StringUtils.isNotBlank(identityLink.getUserId())) {
                        SysUser sysUser = sysUserService.selectUserById(Long.parseLong(identityLink.getUserId()));
                        if (sysUser != null) {
                            stringBuilder.append(sysUser.getNickName()).append(",");
                        }
                    }
                    if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                        SysRole sysRole = sysRoleService.selectRoleById(Long.parseLong(identityLink.getGroupId()));
                        if (sysRole != null) {
                            stringBuilder.append(sysRole.getRoleName()).append(",");
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(stringBuilder)) {
                flowTask.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
            }

            flowTask.setDuration(histIns.getDurationInMillis() == null || histIns.getDurationInMillis() == 0 ? null : getDate(histIns.getDurationInMillis()));
            // ????????????????????????
            List<Comment> commentList = taskService.getProcessInstanceComments(histIns.getProcessInstanceId());
            commentList.forEach(comment -> {
                if (histIns.getTaskId().equals(comment.getTaskId())) {
                    flowTask.setComment(FlowCommentDto.builder().type(comment.getType()).comment(comment.getFullMessage()).build());
                }
            });
            hisFlowList.add(flowTask);
        }
        return Response.create(HttpStatus.SUCCESS, "????????????", hisFlowList);
    }

    /**
     * ??????????????????
     */
    @Override
    public Response<Integer, Map<String, Object>> getFlowForm(String deployId) {
        SysForm sysForm = sysInstanceFormService.selectSysDeployFormByDeployId(deployId);
        if (Objects.isNull(sysForm)) {
            return Response.create(HttpStatus.ERROR, "????????????????????????");
        }
        return Response.create(HttpStatus.SUCCESS, "????????????", JSONObject.parseObject(sysForm.getFormContent()));
    }

    /**
     * ????????????ID???????????????????????????
     *
     * @param taskId ??????Id
     * @return
     */
    @Override
    public Task getTaskForm(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task;
    }

    /**
     * ?????????????????????
     *
     * @param processId
     * @return
     */
    @Override
    public InputStream diagram(String processId) {
        String processDefinitionId;
        // ???????????????????????????
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        // ????????????????????????????????????????????????
        if (Objects.isNull(processInstance)) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        } else {// ???????????????????????????????????????????????????
            // ??????????????????ID?????????????????????????????????ActivityId??????
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // ?????????????????????
        List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //?????????
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //?????????
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //????????????
                highLightedNodes.add(tempActivity.getActivityId());
            }
        }

        //???????????????
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        //??????????????????????????????
        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodes, highLightedFlows, configuration.getActivityFontName(),
                configuration.getLabelFontName(), configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);
        return in;
    }

    /**
     * ????????????????????????
     *
     * @param procInsId
     * @return
     */
    @Override
    public AjaxResult getFlowViewer(String procInsId) {
        List<FlowViewerDto> flowViewerList = new ArrayList<>();
        // ?????????????????????
        List<HistoricActivityInstance> hisActIns = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        for (HistoricActivityInstance activityInstance : hisActIns) {
            if (!"sequenceFlow".equals(activityInstance.getActivityType())) {
                FlowViewerDto flowViewerDto = new FlowViewerDto();
                flowViewerDto.setKey(activityInstance.getActivityId());
                flowViewerDto.setCompleted(!Objects.isNull(activityInstance.getEndTime()));
                flowViewerList.add(flowViewerDto);
            }
        }
        return AjaxResult.success(flowViewerList);
    }

    /**
     * ??????????????????
     *
     * @param taskId
     * @return
     */
    @Override
    public Response<Integer, Map<String, Object>> processVariables(String taskId) {
        // ????????????
        HistoricTaskInstance hiTaskIns = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .finished().taskId(taskId).singleResult();
        if (Objects.nonNull(hiTaskIns)) {
            Map<String, Object> variables = hiTaskIns.getProcessVariables();
            return Response.create(HttpStatus.SUCCESS, "????????????", variables);
        }
        Map<String, Object> variables = taskService.getVariables(taskId);
        return Response.create(HttpStatus.SUCCESS, "????????????", variables);
    }

    /**
     * ???????????????????????????
     * @param taskId
     * @return
     */
    @Override
    public Response<Integer, Boolean> checkIfProcessTaskStartNode(String taskId) {
        TaskInfo task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .finished().taskId(taskId).singleResult();
        if (task == null) {
            task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        }
        if (task == null) {
            return Response.create(HttpStatus.ERROR, "???????????????");
        }

        Map<String, Object> variables = task.getProcessVariables();
        if (variables == null) {
            return Response.create(HttpStatus.SUCCESS, "????????????", false);
        }
        Object startTaskKey = variables.get(ProcessConstants.PROCESS_START_TASK_KEY);
        return Response.create(HttpStatus.SUCCESS, "????????????", startTaskKey != null && startTaskKey.equals(task.getTaskDefinitionKey()));
    }

    /**
     * ??????????????????
     * @param taskId
     * @param variables
     * @return
     */
    @Override
    public Response<Integer, Void> restartTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return Response.create(HttpStatus.ERROR, "???????????????");
        }
        if (task.isSuspended()) {
            return Response.create(HttpStatus.ERROR, "???????????????");
        }

        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        String procInsId = task.getProcessInstanceId();
        taskService.addComment(taskId, procInsId, FlowComment.NORMAL.getType(), sysUser.getNickName() + "????????????????????????");
//        taskService.setAssignee(taskId, sysUser.getUserId().toString());
        taskService.complete(taskId, variables);
        return Response.create(HttpStatus.SUCCESS, "??????????????????");
    }

    /**
     * ??????????????????
     * @return
     */
    @Override
    public AjaxResult getNextFlowNode(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        FlowNextDto flowNextDto = new FlowNextDto();
        if (Objects.isNull(task)) {
            return AjaxResult.success(flowNextDto);
        }

        List<UserTask> nextUserTaskList = FindNextNodeUtil.getNextUserTasks(repositoryService, task, new HashMap<>());
        if (CollectionUtils.isEmpty(nextUserTaskList)) {
            return AjaxResult.success("???????????????", null);
        }
        for (UserTask userTask : nextUserTaskList) {
            MultiInstanceLoopCharacteristics multiInstance =  userTask.getLoopCharacteristics();
            // ????????????
            if (Objects.nonNull(multiInstance)) {
                List<SysUser> list = sysUserService.selectUserList(new SysUser());

                flowNextDto.setVars(ProcessConstants.PROCESS_MULTI_INSTANCE_USER);
                flowNextDto.setType(ProcessConstants.PROCESS_MULTI_INSTANCE);
                flowNextDto.setUserList(list);
            } else {

                // ??????????????????????????? ????????????????????????????????????????????????????????????
                String dataType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_DATA_TYPE);
                String userType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_USER_TYPE);

                if (ProcessConstants.DATA_TYPE.equals(dataType)) {
                    // ??????????????????
                    if (ProcessConstants.USER_TYPE_ASSIGNEE.equals(userType)) {
                        List<SysUser> list = sysUserService.selectUserList(new SysUser());

                        flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
                        flowNextDto.setType(ProcessConstants.USER_TYPE_ASSIGNEE);
                        flowNextDto.setUserList(list);
                    }
                    // ????????????(??????)
                    if (ProcessConstants.USER_TYPE_USERS.equals(userType)) {
                        List<SysUser> list = sysUserService.selectUserList(new SysUser());

                        flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
                        flowNextDto.setType(ProcessConstants.USER_TYPE_USERS);
                        flowNextDto.setUserList(list);
                    }
                    // ?????????
                    if (ProcessConstants.USER_TYPE_ROUPS.equals(userType)) {
                        List<SysRole> sysRoles = sysRoleService.selectRoleAll();

                        flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
                        flowNextDto.setType(ProcessConstants.USER_TYPE_ROUPS);
                        flowNextDto.setRoleList(sysRoles);
                    }
                }
            }
        }
        return AjaxResult.success(flowNextDto);
    }

    /**
     * ????????????????????????
     *
     * @param ms
     * @return
     */
    private String getDate(long ms) {

        long day = ms / (24 * 60 * 60 * 1000);
        long hour = (ms / (60 * 60 * 1000) - day * 24);
        long minute = ((ms / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (ms / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

        if (day > 0) {
            return day + "???" + hour + "??????" + minute + "??????";
        }
        if (hour > 0) {
            return hour + "??????" + minute + "??????";
        }
        if (minute > 0) {
            return minute + "??????";
        }
        if (second > 0) {
            return second + "???";
        } else {
            return 0 + "???";
        }
    }

    @Override
    public List<String> getNextFlowNode(FlowTaskVo flowTaskVo,long deptUserId,long startUserId) {
        List<String> weComNoList = new ArrayList<>();
        // Step 1. ??????????????????????????????????????????
        Task task = taskService.createTaskQuery().taskId(flowTaskVo.getTaskId()).singleResult();
        if (Objects.nonNull(task)) {
            // Step 2. ????????????????????????????????????(????????????????????????????????????)
            Map<String, Object> variables = taskService.getVariables(task.getId());
            List<UserTask> nextUserTaskList = FindNextNodeUtil.getNextUserTasks(repositoryService, task, variables);
            if (CollectionUtils.isEmpty(nextUserTaskList)) {
                return weComNoList;
            }
            UserTask userTask = nextUserTaskList.get(0);
            MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();
            // ????????????
            if (Objects.nonNull(multiInstance)) {
                List<SysUser> list = sysUserService.selectUserList(new SysUser());
            } else {
                if (userTask.getAssignee() != null) {
                    if (userTask.getAssignee().equals(ProcessConstants.DEPT_LEADER_2)) {
                        SysUser sysUser = sysUserService.selectUserById(deptUserId);
                        if (sysUser != null) {
                            weComNoList.add(sysUser.getWeComNo());
                        }
                    } else {
                        String id = userTask.getAssignee();
                        if (!StringUtils.isEmpty(id)) {
                            SysUser sysUser = sysUserService.selectUserById(Long.valueOf(id));
                            if (sysUser != null) {
                                weComNoList.add(sysUser.getWeComNo());
                            }
                        }
                    }
                }
                // ????????????(??????)
                if (userTask.getCandidateUsers() != null && userTask.getCandidateUsers().size() > 0) {
                    System.out.println(userTask.getCandidateUsers().get(0));
                    if (userTask.getCandidateUsers().get(0).equals(ProcessConstants.PROCESS_INITIATOR_2)) {
                        SysUser sysUser = sysUserService.selectUserById(startUserId);
                        if (sysUser != null) {
                            weComNoList.add(sysUser.getWeComNo());
                        }
                    } else {
                        List<String> users = userTask.getCandidateUsers();
                        for (String user : users) {
                            SysUser sysUser = sysUserService.selectUserById(Long.valueOf(user));
                            if (sysUser != null) {
                                weComNoList.add(sysUser.getWeComNo());
                            }
                        }
                    }
                }
                // ?????????
                if (userTask.getCandidateGroups() != null) {
                    List<String> groups = userTask.getCandidateGroups();
                    for (String roleId : groups) {
                        List<SysUserRole> userRoles = userRoleMapper.selectUserRole(roleId);
                        if (userRoles != null) {
                            for (SysUserRole sur : userRoles) {
                                SysUser sysUser = sysUserService.selectUserById(sur.getUserId());
                                weComNoList.add(sysUser.getWeComNo());
                            }
                        }
                    }
                }
            }
        }
            return weComNoList;
        }

}