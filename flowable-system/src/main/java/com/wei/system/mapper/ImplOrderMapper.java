package com.wei.system.mapper;


import com.wei.system.domain.ImplOrder;
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
public interface ImplOrderMapper extends BaseMapper<ImplOrder, Long> {
    @SelectProvider(type = Provider.class, method = "queryImplOrderCount")
    int queryImplOrderCount(@Param("implId")long implId);

    @SelectProvider(type = Provider.class, method = "queryImplOrderList")
    List<ImplOrder> queryImplOrderList(@Param("implId")long implId
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    @SelectProvider(type = Provider.class, method = "findImplOrder")
    ImplOrder findImplOrder(@Param("implId")long implId, @Param("orderId")long orderId);

    @SelectProvider(type = Provider.class, method = "selectListOrderId")
    List<ImplOrder> selectListOrderId(@Param("orderId") Long orderId);


    class Provider extends AbstractProvider {
        public String selectListOrderId(Map<String, Object> param){
            Long orderId = (Long) param.get("orderId");
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ImplOrder.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ImplOrder.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            return builder.toString();
        }




        public String queryImplOrderCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(ImplOrder.class)).append(" a ");
            builder.append(" where a.impl_id = #{implId} ");
            return builder.toString();
        }

        public String queryImplOrderList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ImplOrder.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ImplOrder.class)).append(" a ");
            builder.append(" where a.impl_id = #{implId} ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String findImplOrder(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ImplOrder.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ImplOrder.class)).append(" a ");
            builder.append(" where a.impl_id = #{implId} and a.order_id = #{orderId} ");
            return builder.toString();
        }
    }
}
