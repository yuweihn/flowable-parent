package com.wei.system.mapper;


import com.wei.system.domain.OrderDevice;
import com.yuweix.tripod.dao.PersistUtil;
import com.yuweix.tripod.dao.mybatis.BaseMapper;
import com.yuweix.tripod.dao.mybatis.provider.AbstractProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public interface OrderDeviceMapper extends BaseMapper<OrderDevice, Long> {
    @SelectProvider(type = Provider.class, method = "queryOrderDeviceCount")
    int queryOrderDeviceCount(@Param("orderId")long orderId);

    @SelectProvider(type = Provider.class, method = "queryOrderDeviceList")
    List<OrderDevice> queryOrderDeviceList(@Param("orderId")long orderId
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryOrderDeviceCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(OrderDevice.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            return builder.toString();
        }

        public String queryOrderDeviceList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(OrderDevice.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(OrderDevice.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
