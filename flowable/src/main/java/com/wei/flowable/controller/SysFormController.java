package com.wei.flowable.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wei.common.constant.HttpStatus;
import com.wei.common.utils.SecurityUtils;
import com.wei.flowable.domain.dto.SysFormDto;
import com.wei.flowable.service.ISysDeployFormService;
import com.wei.system.domain.SysDeployForm;
import com.wei.system.domain.vo.OptionVo;
import com.wei.system.domain.vo.PageResponseVo;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wei.common.annotation.Log;
import com.wei.common.core.controller.BaseController;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.enums.BusinessType;
import com.wei.flowable.service.ISysFormService;
import com.wei.common.utils.poi.ExcelUtil;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


/**
 * 流程表单Controller
 *
 * @author XuanXuan
 * @date 2021-04-03
 */
@RestController
public class SysFormController extends BaseController {
    @Autowired
    private ISysFormService sysFormService;
    @Autowired
    private ISysDeployFormService sysDeployFormService;


    @PreAuthorize("@ss.hasPermi('flowable:form:list')")
    @ApiOperation(value = "查询流程表单列表")
    @RequestMapping(value = "/flowable/form/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<SysFormDto>> findFormList(
            @RequestParam(value = "formName", required = false) String formName
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = sysFormService.findFormCount(formName);
        List<SysFormDto> list = sysFormService.findFormList(formName, pageNo, pageSize);

        PageResponseVo<SysFormDto> pageVo = PageResponseVo.<SysFormDto>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @ApiOperation(value = "下拉框默认下拉列表")
    @RequestMapping(value = "/flowable/form/option/default-list", method = GET)
    @ResponseBody
    public Response<Integer, List<OptionVo>> getDefaultOptionList() {
        List<OptionVo> optionList = new ArrayList<>();
        optionList.add(OptionVo.builder().value("1").label("选项一").build());
        optionList.add(OptionVo.builder().value("2").label("选项二").build());
        return Response.of(HttpStatus.SUCCESS, "ok", optionList);
    }

    /**
     * 导出流程表单列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:export')")
    @Log(title = "流程表单", businessType = BusinessType.EXPORT)
    @RequestMapping(value = "/flowable/form/export", method = GET)
    @ResponseBody
    public AjaxResult export(@RequestParam(value = "formName", required = false) String formName
            , @RequestParam(value = "pageNo", required = false) Integer pageNo
            , @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<SysFormDto> list = sysFormService.findFormList(formName, pageNo, pageSize);
        ExcelUtil<SysFormDto> util = new ExcelUtil<>(SysFormDto.class);
        return util.exportExcel(list, "form");
    }

    /**
     * 获取流程表单详细信息
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping(value = "/flowable/form/{formId}")
    public Response<Integer, SysFormDto> getInfo(@PathVariable("formId") long formId) {
        SysFormDto dto = sysFormService.selectSysFormById(formId);
        if (dto == null) {
            return Response.of(HttpStatus.ERROR, "数据不存在");
        } else {
            return Response.of(HttpStatus.SUCCESS, "ok", dto);
        }
    }

    /**
     * 新增流程表单
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @RequestMapping(value = "/flowable/form", method = POST)
    @ResponseBody
    public Response<Integer, Long> add(@RequestBody Map<String, Object> form) {
        String formName = (String) form.get("formName");
        byte formType = Byte.parseByte(form.get("formType").toString());
        String formContent = (String) form.get("formContent");
        String remark = (String) form.get("remark");
        String userName = SecurityUtils.getUsername();
        long id = sysFormService.insertSysForm(formName, formType, formContent, remark, userName);
        return Response.of(HttpStatus.SUCCESS, "ok", id);
    }

    /**
     * 修改流程表单
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @Log(title = "流程表单", businessType = BusinessType.UPDATE)
    @RequestMapping(value = "/flowable/form", method = PUT)
    @ResponseBody
    public Response<Integer, Void> edit(@RequestBody Map<String, Object> form) {
        Long id = Long.parseLong(form.get("formId").toString());
        String formName = (String) form.get("formName");
        byte formType = Byte.parseByte(form.get("formType").toString());
        String formContent = (String) form.get("formContent");
        String remark = (String) form.get("remark");
        String userName = SecurityUtils.getUsername();
        sysFormService.updateSysForm(id, formName, formType, formContent, remark, userName);
        return Response.of(HttpStatus.SUCCESS, "ok");
    }

    /**
     * 删除流程表单
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @Log(title = "流程表单", businessType = BusinessType.DELETE)
    @RequestMapping(value = "/flowable/form/{formIds}", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> remove(@PathVariable Long[] formIds) {
        for (long formId: formIds) {
            sysFormService.deleteSysFormById(formId);
        }
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }

    /**
     * 挂载流程表单
     */
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @RequestMapping(value = "/flowable/form/addDeployForm", method = POST)
    @ResponseBody
    public Response<Integer, Void> addDeployForm(@RequestBody SysDeployForm sysDeployForm) {
        int cnt = sysDeployFormService.insertSysDeployForm(sysDeployForm);
        return cnt > 0
                ? Response.of(HttpStatus.SUCCESS, "ok")
                : Response.of(HttpStatus.ERROR, "Error") ;
    }
}
