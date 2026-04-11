package com.wei.flowable.service.impl;


import com.wei.common.constant.HttpStatus;
import com.wei.common.constant.ProcessConstants;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.core.domain.entity.SysDept;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.flowable.common.enums.FlowComment;
import com.wei.common.utils.SecurityUtils;
import com.wei.flowable.domain.dto.FlowProcDefDto;
import com.wei.flowable.domain.vo.FlowTaskVo;
import com.wei.flowable.factory.FlowServiceFactory;
import com.wei.flowable.service.IFlowDefinitionService;
import com.wei.flowable.service.IFlowTaskService;
import com.wei.flowable.service.ISysDeployFormService;
import com.wei.system.domain.SysForm;
import com.wei.system.service.*;
import com.wei.system.strategy.ProcContext;
import com.wei.system.strategy.ProcStrategy;
import com.yuweix.kuafu.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 流程定义
 *
 * @author XuanXuan
 * @date 2021-04-03
 */
@Service
@Slf4j
public class FlowDefinitionServiceImpl extends FlowServiceFactory implements IFlowDefinitionService {
    @Resource
    private ISysDeployFormService sysDeployFormService;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysDeptService sysDeptService;
    @Resource
    private ISysPostService postService;
    @Resource
    private OrderService orderService;
    @Resource
    private IFlowTaskService flowTaskService;
    @Resource
    private QiyeWeixinService weixinService;

    private static final String BPMN_FILE_SUFFIX = ".bpmn";

    @Override
    public boolean exist(String processDefinitionKey) {
        ProcessDefinitionQuery processDefinitionQuery
                = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        long count = processDefinitionQuery.count();
        return count > 0;
    }


    @Override
    public int queryProcDefCount(String deploymentId, String category) {
        ProcessDefinitionQuery procDefQuery = repositoryService.createProcessDefinitionQuery();
        if (deploymentId != null && !"".equals(deploymentId)) {
            procDefQuery.deploymentId(deploymentId);
        }
        if (category != null && !"".equals(category)) {
            procDefQuery.processDefinitionCategory(category);
        }
        return (int) procDefQuery.count();
    }
    /**
     * 流程定义列表
     *
     * @param pageNo  当前页码
     * @param pageSize 每页条数
     * @return 流程定义分页列表数据
     */
    @Override
    public List<FlowProcDefDto> queryProcDefList(String deploymentId, String category, int pageNo, int pageSize) {
        // 流程定义列表数据查询
        ProcessDefinitionQuery procDefQuery = repositoryService.createProcessDefinitionQuery();
        if (deploymentId != null && !"".equals(deploymentId)) {
            procDefQuery.deploymentId(deploymentId);
        }
        if (category != null && !"".equals(category)) {
            procDefQuery.processDefinitionCategory(category);
        }
        procDefQuery.orderByProcessDefinitionVersion().desc().orderByProcessDefinitionKey().asc();
        List<ProcessDefinition> procDefList = procDefQuery.listPage((pageNo - 1) * pageSize, pageSize);

        return procDefList == null || procDefList.size() <= 0
                ? new ArrayList<>()
                : procDefList.stream().map(this::toFlowProcDefDto).collect(Collectors.toList());
    }
    private FlowProcDefDto toFlowProcDefDto(ProcessDefinition procDef) {
        if (procDef == null) {
            return null;
        }

        FlowProcDefDto dto = new FlowProcDefDto();
        String deployId = procDef.getDeploymentId();
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deployId).singleResult();
        BeanUtils.copyProperties(procDef, dto);
        SysForm sysForm = sysDeployFormService.selectSysDeployFormByDeployId(deployId);
        if (Objects.nonNull(sysForm)) {
            dto.setFormId(sysForm.getId());
            dto.setFormType(sysForm.getFormType());
            dto.setFormName(sysForm.getFormName());
        }
        // 流程定义时间
        dto.setDeploymentTime(deployment.getDeploymentTime());
        return dto;
    }

    @Override
    public FlowProcDefDto getProcDefInfoByDeployId(String deployId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        return toFlowProcDefDto(procDef);
    }


    /**
     * 导入流程文件
     *
     * @param name
     * @param category
     * @param in
     */
    @Override
    public void importFile(String name, String category, InputStream in) {
        Deployment deploy = repositoryService.createDeployment().addInputStream(name + BPMN_FILE_SUFFIX, in).name(name).category(category).deploy();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        repositoryService.setProcessDefinitionCategory(definition.getId(), category);
    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public AjaxResult readXml(String deployId) throws IOException {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName());
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        return AjaxResult.success("", result);
    }

    /**
     * 读取xml
     *
     * @param deployId
     * @return
     */
    @Override
    public InputStream readImage(String deployId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        //获得图片流
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        //输出为图片
        return diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                Collections.emptyList(),
                Collections.emptyList(),
                "宋体",
                "宋体",
                "宋体",
                null,
                1.0,
                false);

    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程定义Id
     * @param variables 流程变量
     * @return
     */
    @Override
    public Response<Integer, Void> startProcessInstanceById(String procDefId, Map<String, Object> variables) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId)
                .singleResult();
        if (procDef == null) {
            return Response.of(HttpStatus.ERROR, "流程定义[" + procDefId + "]不存在");
        }
        if (procDef.isSuspended()) {
            return Response.of(HttpStatus.ERROR, "流程已被挂起，请先激活流程");
        }
//           variables.put("skip", true);
//           variables.put(ProcessConstants.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);
        // 设置流程发起人Id到流程中
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        long startUserId = sysUser.getUserId();
        identityService.setAuthenticatedUserId("" + startUserId);
        variables.put(ProcessConstants.PROCESS_INITIATOR, startUserId);

        Long deptId = sysUser.getDeptId();
        SysDept sysDept = sysDeptService.selectDeptById(deptId);
        long deptUserId = sysDept == null || sysDept.getLeaderId() == null ? startUserId : sysDept.getLeaderId();
        variables.put(ProcessConstants.DEPT_LEADER, deptUserId);
        ProcessInstance procIns = runtimeService.startProcessInstanceById(procDefId, variables);
        String procInsId = procIns.getProcessInstanceId();
        ProcStrategy procStrategy = ProcContext.build(procDef.getCategory());
        if (procStrategy != null) {
            Response<Boolean, Void> resp = procStrategy.save(procInsId, variables, startUserId);
            if (!resp.getCode()) {
                return Response.of(HttpStatus.ERROR, resp.getMsg());
            }
        }
        // 给第一步申请人节点设置任务执行人和意见 todo:第一个节点不设置为申请人节点有点问题？
        Task task = taskService.createTaskQuery().processInstanceId(procInsId).singleResult();
        FlowTaskVo flowTaskVo = new FlowTaskVo();
        flowTaskVo.setTaskId(task.getId());
        flowTaskVo.setInstanceId(procIns.getProcessInstanceId());
        if (Objects.nonNull(task)) {
            List<String> nextWeComNoList = flowTaskService.getNextFlowNode(flowTaskVo, deptUserId, startUserId);
            String title = "你有一条标题为" + variables.get("title") + "的流程任务待处理";
            weixinService.sendNewsMessage(nextWeComNoList, title);
            taskService.addComment(task.getId(), procInsId, FlowComment.NORMAL.getType()
                    , sysUser.getNickName() + "发起流程申请");
            taskService.setAssignee(task.getId(), "" + startUserId);
            taskService.setVariable(task.getId(), ProcessConstants.PROCESS_START_TASK_KEY, task.getTaskDefinitionKey());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("{}", e.getMessage());
            }
            taskService.complete(task.getId(), variables);
        }
        return Response.of(HttpStatus.SUCCESS, "流程启动成功");
    }


    /**
     * 激活或挂起流程定义
     *
     * @param state    状态
     * @param deployId 流程部署ID
     */
    @Override
    public void updateState(Integer state, String deployId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        // 激活
        if (state == 1) {
            repositoryService.activateProcessDefinitionById(procDef.getId(), true, null);
        }
        // 挂起
        if (state == 2) {
            repositoryService.suspendProcessDefinitionById(procDef.getId(), true, null);
        }
    }


    /**
     * 删除流程定义
     *
     * @param deployIds 流程部署ID act_ge_bytearray 表中 deployment_id值
     */
    @Override
    public void delete(String[] deployIds) {
        if (deployIds == null || deployIds.length <= 0) {
            return;
        }
        for (String deployId : deployIds) {
            // true 允许级联删除 ,不设置会导致数据库外键关联异常
            repositoryService.deleteDeployment(deployId, true);
        }
    }

    @Override
    public String getProcDefIdByDeployId(String deployId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
        return procDef == null ? null : procDef.getId();
    }
}
