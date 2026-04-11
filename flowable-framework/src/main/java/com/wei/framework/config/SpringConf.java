package com.wei.framework.config;


import com.wei.common.utils.WechatUtil;
import com.yuweix.kuafu.core.cloud.CosUtil;
import com.yuweix.kuafu.data.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;


/**
 * @author yuwei
 */
@Configuration
public class SpringConf {
    @Bean
    public CosUtil cosUtil(@Value("${cos.secretId}")String secretId, @Value("${cos.secretKey}")String secretKey
            , @Value("${cos.region}")String region, @Value("${cos.protocol:}")String protocol
            , @Value("${cos.bucketName}")String bucketName) {
        return new CosUtil(secretId, secretKey, region, protocol, bucketName);
    }

    @Bean(name = "wechatUtil")
    public WechatUtil wechatUtil(Cache cache) {
        try {
            Class<?> clz = Class.forName(WechatUtil.class.getName());
            Constructor<?> constructor = clz.getDeclaredConstructor();
            constructor.setAccessible(true);
            WechatUtil wechatUtil = (WechatUtil) constructor.newInstance();
            wechatUtil.setCache(cache);
            return wechatUtil;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
