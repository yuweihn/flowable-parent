package com.wei.framework.config;


import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.wei.common.config.AppConf;
import com.wei.common.constant.Constants;
import com.wei.framework.interceptor.LoginCheckInterceptor;
import com.wei.framework.interceptor.RepeatSubmitInterceptor;
import com.yuweix.kuafu.core.serialize.SensitiveUtil;
import com.yuweix.kuafu.core.serialize.fastjson.FastjsonDateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author ruoyi
 */
@Configuration
public class SpringMvcConf implements WebMvcConfigurer {
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;
    @Resource
    private LoginCheckInterceptor loginCheckInterceptor;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**").addResourceLocations("file:" + AppConf.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(mediaTypes);

        /**
         * 脱敏配置
         */
        FastJsonConfig fastJsonConf = fastConverter.getFastJsonConfig();
        Filter[] filters = fastJsonConf.getWriterFilters();
        List<Filter> filterList = filters == null || filters.length <= 0
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(filters));
        filterList.add((ValueFilter) SensitiveUtil::shield);
        filterList.add(new FastjsonDateFilter());
        fastJsonConf.setWriterFilters(filterList.toArray(new Filter[0]));
        converters.add(0, fastConverter);
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/**");
    }

//    /**
//     * 跨域配置
//     */
//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        // 设置访问源地址
//        config.addAllowedOrigin("*");
//        // 设置访问源请求头
//        config.addAllowedHeader("*");
//        // 设置访问源请求方法
//        config.addAllowedMethod("*");
//        // 对接口配置跨域设置
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
}