package com.wei.system.mapper;


import com.wei.system.domain.OrderSpecialLine;
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
public interface OrderSpecialLineMapper extends BaseMapper<OrderSpecialLine, Long> {
    @SelectProvider(type = Provider.class, method = "queryOrderSpecialLineCount")
    int queryOrderSpecialLineCount(@Param("orderId")long orderId);

    @SelectProvider(type = Provider.class, method = "queryOrderSpecialLineList")
    List<OrderSpecialLine> queryOrderSpecialLineList(@Param("orderId")long orderId
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryOrderSpecialLineCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(OrderSpecialLine.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            return builder.toString();
        }

        public String queryOrderSpecialLineList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(OrderSpecialLine.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(OrderSpecialLine.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
