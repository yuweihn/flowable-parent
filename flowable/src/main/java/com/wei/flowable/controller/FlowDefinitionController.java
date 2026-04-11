package com.wei.flowable.controller;


import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.core.domain.entity.SysRole;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.flowable.domain.dto.FlowProcDefDto;
import com.wei.flowable.service.IFlowDefinitionService;
import com.wei.system.domain.vo.PageResponseVo;
import com.wei.system.service.ISysRoleService;
import com.wei.system.service.ISysUserService;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;


/**
 * <p>
 * 工作流程定义
 * </p>
 *
 * @author XuanXuan
 * @date 2021-04-03
 */
@Slf4j
@Api(tags = "流程定义")
@RestController
public class FlowDefinitionController {
    @Autowired
    private IFlowDefinitionService flowDefinitionService;
    @Autowired
    private ISysUserService userService;
    @Resource
    private ISysRoleService sysRoleService;


    @ApiOperation(value = "流程定义列表", response = FlowProcDefDto.class)
    @RequestMapping(value = "/flowable/definition/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<FlowProcDefDto>> queryProcDefList(
            @RequestParam(value = "deploymentId", required = false) String deploymentId
            , @RequestParam(value = "category", required = false) String category
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = flowDefinitionService.queryProcDefCount(deploymentId, category);
        List<FlowProcDefDto> list = flowDefinitionService.queryProcDefList(deploymentId, category, pageNo, pageSize);

        PageResponseVo<FlowProcDefDto> pageVo = PageResponseVo.<FlowProcDefDto>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @ApiOperation(value = "根据deployId查询详情", notes = "......")
    @RequestMapping(value = "/flowable/definition/info/by-deploy-id", method = GET)
    @ResponseBody
    public Response<Integer, FlowProcDefDto> getProcDefInfoByDeployId(
            @RequestParam(value = "deployId", required = true) String deployId) {
        FlowProcDefDto dto = flowDefinitionService.getProcDefInfoByDeployId(deployId);
        if (dto == null) {
            return Response.of(HttpStatus.ERROR, "流程定义不存在");
        }
        return Response.of(HttpStatus.SUCCESS, "ok", dto);
    }


    @ApiOperation(value = "导入流程文件", notes = "上传bpmn20的xml文件")
    @RequestMapping(value = "/flowable/definition/import", method = POST)
    @ResponseBody
    public AjaxResult importFile(@RequestParam(required = false) String name
            , @RequestParam(required = false) String category, MultipartFile file) {
        InputStream in = null;
        try {
            in = file.getInputStream();
            flowDefinitionService.importFile(name, category, in);
        } catch (Exception e) {
            log.error("导入失败:", e);
            return AjaxResult.success(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("关闭输入流出错", e);
            }
        }

        return AjaxResult.success("导入成功");
    }


    @ApiOperation(value = "读取xml文件")
    @RequestMapping(value = "/flowable/definition/readXml/{deployId}", method = GET)
    @ResponseBody
    public AjaxResult readXml(@ApiParam(value = "流程定义id") @PathVariable(value = "deployId") String deployId) {
        try {
            return flowDefinitionService.readXml(deployId);
        } catch (Exception e) {
            return AjaxResult.error("加载xml文件异常");
        }
    }

    @ApiOperation(value = "读取图片文件")
    @RequestMapping(value = "/flowable/definition/readImage/{deployId}", method = GET)
    @ResponseBody
    public void readImage(@ApiParam(value = "流程定义id") @PathVariable(value = "deployId") String deployId
            , HttpServletResponse response) {
        OutputStream os = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(flowDefinitionService.readImage(deployId));
            response.setContentType("image/png");
            os = response.getOutputStream();
            if (image != null) {
                ImageIO.write(image, "png", os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @ApiOperation(value = "保存流程设计器内的xml文件")
    @RequestMapping(value = "/flowable/definition/save", method = POST)
    @ResponseBody
    public Response<Integer, Void> save(@RequestParam(value = "name", required = true) String name
            , @RequestParam(value = "category", required = true) String category
            , @RequestParam(value = "xml", required = true) String xml) {
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            flowDefinitionService.importFile(name, category, in);
        } catch (Exception e) {
            log.error("保存失败:", e);
            return Response.of(HttpStatus.ERROR, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("关闭输入流出错", e);
            }
        }
        return Response.of(HttpStatus.SUCCESS, "保存成功");
    }


    @ApiOperation(value = "根据流程定义ID启动流程实例")
    @RequestMapping(value = "/flowable/definition/start/{procDefId}", method = POST)
    @ResponseBody
    public Response<Integer, Void> start(@ApiParam(value = "流程定义id") @PathVariable(value = "procDefId") String procDefId
            , @ApiParam(value = "变量集合,json对象") @RequestBody Map<String, Object> variables) {
        return flowDefinitionService.startProcessInstanceById(procDefId, variables);
    }

    @ApiOperation(value = "激活或挂起流程定义")
    @RequestMapping(value = "/flowable/definition/updateState", method = PUT)
    @ResponseBody
    public AjaxResult updateState(@ApiParam(value = "1:激活,2:挂起", required = true) @RequestParam Integer state
            , @ApiParam(value = "流程部署ID", required = true) @RequestParam String deployId) {
        flowDefinitionService.updateState(state, deployId);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('flowable:definition:remove')")
    @ApiOperation(value = "删除流程")
    @RequestMapping(value = "/flowable/definition/{deployIds}", method = DELETE)
    @ResponseBody
    public AjaxResult delete(@ApiParam(value = "流程部署ID", required = true) @PathVariable String[] deployIds) {
        flowDefinitionService.delete(deployIds);
        return AjaxResult.success();
    }

    @ApiOperation(value = "指定流程办理人员列表")
    @RequestMapping(value = "/flowable/definition/userList", method = GET)
    @ResponseBody
    public AjaxResult getUserList(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        return AjaxResult.success(list);
    }

    @ApiOperation(value = "指定流程办理组列表")
    @RequestMapping(value = "/flowable/definition/roleList", method = GET)
    @ResponseBody
    public AjaxResult getRoleList(SysRole role) {
        List<SysRole> list = sysRoleService.selectRoleList(role);
        return AjaxResult.success(list);
    }

    @ApiOperation(value = "根据deployId查询procDefId", notes = "......")
    @RequestMapping(value = "/flowable/definition/deploy-to-def-id", method = GET)
    @ResponseBody
    public Response<Integer, String> getProcDefIdByDeployId(
            @RequestParam(value = "deployId", required = true) String deployId) {
        String procDefId = flowDefinitionService.getProcDefIdByDeployId(deployId);
        return Response.of(HttpStatus.SUCCESS, "ok", procDefId);
    }
}
