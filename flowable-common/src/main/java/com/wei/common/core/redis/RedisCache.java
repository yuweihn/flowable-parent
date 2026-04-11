package com.wei.common.core.redis;


import com.wei.common.constant.Constants;
import com.yuweix.kuafu.data.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;


/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Deprecated
@Component("rdsCache")
public class RedisCache {
    @Autowired
    @Qualifier("redisTemplate")
    public RedisTemplate rdsTemplate;
    @Resource
    public Cache cache;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        cache.put(key, value, Constants.DEFUALT_TIMEOUT);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间(s)
     */
    public <T> void setCacheObject(final String key, final T value, final long timeout)
    {
        cache.put(key, value, timeout);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        return cache.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteObject(final String key)
    {
        cache.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection)
    {
        return rdsTemplate.delete(collection);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return rdsTemplate.keys(pattern);
    }
}
