package com.wei.system.service.impl;


import com.wei.common.constant.ProcessConstants;
import com.wei.common.utils.FlowBeanUtil;
import com.wei.system.domain.*;
import com.wei.system.domain.vo.*;
import com.wei.system.mapper.*;
import com.wei.system.service.FlowAuthService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.sequence.base.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.TaskInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
@Slf4j
@Service("flowAuthService")
public class FlowAuthServiceImpl implements FlowAuthService {
	@Resource
	private RepositoryService repositoryService;
	@Resource
	private TaskService taskService;
	@Resource
	private HistoryService historyService;

	@Resource
	private FlowAuthWidgetMapper flowAuthWidgetMapper;
	@Resource
	private FlowAuthNodeSettingMapper flowAuthNodeSettingMapper;

	@Resource
	private Sequence seqFlowAuthWidget;
	@Resource
	private Sequence seqFlowAuthNodeSetting;


	@Override
	public int queryFlowAuthWidgetCount(String procCategory) {
		return flowAuthWidgetMapper.queryWidgetCount(procCategory);
	}

	@Override
	public List<FlowAuthWidgetVo> queryFlowAuthWidgetList(String procCategory, int pageNo, int pageSize) {
		List<FlowAuthWidget> list = flowAuthWidgetMapper.queryWidgetList(procCategory, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toFlowAuthWidgetVo).collect(Collectors.toList());
	}

	private FlowAuthWidgetVo toFlowAuthWidgetVo(FlowAuthWidget widget) {
		if (widget == null) {
			return null;
		}
		FlowAuthWidgetVo vo = new FlowAuthWidgetVo();
		vo.setId(widget.getId());
		vo.setProcCategory(widget.getProcCategory());
		vo.setCode(widget.getCode());
		vo.setName(widget.getName());
		vo.setCreator(widget.getCreator());
		vo.setCreateTime(widget.getCreateTime() == null ? "" : DateUtil.formatDate(widget.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(widget.getModifier());
		vo.setModifyTime(widget.getModifyTime() == null ? "" : DateUtil.formatDate(widget.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Transactional
	@Override
	public long createFlowAuthWidget(String procCategory, String code, String name, String creator) {
		procCategory = procCategory == null ? null : procCategory.trim();
		code = code == null ? null : code.trim();
		name = name == null ? null : name.trim();
		FlowAuthWidget widget = flowAuthWidgetMapper.findWidget(procCategory, code);
		if (widget != null) {
			throw new RuntimeException("数据[procCategory=" + procCategory + ", code=" + code + "]已存在");
		}

		widget = new FlowAuthWidget();
		widget.setId(seqFlowAuthWidget.next());
		widget.setProcCategory(procCategory);
		widget.setCode(code);
		widget.setName(name);
		widget.setCreator(creator);
		widget.setCreateTime(new Date());
		flowAuthWidgetMapper.insertSelective(widget);
		return widget.getId();
	}

	@Transactional
	@Override
	public void updateFlowAuthWidget(long id, String procCategory, String code, String name, String modifier) {
		procCategory = procCategory == null ? null : procCategory.trim();
		code = code == null ? null : code.trim();
		name = name == null ? null : name.trim();
		FlowAuthWidget widget = flowAuthWidgetMapper.selectOneById(id, FlowAuthWidget.class);
		if (widget == null) {
			throw new RuntimeException("数据不存在[id=" + id + "]");
		}

		FlowAuthWidget widget0 = flowAuthWidgetMapper.findWidget(procCategory, code);
		if (widget0 != null && widget0.getId() != id) {
			throw new RuntimeException("数据[procCategory=" + procCategory + ", code=" + code + "]已存在");
		}

		widget.setProcCategory(procCategory);
		widget.setCode(code);
		widget.setName(name);
		widget.setModifier(modifier);
		widget.setModifyTime(new Date());
		int i = flowAuthWidgetMapper.updateByPrimaryKey(widget);
		if (i < 0) {
			throw new RuntimeException("修改失败!");
		}
	}

	@Transactional
	@Override
	public void deleteFlowAuthWidget(long id) {
		flowAuthWidgetMapper.deleteByKey(id, FlowAuthWidget.class);
	}

	@Override
	public List<FlowAuthNodeVo> queryFlowAuthNodeList(String deployId) {
		List<FlowAuthNodeVo> voList = new ArrayList<>();
		ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
		if (procDef == null) {
			return voList;
		}
		String procDefId = procDef.getId();

		/**
		 * 查询流程定义中的节点
		 */
		List<FlowNode> flowNodeList = getFlowNodeList(procDefId);
		for (FlowNode flowNode : flowNodeList) {
			voList.add(FlowAuthNodeVo.builder().procDefId(procDefId).nodeKey(flowNode.key).nodeName(flowNode.name).valid(true).build());
		}

		/**
		 * 查询已配置的权限节点
		 */
		List<String> nodeKeyList = flowAuthNodeSettingMapper.findNodeKeyListByProcDefId(procDefId);
		for (String nodeKey : nodeKeyList) {
			FlowAuthNodeVo vo = FlowAuthNodeVo.builder().procDefId(procDefId).nodeKey(nodeKey).build();
			if (voList.contains(vo)) {
				continue;
			}
			List<FlowAuthNodeSetting> settings = flowAuthNodeSettingMapper.queryNodeSettingList(procDefId, nodeKey, 1, 1);
			String nodeName = settings == null || settings.size() <= 0 ? "Unknown" : settings.get(0).getNodeName();
			voList.add(FlowAuthNodeVo.builder().procDefId(procDefId).nodeKey(nodeKey).nodeName(nodeName).valid(false).build());
		}
		return voList;
	}

	private List<FlowNode> getFlowNodeList(String procDefId) {
		BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
		if (bpmnModel == null) {
			return new ArrayList<>();
		}

		Process process = bpmnModel.getProcesses().get(0);
		Collection<FlowElement> flowElements = process.getFlowElements();
		if (flowElements == null) {
			return new ArrayList<>();
		}
		return flowElements.stream().filter(ele -> ele instanceof UserTask).map(ele -> {
			UserTask uTask = (UserTask) ele;
			return new FlowNode(uTask.getId(), uTask.getName());
		}).collect(Collectors.toList());
	}

	private static class FlowNode {
		private String key;
		private String name;

		FlowNode(String key, String name) {
			this.key = key;
			this.name = name;
		}
	}

	@Transactional
	@Override
	public void deleteFlowAuthNode(String procDefId, String nodeKey) {
		flowAuthNodeSettingMapper.deleteFlowAuthNode(procDefId, nodeKey);
	}

	@Override
	public List<FlowAuthNodeSettingVo> queryFlowAuthNodeSettingList(String procDefId, String nodeKey) {
		List<FlowAuthNodeSettingVo> voList = new ArrayList<>();
		ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
		if (procDef == null) {
			return voList;
		}
		String procCategory = procDef.getCategory();
		UserTask userTask = getUserTaskFromProc(procDefId, nodeKey);

		/**
		 * 根据流程分类查询权限组件列表
		 */
		List<FlowAuthWidget> widgetList = flowAuthWidgetMapper.queryWidgetList(procCategory, null, null);
		for (FlowAuthWidget widget : widgetList) {
			FlowAuthNodeSetting nodeSetting = flowAuthNodeSettingMapper.findWNodeSetting(procDefId, nodeKey, widget.getId());
			FlowAuthNodeSettingVo vo = new FlowAuthNodeSettingVo();
			if (nodeSetting != null) {
				vo.setId(nodeSetting.getId());
			}
			vo.setProcDefId(procDefId);
			vo.setNodeKey(nodeKey);
			vo.setNodeName(userTask == null ? nodeKey : userTask.getName());
			vo.setWidgetId(widget.getId());
			vo.setWidgetCode(widget.getCode());
			vo.setWidgetName(widget.getName());
			if (nodeSetting != null) {
				vo.setViewable(nodeSetting.isViewable());
				vo.setEditable(nodeSetting.isEditable());
			}
			vo.setValid(true);
			voList.add(vo);
		}

		/**
		 * 查询已配置的权限组件
		 */
		List<FlowAuthNodeSetting> settingList = flowAuthNodeSettingMapper.queryNodeSettingList(procDefId, nodeKey, null, null);
		for (FlowAuthNodeSetting setting : settingList) {
			FlowAuthNodeSettingVo vo2 = FlowAuthNodeSettingVo.builder().procDefId(procDefId).nodeKey(nodeKey).widgetId(setting.getWidgetId()).build();
			if (voList.contains(vo2)) {
				continue;
			}
			FlowAuthWidget widget = flowAuthWidgetMapper.selectOneById(setting.getWidgetId(), FlowAuthWidget.class);
			FlowAuthNodeSettingVo vo = new FlowAuthNodeSettingVo();
			vo.setId(setting.getId());
			vo.setProcDefId(procDefId);
			vo.setNodeKey(nodeKey);
			vo.setNodeName(userTask == null ? nodeKey : userTask.getName());
			vo.setWidgetId(setting.getWidgetId());
			vo.setWidgetCode(widget == null ? null : widget.getCode());
			vo.setWidgetName(widget == null ? null : widget.getName());
			vo.setViewable(setting.isViewable());
			vo.setEditable(setting.isEditable());
			vo.setValid(false);
			voList.add(vo);
		}
		return voList;
	}

	private UserTask getUserTaskFromProc(String procDefId, String nodeKey) {
		BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);
		if (bpmnModel == null) {
			return null;
		}

		Process process = bpmnModel.getProcesses().get(0);
		Collection<FlowElement> flowElements = process.getFlowElements();
		if (flowElements == null) {
			return null;
		}
		for (FlowElement flowElement : flowElements) {
			if (!(flowElement instanceof UserTask)) {
				continue;
			}
			UserTask uTask = (UserTask) flowElement;
			if (uTask.getId().equals(nodeKey)) {
				return uTask;
			}
		}
		return null;
	}

	@Transactional
	@Override
	public void saveFlowAuthNodeSetting(String procDefId, String nodeKey, List<FlowAuthNodeSettingRequestVo> dataList, String creator) {
		UserTask userTask = getUserTaskFromProc(procDefId, nodeKey);
		String nodeName = userTask == null ? nodeKey : userTask.getName();

		List<FlowAuthNodeSetting> existList = flowAuthNodeSettingMapper.queryNodeSettingList(procDefId, nodeKey, null, null);
		/**
		 * 之前已经存在的记录，如果仍在待更新的记录列表中，则保留，否则删除
		 */
		List<Long> widgetIdList = (dataList == null || dataList.size() <= 0)
				? new ArrayList<>()
				: dataList.stream().map(FlowAuthNodeSettingRequestVo::getWidgetId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(existList)) {
			for (FlowAuthNodeSetting itm : existList) {
				boolean exists = FlowBeanUtil.exists(widgetIdList, itm.getWidgetId());
				if (!exists) {
					flowAuthNodeSettingMapper.delete(itm);
				}
			}
		}

		/**
		 * 增加新纪录
		 */
		if (dataList != null && dataList.size() > 0) {
			Date now = new Date();
			for (FlowAuthNodeSettingRequestVo vo : dataList) {
				FlowAuthNodeSetting setting = flowAuthNodeSettingMapper.findWNodeSetting(procDefId, nodeKey, vo.getWidgetId());
				if (setting != null) {
					this.updateFlowAuthNodeSetting(setting, nodeName, vo, creator, now);
				} else {
					this.createFlowAuthNodeSetting(procDefId, nodeKey, nodeName, vo, creator, now);
				}
			}
		}
	}

	private void createFlowAuthNodeSetting(String procDefId, String nodeKey, String nodeName, FlowAuthNodeSettingRequestVo vo
			, String creator, Date date) {
		long settingId = seqFlowAuthNodeSetting.next();
		FlowAuthNodeSetting setting = new FlowAuthNodeSetting();
		setting.setId(settingId);
		setting.setProcDefId(procDefId);
		setting.setNodeKey(nodeKey);
		setting.setNodeName(nodeName);
		setting.setWidgetId(vo.getWidgetId());
		setting.setViewable(vo.getViewable());
		setting.setEditable(vo.getEditable());
		setting.setCreator(creator);
		setting.setCreateTime(date);
		flowAuthNodeSettingMapper.insert(setting);

	}

	private void updateFlowAuthNodeSetting(FlowAuthNodeSetting setting, String nodeName, FlowAuthNodeSettingRequestVo vo
			, String modifier, Date date) {
		setting.setNodeName(nodeName);
		setting.setWidgetId(vo.getWidgetId());
		setting.setViewable(vo.getViewable());
		setting.setEditable(vo.getEditable());
		setting.setModifier(modifier);
		setting.setModifyTime(date);
		flowAuthNodeSettingMapper.updateByPrimaryKey(setting);
	}

	@Transactional
	@Override
	public void deleteFlowAuthNodeSetting(long settingId) {
		flowAuthNodeSettingMapper.deleteByKey(settingId, FlowAuthNodeSetting.class);
	}

	@Override
	public List<FlowAuthNodeTaskSettingVo> queryFlowAuthSettingListByTaskId(String taskId) {
		TaskInfo task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).finished().singleResult();
		if (task == null) {
			task = taskService.createTaskQuery().taskId(taskId).singleResult();
		}

		if (task == null) {
			return new ArrayList<>();
		}
		return queryFlowAuthSettingList(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
	}

	private List<FlowAuthNodeTaskSettingVo> queryFlowAuthSettingList(String procDefId, String nodeKey) {
		List<FlowAuthNodeSetting> settingList = flowAuthNodeSettingMapper.queryNodeSettingList(procDefId, nodeKey, null, null);
		return settingList == null || settingList.size() <= 0
				? new ArrayList<>()
				: settingList.stream().map(st -> {
					FlowAuthWidget widget = flowAuthWidgetMapper.selectOneById(st.getWidgetId(), FlowAuthWidget.class);
					if (widget == null) {
						return null;
					}
					return FlowAuthNodeTaskSettingVo.builder().widgetCode(widget.getCode())
							.widgetName(widget.getName())
							.viewable(st.isViewable())
							.editable(st.isEditable())
							.build();
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public List<FlowAuthNodeTaskSettingVo> queryStartNodeFlowAuthSettingListByTaskId(String taskId) {
		TaskInfo task = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
				.finished().taskId(taskId).singleResult();
		if (task == null) {
			task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
		}
		if (task == null) {
			return new ArrayList<>();
		}
		Map<String, Object> variables = task.getProcessVariables();
		if (variables == null) {
			return new ArrayList<>();
		}
		String startNodeKey = (String) variables.get(ProcessConstants.PROCESS_START_TASK_KEY);
		return queryFlowAuthSettingList(task.getProcessDefinitionId(), startNodeKey);
	}
}