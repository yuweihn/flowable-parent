package com.wei.web.controller.system;


import com.wei.common.constant.HttpStatus;
import com.wei.framework.web.service.TokenService;
import com.wei.system.domain.vo.*;
import com.wei.system.service.OrderService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author yuwei
 */
@Api(tags = {"订单信息"})
@RestController
public class SysOrderController {
    @Autowired
    private TokenService tokenService;
    @Resource
    private OrderService orderService;


    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @ApiOperation(value = "订单列表", notes = "......")
    @RequestMapping(value = "/system/order/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo>> queryOrderList(
            @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "userId", required = false) Long userId
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "fuzzyOrderNo", required = false) String fuzzyOrderNo
            , @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime
            , @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderCount(customerNo, userId, title, fuzzyOrderNo, startTime, endTime);
        List<BizOrderVo> list = orderService.queryOrderList(customerNo, userId, title, fuzzyOrderNo, startTime, endTime, pageNo, pageSize);
        PageResponseVo<BizOrderVo> pageVo = PageResponseVo.<BizOrderVo>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:port:list')")
    @ApiOperation(value = "订单标准产品(端口)", notes = "......")
    @RequestMapping(value = "/system/order/port/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Port>> queryOrderPortList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderPortCount(orderId);
        List<BizOrderVo.Port> list = orderService.queryOrderPortList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Port> pageVo = PageResponseVo.<BizOrderVo.Port>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:device:list')")
    @ApiOperation(value = "订单标准产品(设备)", notes = "......")
    @RequestMapping(value = "/system/order/device/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Device>> queryOrderDeviceList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderDeviceCount(orderId);
        List<BizOrderVo.Device> list = orderService.queryOrderDeviceList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Device> pageVo = PageResponseVo.<BizOrderVo.Device>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:MplsIpsec:list')")
    @ApiOperation(value = "订单非标准产品(MPLS/IPSec)", notes = "......")
    @RequestMapping(value = "/system/order/mpls-ipsec/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.MplsIpsec>> queryOrderMplsIpsecList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderMplsIpsecCount(orderId);
        List<BizOrderVo.MplsIpsec> list = orderService.queryOrderMplsIpsecList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.MplsIpsec> pageVo = PageResponseVo.<BizOrderVo.MplsIpsec>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:specialLine:list')")
    @ApiOperation(value = "订单非标准产品(专线)", notes = "......")
    @RequestMapping(value = "/system/order/special-line/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.SpecialLine>> queryOrderSpecialLineList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderSpecialLineCount(orderId);
        List<BizOrderVo.SpecialLine> list = orderService.queryOrderSpecialLineList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.SpecialLine> pageVo = PageResponseVo.<BizOrderVo.SpecialLine>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:internet:list')")
    @ApiOperation(value = "订单非标准产品(Internet)", notes = "......")
    @RequestMapping(value = "/system/order/internet/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Internet>> queryOrderInternetList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderInternetCount(orderId);
        List<BizOrderVo.Internet> list = orderService.queryOrderInternetList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Internet> pageVo = PageResponseVo.<BizOrderVo.Internet>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:idc:list')")
    @ApiOperation(value = "订单非标准产品(IDC)", notes = "......")
    @RequestMapping(value = "/system/order/idc/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Idc>> queryOrderIdcList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderIdcCount(orderId);
        List<BizOrderVo.Idc> list = orderService.queryOrderIdcList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Idc> pageVo = PageResponseVo.<BizOrderVo.Idc>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:srv:list')")
    @ApiOperation(value = "订单非标准产品(服务)", notes = "......")
    @RequestMapping(value = "/system/order/srv/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Srv>> queryOrderSrvList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderSrvCount(orderId);
        List<BizOrderVo.Srv> list = orderService.queryOrderSrvList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Srv> pageVo = PageResponseVo.<BizOrderVo.Srv>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:integration:list')")
    @ApiOperation(value = "订单非标准产品(集成)", notes = "......")
    @RequestMapping(value = "/system/order/integration/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizOrderVo.Integration>> queryOrderIntegrationList(
            @RequestParam(value = "orderId", required = true) long orderId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = orderService.queryOrderIntegrationCount(orderId);
        List<BizOrderVo.Integration> list = orderService.queryOrderIntegrationList(orderId, pageNo, pageSize);
        PageResponseVo<BizOrderVo.Integration> pageVo = PageResponseVo.<BizOrderVo.Integration>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:order:delete')")
    @ApiOperation(value = "删除订单", notes = "......")
    @RequestMapping(value = "/system/order/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteOrder(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long orderId: ids) {
            orderService.deleteOrder(orderId);
        }
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }


    @ApiOperation(value = "订单下拉列表", notes = "......")
    @RequestMapping(value = "/system/order/drop-down-list", method = GET)
    @ResponseBody
    public Response<Integer, List<OrderDropDownVo>> getOrderDropDownList(
            @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "keywords", required = false) String keywords
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        Long userId = loginUser.getUser().getUserId();
        List<OrderDropDownVo> voList = orderService.getOrderDropDownList(customerNo, null, keywords, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "指定客户未绑定合同的订单下拉列表", notes = "......")
    @RequestMapping(value = "/system/order/customer/unbound-contract/order/drop-down-list", method = GET)
    @ResponseBody
    public Response<Integer, List<OrderDropDownVo>> getUnboundContractOrderDropDownList(
            @RequestParam(value = "customerNo", required = true) String customerNo
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        List<OrderDropDownVo> voList = orderService.getUnboundContractOrderListByCustomerNo(customerNo, title, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "根据合同ID查询订单下拉列表", notes = "......")
    @RequestMapping(value = "/system/order/contract/order/drop-down-list", method = GET)
    @ResponseBody
    public Response<Integer, List<OrderDropDownVo>> getOrderDropDownListByContractId(
            @RequestParam(value = "contractId", required = true) long contractId
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        List<OrderDropDownVo> voList = orderService.getOrderDropDownListByContractId(contractId, title, pageNo, pageSize);
        return new Response<>(HttpStatus.SUCCESS, "ok", voList);
    }

    @ApiOperation(value = "根据订单ID查询订单详情", notes = "......")
    @RequestMapping(value = "/system/order/info", method = GET)
    @ResponseBody
    public Response<Integer, BizOrderVo> getOrderInfo(
            @RequestParam(value = "orderId", required = true) long orderId) {
        BizOrderVo orderVo= orderService.getOrderInfo(orderId);
        return new Response<>(HttpStatus.SUCCESS, "ok", orderVo);
    }
}
