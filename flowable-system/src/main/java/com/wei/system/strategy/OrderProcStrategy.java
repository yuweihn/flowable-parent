package com.wei.system.strategy;


import com.alibaba.fastjson2.JSON;
import com.wei.common.constant.Constants;
import com.wei.common.constant.ProcessConstants;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.system.domain.Order;
import com.wei.system.domain.vo.OrderVo;
import com.wei.system.service.ISysUserService;
import com.wei.system.service.OrderService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;

import java.util.Date;
import java.util.Map;


/**
 * @Author: yuwei
 */
@Slf4j
public class OrderProcStrategy implements ProcStrategy {
    @Override
    public String getProcTitle(String procInsId) {
        OrderService orderService = SpringContext.getBean(OrderService.class);
        return orderService.getFullOrderTitle(procInsId);
    }

    @Override
    public String getProcNo(String procInsId) {
        OrderService orderService = SpringContext.getBean(OrderService.class);
        Order order = orderService.getOrder(procInsId);
        return order == null ? null : order.getOrderNo();
    }

    @Override
    public Response<Boolean, Void> save(String procInsId, Map<String, Object> variables, long userId) {
        ISysUserService userService = SpringContext.getBean(ISysUserService.class);
        OrderService orderService = SpringContext.getBean(OrderService.class);
        RuntimeService runtimeService = SpringContext.getBean(RuntimeService.class);
        HistoryService historyService = SpringContext.getBean(HistoryService.class);

        try {
            OrderVo orderVo = JSON.parseObject(JSON.toJSONString(variables), OrderVo.class);
            variables.put(ProcessConstants.PROCESS_ORDER_INVOLVE_NON_STANDARD
                    , orderVo.getInvolveNonStandard() != null && orderVo.getInvolveNonStandard());

            orderVo.setOrderNo(generateSerialNo(orderVo.getCustomerNo()));
            SysUser sysUser = userService.selectUserById(userId);
            long orderId = orderService.createOrder(procInsId, orderVo, userId
                    , sysUser == null ? "User[userId=" + userId + "]" : sysUser.getUserName());
            return Response.of(true, "ok");
        } catch (Exception e) {
            log.error("", e);
            runtimeService.deleteProcessInstance(procInsId, "");
            historyService.deleteHistoricProcessInstance(procInsId);
            return Response.of(false, e.getMessage());
        }
    }

    @Override
    public void update(String procInsId, Map<String, Object> variables, long userId) {
        ISysUserService userService = SpringContext.getBean(ISysUserService.class);
        OrderService orderService = SpringContext.getBean(OrderService.class);

        OrderVo orderVo = JSON.parseObject(JSON.toJSONString(variables), OrderVo.class);
        SysUser sysUser = userService.selectUserById(userId);
        orderService.updateOrder(procInsId, orderVo, sysUser == null ? "User[userId=" + userId + "]" : sysUser.getUserName());
    }

    /**
     * @param customerNo
     * @return    SO-A002113-20220607094
     */
    @Override
    public String generateSerialNo(String customerNo) {
        String key = String.format(Constants.SYS_CONFIG_KEY_MAX_ORDER_INDEX, customerNo);
        long idx = getNextSerialIndex(customerNo, key, "订单");
        return "SO-" + customerNo + "-" + DateUtil.formatDate(new Date(), "yyyyMMdd")
                + String.format("%03d", idx);
    }
}
