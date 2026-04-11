package com.wei.web.controller.system;
import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.enums.ContractType;
import com.wei.framework.web.service.TokenService;
import com.wei.system.domain.*;
import com.wei.system.domain.vo.*;
import com.wei.system.mapper.*;
import com.wei.system.service.*;
import com.wei.system.service.impl.ContractServiceImpl;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author yuwei
 */
@Api(tags = {"实施启动单信息"})
@RestController
public class SysImplController {
    @Autowired
    private TokenService tokenService;
    @Resource
    private ImplService implService;
    @Resource
    private SysCustomerMapper customerMapper;
    @Resource
    private ISysUserService userService;

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private ContractOrderMapper contractOrderMapper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ISysCustomerService customerService;

    @PreAuthorize("@ss.hasPermi('system:impl:list')")
    @ApiOperation(value = "实施启动列表", notes = "......")
    @RequestMapping(value = "/system/impl/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizImplVo>> queryImplList(
            @RequestParam(value = "customerNo", required = false) String customerNo
            , @RequestParam(value = "preSalesId", required = false) Long preSalesId
            , @RequestParam(value = "title", required = false) String title
            , @RequestParam(value = "fuzzyImplNo", required = false) String fuzzyImplNo
            , @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime
            , @RequestParam(value = "endTime", required = false)@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = implService.queryImplCount(customerNo, preSalesId, title, fuzzyImplNo, startTime, endTime);
        List<BizImplVo> list = implService.queryImplList(customerNo, preSalesId, title, fuzzyImplNo, startTime, endTime, pageNo, pageSize);
        PageResponseVo<BizImplVo> pageVo = PageResponseVo.<BizImplVo>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @ApiOperation(value = "实施启动订单", notes = "......")
    @RequestMapping(value = "/system/impl/order/list", method = GET)
    @ResponseBody
    public Response<Integer, PageResponseVo<BizImplVo.Order>> queryImplOrderList(
            @RequestParam(value = "implId", required = true) long implId
            , @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
            , @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        int size = implService.queryImplOrderCount(implId);
        List<BizImplVo.Order> list = implService.queryImplOrderList(implId, pageNo, pageSize);
        PageResponseVo<BizImplVo.Order> pageVo = PageResponseVo.<BizImplVo.Order>builder()
                .size(size)
                .list(list)
                .build();
        return new Response<>(HttpStatus.SUCCESS, "ok", pageVo);
    }

    @PreAuthorize("@ss.hasPermi('system:impl:delete')")
    @ApiOperation(value = "删除实施启动", notes = "......")
    @RequestMapping(value = "/system/impl/delete", method = DELETE)
    @ResponseBody
    public Response<Integer, Void> deleteImpl(@RequestParam(value = "ids", required = true) long[] ids) {
        for (long implId: ids) {
            implService.deleteImpl(implId);
        }
        return new Response<>(HttpStatus.SUCCESS, "ok");
    }

    private String getFullContractTitle(String title, String contractType, String enterpriseName) {
        return "合同"
                + "+" + ContractType.getNameByCode(contractType)
                + "+" + enterpriseName
                + "+" + title;
    }
    @ApiOperation(value = "根据订单Id查售前，合同，客户详情", notes = "......")
    @RequestMapping(value = "/system/impl/order/related/info", method = GET)
    @ResponseBody
    public Response<Integer, PreSaleCustomerContractVo> queryPreSaleContractCustomerByOrder(@RequestParam(value = "orderId", required = true) long orderId) {
        //根据订单id查售前详情
        Order order = orderMapper.selectOneById(orderId, Order.class);
        if (order == null) {
            return new Response<>(HttpStatus.ERROR, "订单[OrderId=" + orderId + "]不存在");
        }

        //根据订单id查客户信息
        CustomerVo customerVo = customerService.findCustomerByNo(order.getCustomerNo());
        String enterpriseName = customerVo == null ? order.getCustomerNo() : customerVo.getEnterpriseName();

        //根据订单id查合同详情
        ContractOrder contractOrder = contractOrderMapper.findContractOrderByOrderId(orderId);
        String contractTitle = null;
        if (contractOrder != null) {
            BizContractVo contract = contractService.findContractById(contractOrder.getContractId());
            contractTitle = contract == null ? null : getFullContractTitle(contract.getTitle(), contract.getContractType(), enterpriseName);
        }

        SysUser preSales = userService.selectUserById(order.getPreSalesId());
        String preSalesName = preSales == null ? null : preSales.getUserName();//售前账号

        PreSaleCustomerContractVo vo = new PreSaleCustomerContractVo();
        vo.setCustomerNo(order.getCustomerNo());
        vo.setEnterpriseName(enterpriseName);
        vo.setContractTitle(contractTitle);
        vo.setPreSalesId(order.getPreSalesId());
        vo.setPreSalesUserName(preSalesName);
        return new Response<>(HttpStatus.SUCCESS, "ok", vo);
    }
    @RequestMapping(value = "/system/impl/info", method = GET)
    @ResponseBody
    public Response<Integer, BizImplVo> queryContractInfoById(
            @RequestParam(value = "implId", required = true) long implId) {
        BizImplVo bizImplVo = implService.getImplInfo(implId);
        return new Response<>(HttpStatus.SUCCESS, "ok", bizImplVo);
    }
}
