package com.wei.web.controller.system;


import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.model.LoginUser;
import com.wei.common.utils.ServletUtils;
import com.wei.framework.web.service.TokenService;
import com.wei.system.domain.ContractOrder;
import com.wei.system.domain.Order;
import com.wei.system.domain.vo.*;
import com.wei.system.mapper.ContractOrderMapper;
import com.wei.system.mapper.OrderMapper;
import com.wei.system.service.ContractService;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author yuwei
 */
@Api(tags = {"合同信息"})
@RestController
public class SysContractController {
    @Autowired
    private TokenService tokenService;
    @Resource
    private ContractService contractService;

    @PreAuthorize("@ss.hasPermi('system:contract:list')")
    @ApiOperation(value = "合同列表", notes = "......")
    @RequestMapping(value = "/system/contract/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizContractVo>> queryContractList(
            @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "userId", required = false) Long userId
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "fuzzyContractNo", required = false) String fuzzyContractNo
            , @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime
            , @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = contractService.queryContractCount(customerNo, userId, title, fuzzyContractNo
                , startTime, endTime);
        List<BizContractVo> list = contractService.queryContractList(customerNo, userId, title, fuzzyContractNo
                , startTime, endTime, pageNo, pageSize);
        PageResponseVo<BizContractVo> pageVo = PageResponseVo.<BizContractVo>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:contract:delete')")
    @ApiOperation(value = "删除合同", notes = "......")
    @RequestMapping(value = "/system/contract/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteContract(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long ContractId: ids) {
            contractService.deleteContract(ContractId);
        }
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }

    @ApiOperation(value = "合同下拉列表", notes = "......")
    @RequestMapping(value = "/system/contract/drop-down-list", method = GET)
    @ResponseBody
    public Response<Integer, List<ContractDropDownVo>> getContractDropDownList(
            @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        Long userId = loginUser.getUser().getUserId();
        List<ContractDropDownVo> voList = contractService.getContractDropDownList(customerNo, null, title, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "合同附件", notes = "......")
    @RequestMapping(value = "/system/contract/attach/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizContractVo.Attach>> getContractAttachList(
            @RequestParam(value = "contractId", required = true) long contractId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = contractService.queryContractAttachCount(contractId);
        List<BizContractVo.Attach> list = contractService.queryContractAttachList(contractId,pageNo, pageSize);
        PageResponseVo<BizContractVo.Attach> pageVo = PageResponseVo.<BizContractVo.Attach>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @ApiOperation(value = "合同订单", notes = "......")
    @RequestMapping(value = "/system/contract/order/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizContractVo.Order>> getContractOrderList(
            @RequestParam(value = "contractId", required = true) long contractId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = contractService.queryContractOrderCount(contractId);
        List<BizContractVo.Order> list = contractService.queryContractOrderList(contractId,pageNo, pageSize);
        PageResponseVo<BizContractVo.Order> pageVo = PageResponseVo.<BizContractVo.Order>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }


    @RequestMapping(value = "/system/contract/info", method = GET)
    @ResponseBody
    public Response<Integer, BizContractVo> queryContractInfoById(
            @RequestParam(value = "contractId", required = true) long contractId) {
        BizContractVo bizContractVo = contractService.getContractInfo(contractId);
        return new Response<>(HttpStatus.SUCCESS, "ok",bizContractVo);
    }

}
