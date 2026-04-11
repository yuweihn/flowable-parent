package com.wei.system.mapper;


import com.wei.system.domain.OrderIdc;
import com.yuweix.kuafu.dao.PersistUtil;
import com.yuweix.kuafu.dao.mybatis.BaseMapper;
import com.yuweix.kuafu.dao.mybatis.provider.AbstractProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public interface OrderIdcMapper extends BaseMapper<OrderIdc, Long> {
    @SelectProvider(type = Provider.class, method = "queryOrderIdcCount")
    int queryOrderIdcCount(@Param("orderId")long orderId);

    @SelectProvider(type = Provider.class, method = "queryOrderIdcList")
    List<OrderIdc> queryOrderIdcList(@Param("orderId")long orderId
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryOrderIdcCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(OrderIdc.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            return builder.toString();
        }

        public String queryOrderIdcList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(OrderIdc.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(OrderIdc.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
