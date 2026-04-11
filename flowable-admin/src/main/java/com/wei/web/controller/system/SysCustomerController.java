package com.wei.web.controller.system;


import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.utils.SecurityUtils;
import com.wei.system.domain.vo.CustomerDropDownVo;
import com.wei.system.domain.vo.CustomerVo;
import com.wei.system.domain.vo.PageResponseVo;
import com.wei.system.mapper.OrderMapper;
import com.wei.system.service.ISysCustomerService;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author yuwei
 */
@Api(tags = {"客户信息"})
@RestController
public class SysCustomerController {
    @Autowired
    private ISysCustomerService customerService;
    @Resource
    private OrderMapper orderMapper;


    @PreAuthorize("@ss.hasPermi('system:customer:list')")
    @ApiOperation(value = "管理员客户列表", notes = "......")
    @RequestMapping(value = {"/system/customer/list", "/api/system/customer/list"}, method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<CustomerVo>> queryCustomerList(
            @RequestParam(value = "id", required = false) Long id
            , @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "enterpriseName", required = false) String enterpriseName
            , @RequestParam(value = "salesUserId", required = false) Long salesUserId
            , @RequestParam(value = "keywords", required = false) String keywords
            , @RequestParam(value = "statusCode", required = false) Integer statusCode
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = customerService.queryCustomerCount(id, customerNo, enterpriseName, salesUserId
                    , keywords, statusCode);
        List<CustomerVo> list = customerService.queryCustomerList(id, customerNo, enterpriseName, salesUserId
                    , keywords, statusCode, pageNo, pageSize);
        PageResponseVo<CustomerVo> pageVo = PageResponseVo.<CustomerVo>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:saleCustomer:list')")
    @ApiOperation(value = "销售客户列表", notes = "......")
    @RequestMapping(value = "/system/sale/customer/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<CustomerVo>> querySaleCustomerList(
            @RequestParam(value = "id", required = false) Long id
            , @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "enterpriseName", required = false) String enterpriseName
            , @RequestParam(value = "keywords", required = false) String keywords
            , @RequestParam(value = "statusCode", required = false) Integer statusCode
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        int size = customerService.queryCustomerCount(id, customerNo, enterpriseName, sysUser.getUserId()
                , keywords, statusCode);
        List<CustomerVo> list = customerService.queryCustomerList(id, customerNo, enterpriseName, sysUser.getUserId()
                , keywords, statusCode, pageNo, pageSize);
        PageResponseVo<CustomerVo> pageVo = PageResponseVo.<CustomerVo>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @ApiOperation(value = "客户详情", notes = "......")
    @RequestMapping(value = "/system/customer/info", method = GET)
    @ResponseBody
    public Response<Integer, CustomerVo> queryCustomerInfo(@RequestParam(value = "id", required = true) long customerId) {
        CustomerVo vo = customerService.findCustomerById(customerId);
        if (vo == null) {
            return new Response<>(HttpStatus.ERROR, "数据不存在");
        } else {
            return new Response<>(HttpStatus.SUCCESS, "ok", vo);
        }
    }

    @ApiOperation(value = "客户下拉列表", notes = "......")
    @RequestMapping(value = {"/system/customer/option-list", "/api/system/customer/option-list"}, method = GET)
    @ResponseBody
    public Response<Integer, List<CustomerDropDownVo>> getCustomerOptionList(
            @RequestParam(value = "keywords", required = false) String keywords
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        List<CustomerDropDownVo> voList = customerService.getCustomerOptionList(null, keywords, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "指定销售的客户下拉列表", notes = "......")
    @RequestMapping(value = "/system/customer/sale-customers", method = GET)
    @ResponseBody
    public Response<Integer, List<CustomerDropDownVo>> getCustomerOptionListBySales(
            @RequestParam(value = "keywords", required = false) String keywords
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        Long salesId = loginUser.getUserId();
        List<CustomerDropDownVo> voList = customerService.getCustomerOptionList(salesId, keywords, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "根据客户编号查详情", notes = "......")
    @RequestMapping(value = {"/system/customer/no/info", "/api/system/customer/no/info"}, method = GET)
    @ResponseBody
    public Response<Integer, CustomerVo> queryCustomerInfoByNo(@RequestParam(value = "customerNo", required = true) String customerNo) {
        CustomerVo vo = customerService.findCustomerByNo(customerNo);
        if (vo == null) {
            return new Response<>(HttpStatus.ERROR, "数据不存在");
        } else {
            return new Response<>(HttpStatus.SUCCESS, "ok", vo);
        }
    }

    @ApiOperation(value = "创建客户", notes = "......")
    @RequestMapping(value = "/system/customer/create", method = POST)
    @ResponseBody
    public Response<Integer, Long> createCustomer(@RequestParam(value = "enterpriseName", required = true) String enterpriseName
            , @RequestParam(value = "contacts", required = false) String contacts
            , @RequestParam(value = "phoneNo", required = false) String phoneNo
            , @RequestParam(value = "email", required = false) String email
            , @RequestParam(value = "contacts2", required = false) String contacts2
            , @RequestParam(value = "phoneNo2", required = false) String phoneNo2
            , @RequestParam(value = "email2", required = false) String email2
            , @RequestParam(value = "tenantId", required = false) String tenantId
            , @RequestParam(value = "statusCode", required = false) Integer statusCode
            , @RequestParam(value = "typeCode", required = false) Integer typeCode
            , @RequestParam(value = "industryCode", required = false) Integer industryCode
            , @RequestParam(value = "addr", required = false) String addr
            , @RequestParam(value = "fpTitle", required = false) String fpTitle
            , @RequestParam(value = "fpBankName", required = false) String fpBankName
            , @RequestParam(value = "fpBankAccNo", required = false) String fpBankAccNo
            , @RequestParam(value = "fpPhoneNo", required = false) String fpPhoneNo
            , @RequestParam(value = "fpTaxNo", required = false) String fpTaxNo
            , @RequestParam(value = "fpAddr", required = false) String fpAddr
           // , @RequestParam(value = "salesUserId", required = false) Long salesUserId
            , @RequestParam(value = "remark", required = false) String remark) {
        String userName = SecurityUtils.getUsername();
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        long id = customerService.createCustomer(enterpriseName, contacts, phoneNo, email
                , contacts2, phoneNo2, email2, tenantId, statusCode, typeCode, industryCode, addr
                , fpTitle, fpBankName, fpBankAccNo, fpPhoneNo, fpTaxNo, fpAddr
                , sysUser.getUserId(), remark, userName);
        return new Response<>(HttpStatus.SUCCESS, "ok", id);
    }

    @ApiOperation(value = "修改客户", notes = "......")
    @RequestMapping(value = "/system/customer/update", method = {POST, PUT})
    @ResponseBody
    public Response<Integer, Void> updateCustomer(@RequestParam(value = "id", required = true) long id
            , @RequestParam(value = "enterpriseName", required = true) String enterpriseName
            , @RequestParam(value = "contacts", required = false) String contacts
            , @RequestParam(value = "phoneNo", required = false) String phoneNo
            , @RequestParam(value = "email", required = false) String email
            , @RequestParam(value = "contacts2", required = false) String contacts2
            , @RequestParam(value = "phoneNo2", required = false) String phoneNo2
            , @RequestParam(value = "email2", required = false) String email2
            , @RequestParam(value = "tenantId", required = false) String tenantId
            , @RequestParam(value = "statusCode", required = false) Integer statusCode
            , @RequestParam(value = "typeCode", required = false) Integer typeCode
            , @RequestParam(value = "industryCode", required = false) Integer industryCode
            , @RequestParam(value = "addr", required = false) String addr
            , @RequestParam(value = "fpTitle", required = false) String fpTitle
            , @RequestParam(value = "fpBankName", required = false) String fpBankName
            , @RequestParam(value = "fpBankAccNo", required = false) String fpBankAccNo
            , @RequestParam(value = "fpPhoneNo", required = false) String fpPhoneNo
            , @RequestParam(value = "fpTaxNo", required = false) String fpTaxNo
            , @RequestParam(value = "fpAddr", required = false) String fpAddr
            , @RequestParam(value = "remark", required = false) String remark) {
        String userName = SecurityUtils.getUsername();
        customerService.updateCustomer(id, enterpriseName, contacts, phoneNo, email
                , contacts2, phoneNo2, email2, tenantId, statusCode, typeCode, industryCode, addr
                , fpTitle, fpBankName, fpBankAccNo, fpPhoneNo, fpTaxNo, fpAddr
                , remark, userName);
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }

    @ApiOperation(value = "删除客户", notes = "......")
    @RequestMapping(value = "/system/customer/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteCustomer(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long customerId: ids) {
            customerService.deleteCustomer(customerId);
        }
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }
}
