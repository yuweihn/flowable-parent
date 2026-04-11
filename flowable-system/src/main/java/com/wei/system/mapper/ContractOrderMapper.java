package com.wei.system.mapper;


import com.wei.system.domain.ContractOrder;
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
public interface ContractOrderMapper extends BaseMapper<ContractOrder, Long> {
    @SelectProvider(type = Provider.class, method = "queryContractOrderCount")
    int queryContractOrderCount(@Param("contractId")long contractId);

    @SelectProvider(type = Provider.class, method = "queryContractOrderList")
    List<ContractOrder> queryContractOrderList(@Param("contractId")long contractId
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    @SelectProvider(type = Provider.class, method = "findContractOrderByOrderId")
    ContractOrder findContractOrderByOrderId(@Param("orderId")long orderId);

    class Provider extends AbstractProvider {
        public String queryContractOrderCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(ContractOrder.class)).append(" a ");
            builder.append(" where a.contract_id = #{contractId} ");
            return builder.toString();
        }

        public String queryContractOrderList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ContractOrder.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ContractOrder.class)).append(" a ");
            builder.append(" where a.contract_id = #{contractId} ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String findContractOrderByOrderId(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ContractOrder.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ContractOrder.class)).append(" a ");
            builder.append(" where a.order_id = #{orderId} ");
            return builder.toString();
        }
    }
}
