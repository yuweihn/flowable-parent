package com.wei.system.mapper;


import com.wei.system.domain.ContractOrder;
import com.wei.system.domain.Order;
import com.yuweix.kuafu.dao.PersistUtil;
import com.yuweix.kuafu.dao.mybatis.BaseMapper;
import com.yuweix.kuafu.dao.mybatis.provider.AbstractProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public interface OrderMapper extends BaseMapper<Order, Long> {
    @SelectProvider(type = Provider.class, method = "queryOrderByProcInsId")
    Order queryOrderByProcInsId(@Param("procInsId") String procInsId);

    @SelectProvider(type = Provider.class, method = "queryOrderCount")
    int queryOrderCount(@Param("customerNo") String customerNo, @Param("userId") Long userId, @Param("keywords") String keywords
            , @Param("fuzzyOrderNo") String fuzzyOrderNo, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    @SelectProvider(type = Provider.class, method = "queryOrderList")
    List<Order> queryOrderList(@Param("customerNo") String customerNo, @Param("userId") Long userId, @Param("keywords") String keywords
            , @Param("fuzzyOrderNo") String fuzzyOrderNo, @Param("startTime") Date startTime, @Param("endTime") Date endTime
            , @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    /**
     * 查询指定客户未绑定合同的订单列表
     */
    @SelectProvider(type = Provider.class, method = "getUnboundContractOrderListByCustomerNo")
    List<Order> getUnboundContractOrderListByCustomerNo(@Param("customerNo") String customerNo, @Param("title") String title
            , @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    /**
     * 根据合同ID查询订单列表
     */
    @SelectProvider(type = Provider.class, method = "getOrderListByContractId")
    List<Order> getOrderListByContractId(@Param("contractId") long contractId, @Param("title") String title
            , @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryOrderByProcInsId(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Order.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Order.class)).append(" a ");
            builder.append(" where a.proc_ins_id = #{procInsId} ");
            return builder.toString();
        }

        public String queryOrderCount(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            Long userId = (Long) param.get("userId");
            String keywords = (String) param.get("keywords");
            String fuzzyOrderNo = (String) param.get("fuzzyOrderNo");
            Date startTime = (Date) param.get("startTime");
            Date endTime = (Date) param.get("endTime");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(Order.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (userId != null) {
                builder.append(" and a.user_id = #{userId} ");
            }
            if (keywords != null && !"".equals(keywords.trim())) {
                param.put("keywords", "%" + keywords.trim() + "%");
                builder.append(" and a.title like #{keywords} ");
            }
            if (fuzzyOrderNo != null && !"".equals(fuzzyOrderNo.trim())) {
                param.put("fuzzyOrderNo", "%" + fuzzyOrderNo.trim() + "%");
                builder.append(" and a.order_no like #{fuzzyOrderNo} ");
            }
            if (startTime != null ) {
                builder.append(" and a.create_time >= #{startTime} ");
            }
            if (endTime != null) {
                builder.append(" and a.create_time <= #{endTime} ");
            }

            return builder.toString();
        }

        public String queryOrderList(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            Long userId = (Long) param.get("userId");
            String keywords = (String) param.get("keywords");
            String fuzzyOrderNo = (String) param.get("fuzzyOrderNo");
            Date startTime = (Date) param.get("startTime");
            Date endTime = (Date) param.get("endTime");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Order.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Order.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (userId != null) {
                builder.append(" and a.user_id = #{userId} ");
            }
            if (keywords != null && !"".equals(keywords.trim())) {
                param.put("keywords", "%" + keywords.trim() + "%");
                builder.append(" and a.title like #{keywords} ");
            }
            if (fuzzyOrderNo != null && !"".equals(fuzzyOrderNo.trim())) {
                param.put("fuzzyOrderNo", "%" + fuzzyOrderNo.trim() + "%");
                builder.append(" and a.order_no like #{fuzzyOrderNo} ");
            }
            if (startTime != null ) {
                builder.append(" and a.create_time >= #{startTime} ");
            }
            if (endTime != null) {
                builder.append(" and a.create_time <= #{endTime} ");
            }
            builder.append(" order by a.id desc ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String getUnboundContractOrderListByCustomerNo(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            String title = (String) param.get("title");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select distinct ").append(PersistUtil.getAllColumnSql(Order.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Order.class)).append(" a ");
            builder.append(" where a.customer_no = #{customerNo} ");
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and a.title like #{title} ");
            }
            builder.append(" and not exists (select b.id from ").append(PersistUtil.getTableName(ContractOrder.class)).append(" b where b.order_id = a.id) ");
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String getOrderListByContractId(Map<String, Object> param) {
            long contractId = (long) param.get("contractId");
            String title = (String) param.get("title");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select distinct ").append(PersistUtil.getAllColumnSql(Order.class, "b"));
            builder.append(" from ").append(PersistUtil.getTableName(ContractOrder.class)).append(" a ");
            builder.append(" inner join ").append(PersistUtil.getTableName(Order.class)).append(" b on a.order_id = b.id ");
            builder.append(" where a.contract_id = #{contractId} ");
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and b.title like #{title} ");
            }
            builder.append(" order by b.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
