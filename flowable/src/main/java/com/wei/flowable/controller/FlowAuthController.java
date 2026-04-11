package com.wei.flowable.controller;


import com.wei.common.constant.HttpStatus;
import com.wei.common.utils.SecurityUtils;
import com.wei.system.domain.vo.*;
import com.wei.system.service.FlowAuthService;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author yuwei
 */
@Slf4j
@Api(tags = "流程权限")
@Controller
public class FlowAuthController {
    @Resource
    private FlowAuthService flowAuthService;


    @PreAuthorize("@ss.hasPermi('flow:auth:widget:list')")
    @ApiOperation(value = "权限组件列表", notes = "......")
    @RequestMapping(value = "/flowauth/widget/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<FlowAuthWidgetVo>> queryFlowAuthWidgetList(
            @RequestParam(value = "procCategory", required = true) String procCategory
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = flowAuthService.queryFlowAuthWidgetCount(procCategory);
        List<FlowAuthWidgetVo> list = flowAuthService.queryFlowAuthWidgetList(procCategory, pageNo, pageSize);

        PageResponseVo<FlowAuthWidgetVo> pageVo = PageResponseVo.<FlowAuthWidgetVo>builder()
                .size(size)
                .list(list)
                .build();
        return Response.of(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('flow:auth:widget:create')")
    @ApiOperation(value = "创建权限组件", notes = "......")
    @RequestMapping(value = "/flowauth/widget/create", method = POST)
    @ResponseBody
    public Response<Integer, Long> createFlowAuthWidget(@RequestParam(value = "procCategory", required = true) String procCategory
            , @RequestParam(value = "code", required = true) String code
            , @RequestParam(value = "name", required = true) String name) {
        String userName = SecurityUtils.getUsername();
        long id = flowAuthService.createFlowAuthWidget(procCategory, code, name, userName);
        return Response.of(HttpStatus.SUCCESS, "ok", id);
    }

    @PreAuthorize("@ss.hasPermi('flow:auth:widget:update')")
    @ApiOperation(value = "修改权限组件", notes = "......")
    @RequestMapping(value = "/flowauth/widget/update", method = {POST, PUT})
    @ResponseBody
    public Response<Integer, Void> updateFlowAuthWidget(@RequestParam(value = "id", required = true) long id
            , @RequestParam(value = "procCategory", required = true) String procCategory
            , @RequestParam(value = "code", required = true) String code
            , @RequestParam(value = "name", required = true) String name) {
        String userName = SecurityUtils.getUsername();
        flowAuthService.updateFlowAuthWidget(id, procCategory, code, name, userName);
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

    @PreAuthorize("@ss.hasPermi('flow:auth:widget:delete')")
    @ApiOperation(value = "删除权限组件", notes = "......")
    @RequestMapping(value = "/flowauth/widget/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteFlowAuthWidget(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long widgetId: ids) {
            flowAuthService.deleteFlowAuthWidget(widgetId);
        }
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

//    @PreAuthorize("@ss.hasPermi('flow:auth:node:list')")
    @ApiOperation(value = "权限节点列表", notes = "......")
    @RequestMapping(value = "/flowauth/node/list", method = GET)
    @ResponseBody
    public Response<Integer, List<FlowAuthNodeVo>> queryFlowAuthNodeList(
            @RequestParam(value = "deployId", required = true) String deployId) {
        List<FlowAuthNodeVo> list = flowAuthService.queryFlowAuthNodeList(deployId);
        return Response.of(HttpStatus.SUCCESS, "ok", list);
    }

    //    @PreAuthorize("@ss.hasPermi('flow:auth:node:delete')")
    @ApiOperation(value = "删除权限节点配置", notes = "......")
    @RequestMapping(value = "/flowauth/node/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteFlowAuthNode(@RequestParam(value = "procDefId", required = true) String procDefId
            , @RequestParam(value = "nodeKeys", required = true) String[] nodeKeys) {
        for (String nodeKey: nodeKeys) {
            flowAuthService.deleteFlowAuthNode(procDefId, nodeKey);
        }
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

    //    @PreAuthorize("@ss.hasPermi('flow:auth:node:setting:list')")
    @ApiOperation(value = "节点权限配置列表", notes = "......")
    @RequestMapping(value = "/flowauth/node/setting/list", method = GET)
    @ResponseBody
    public Response<Integer, List<FlowAuthNodeSettingVo>> queryFlowAuthNodeSettingList(
            @RequestParam(value = "procDefId", required = true) String procDefId
            , @RequestParam(value = "nodeKey", required = true) String nodeKey) {
        List<FlowAuthNodeSettingVo> list = flowAuthService.queryFlowAuthNodeSettingList(procDefId, nodeKey);
        return Response.of(HttpStatus.SUCCESS, "ok", list);
    }

    //    @PreAuthorize("@ss.hasPermi('flow:auth:node:setting:save')")
    @ApiOperation(value = "保存节点权限配置", notes = "......")
    @RequestMapping(value = "/flowauth/node/setting/{procDefId}/{nodeKey}", method = POST)
    @ResponseBody
    public Response<Integer, Void> saveFlowAuthNodeSetting(@PathVariable(value = "procDefId") String procDefId
            , @PathVariable(value = "nodeKey") String nodeKey, @RequestBody List<FlowAuthNodeSettingRequestVo> dataList) {
        String userName = SecurityUtils.getUsername();
        flowAuthService.saveFlowAuthNodeSetting(procDefId, nodeKey, dataList, userName);
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

    //    @PreAuthorize("@ss.hasPermi('flow:auth:node:setting:delete')")
    @ApiOperation(value = "删除节点权限配置", notes = "......")
    @RequestMapping(value = "/flowauth/node/setting/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteFlowAuthNodeSetting(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long settingId: ids) {
            flowAuthService.deleteFlowAuthNodeSetting(settingId);
        }
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

    //    @PreAuthorize("@ss.hasPermi('flow:auth:node:task:setting:list')")
    @ApiOperation(value = "根据taskId查询权限配置列表", notes = "......")
    @RequestMapping(value = "/flowauth/node/task/setting/list", method = GET)
    @ResponseBody
    public Response<Integer, List<FlowAuthNodeTaskSettingVo>> queryFlowAuthSettingListByTaskId(
            @RequestParam(value = "taskId", required = true) String taskId) {
        List<FlowAuthNodeTaskSettingVo> list = flowAuthService.queryFlowAuthSettingListByTaskId(taskId);
        return Response.of(HttpStatus.SUCCESS, "ok", list);
    }

    @ApiOperation(value = "根据taskId查询起始节点权限列表", notes = "......")
    @RequestMapping(value = "/flowauth/node/start-task/setting/list", method = GET)
    @ResponseBody
    public Response<Integer, List<FlowAuthNodeTaskSettingVo>> queryStartNodeFlowAuthSettingList(
            @RequestParam(value = "taskId", required = true) String taskId) {
        List<FlowAuthNodeTaskSettingVo> list = flowAuthService.queryStartNodeFlowAuthSettingListByTaskId(taskId);
        return Response.of(HttpStatus.SUCCESS, "ok", list);
    }
}
