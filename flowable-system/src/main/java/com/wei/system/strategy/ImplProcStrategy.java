package com.wei.system.strategy;


import com.alibaba.fastjson2.JSON;
import com.wei.common.constant.Constants;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.system.domain.Impl;
import com.wei.system.domain.vo.ImplVo;
import com.wei.system.service.ISysUserService;
import com.wei.system.service.ImplService;
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
public class ImplProcStrategy implements ProcStrategy {
    @Override
    public String getProcTitle(String procInsId) {
        ImplService implService = SpringContext.getBean(ImplService.class);
        return implService.getFullImplTitle(procInsId);
    }

    @Override
    public String getProcNo(String procInsId) {
        ImplService implService = SpringContext.getBean(ImplService.class);
        Impl impl = implService.getImpl(procInsId);
        return impl == null ? null : impl.getImplNo();
    }

    @Override
    public Response<Boolean, Void> save(String procInsId, Map<String, Object> variables, long userId) {
        ISysUserService userService = SpringContext.getBean(ISysUserService.class);
        ImplService implService = SpringContext.getBean(ImplService.class);
        RuntimeService runtimeService = SpringContext.getBean(RuntimeService.class);
        HistoryService historyService = SpringContext.getBean(HistoryService.class);

        try {
            ImplVo implVo = JSON.parseObject(JSON.toJSONString(variables), ImplVo.class);
            implVo.setImplNo(generateSerialNo(implVo.getCustomerNo()));
            SysUser sysUser = userService.selectUserById(userId);
            long implId = implService.createImpl(procInsId, implVo, userId
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
        ImplService implService = SpringContext.getBean(ImplService.class);

        ImplVo implVo = JSON.parseObject(JSON.toJSONString(variables), ImplVo.class);
        SysUser sysUser = userService.selectUserById(userId);
        implService.updateImpl(procInsId, implVo, sysUser == null ? "User[userId=" + userId + "]" : sysUser.getUserName());
    }

    /**
     * @param customerNo
     * @return    A002113-20220607094
     */
    @Override
    public String generateSerialNo(String customerNo) {
        String key = String.format(Constants.SYS_CONFIG_KEY_MAX_IMPL_INDEX, customerNo);
        long idx = getNextSerialIndex(customerNo, key, "实施启动");
        return customerNo + "-" + DateUtil.formatDate(new Date(), "yyyyMMdd")
                + String.format("%03d", idx);
    }
}
