package com.wei.system.strategy;


import com.alibaba.fastjson2.JSON;
import com.wei.common.constant.Constants;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.system.domain.Contract;
import com.wei.system.domain.vo.ContractVo;
import com.wei.system.service.ContractService;
import com.wei.system.service.ISysUserService;
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
public class ContractProcStrategy implements ProcStrategy {
    @Override
    public String getProcTitle(String procInsId) {
        ContractService contractService = SpringContext.getBean(ContractService.class);
        return contractService.getFullContractTitle(procInsId);
    }

    @Override
    public String getProcNo(String procInsId) {
        ContractService contractService = SpringContext.getBean(ContractService.class);
        Contract contract = contractService.getContract(procInsId);
        return contract == null ? null : contract.getContractNo();
    }

    @Override
    public Response<Boolean, Void> save(String procInsId, Map<String, Object> variables, long userId) {
        ISysUserService userService = SpringContext.getBean(ISysUserService.class);
        ContractService contractService = SpringContext.getBean(ContractService.class);
        RuntimeService runtimeService = SpringContext.getBean(RuntimeService.class);
        HistoryService historyService = SpringContext.getBean(HistoryService.class);

        try {
            ContractVo contractVo = JSON.parseObject(JSON.toJSONString(variables), ContractVo.class);
            contractVo.setContractNo(generateSerialNo(contractVo.getCustomerNo()));
            SysUser sysUser = userService.selectUserById(userId);
            long contractId = contractService.createContract(procInsId, contractVo, userId
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
        ContractService contractService = SpringContext.getBean(ContractService.class);

        ContractVo contractVo = JSON.parseObject(JSON.toJSONString(variables), ContractVo.class);
        SysUser sysUser = userService.selectUserById(userId);
        contractService.updateContract(procInsId, contractVo, sysUser == null ? "User[userId=" + userId + "]" : sysUser.getUserName());
    }

    /**
     * @param customerNo
     * @return    A002113-20220607094
     */
    @Override
    public String generateSerialNo(String customerNo) {
        String key = String.format(Constants.SYS_CONFIG_KEY_MAX_CONTRACT_INDEX, customerNo);
        long idx = getNextSerialIndex(customerNo, key, "合同");
        return customerNo + "-" + DateUtil.formatDate(new Date(), "yyyyMMdd")
                + String.format("%03d", idx);
    }
}
