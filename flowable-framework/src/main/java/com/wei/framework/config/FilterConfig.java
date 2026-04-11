package com.wei.framework.config;


import com.wei.common.filter.RepeatableFilter;
import com.wei.common.filter.XssFilter;
import com.wei.common.utils.StringUtils;
import com.yuweix.kuafu.web.filter.CacheContentHttpFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Filter配置
 *
 * @author ruoyi
 */
@Configuration
public class FilterConfig {
    @Value("${xss.enabled}")
    private String enabled;

    @Value("${xss.excludes}")
    private String excludes;

    @Value("${xss.urlPatterns}")
    private String urlPatterns;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean xssFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns(StringUtils.split(urlPatterns, ","));
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", excludes);
        initParameters.put("enabled", enabled);
        registration.setInitParameters(initParameters);
        return registration;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatableFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE);
        return registration;
    }

    @Bean("webFilterReg")
    public FilterRegistrationBean<Filter> httpFilterRegistrationBean() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        CacheContentHttpFilter webFilter = new CacheContentHttpFilter();
        bean.setFilter(webFilter);
        bean.setUrlPatterns(Arrays.asList("/*"));
        bean.addInitParameter("exclusive", "/, /static/**, /js/**, /**/*.*");
        bean.addInitParameter("requestAtt", "userName");
        return bean;
    }
}
