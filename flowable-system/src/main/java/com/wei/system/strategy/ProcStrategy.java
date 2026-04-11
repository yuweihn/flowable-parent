package com.wei.system.strategy;


import com.wei.system.domain.SysConfig;
import com.wei.system.service.ISysConfigService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.SpringContext;

import java.util.Date;
import java.util.Map;


/**
 * @Author: yuwei
 */
public interface ProcStrategy {
    /**
     * 获得流程实例标题
     * @param procInsId
     * @return
     */
    String getProcTitle(String procInsId);

    String getProcNo(String procInsId);

    /**
     * 新建流程实例时，保存表单数据
     */
    Response<Boolean, Void> save(String procInsId, Map<String, Object> variables, long userId);

    /**
     * 审批流程时，更新表单数据
     */
    void update(String procInsId, Map<String, Object> variables, long userId);

    /**
     * 生成流程业务编号
     */
    String generateSerialNo(String customerNo);

    default long getNextSerialIndex(String customerNo, String key, String typeName) {
        ISysConfigService configService = SpringContext.getBean(ISysConfigService.class);
        try {
            SysConfig sysConfig = configService.findByKey(key);
            String oldVal = sysConfig == null ? null : sysConfig.getConfigValue();
            Date latestTime = sysConfig == null
                    ? null
                    : (sysConfig.getUpdateTime() == null ? sysConfig.getCreateTime() : sysConfig.getUpdateTime());
            if (oldVal == null) {
                long initNo = 1;
                boolean b = configService.insertConfig(key, "" + initNo, "[" + customerNo + "]当前最大" + typeName + "编号", "system");
                if (!b) {
                    throw new Exception("获取[" + customerNo + "]当前最大" + typeName + "编号失败");
                } else {
                    return initNo;
                }
            } else if (latestTime == null || !DateUtil.isSameDay(new Date(), latestTime)) {
                long initNo = 1;
                boolean updateSuccess = configService.updateConfigByKV(key, "" + initNo, oldVal, "system");
                if (!updateSuccess) {
                    throw new RuntimeException("生成" + typeName + "编号失败，请重试！");
                }
                return initNo;
            } else {
                long maxNo = Long.parseLong(oldVal);
                maxNo = maxNo + 1;
                boolean updateSuccess = configService.updateConfigByKV(key, "" + maxNo, oldVal, "system");
                if (!updateSuccess) {
                    throw new RuntimeException("生成" + typeName + "编号失败，请重试！");
                }
                return maxNo;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
