package com.wei.system.mapper;


import com.wei.system.domain.SysCustomer;
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
public interface SysCustomerMapper extends BaseMapper<SysCustomer, Long> {
    @SelectProvider(type = Provider.class, method = "getByCustomerNo")
    SysCustomer getByCustomerNo(@Param("customerNo")String customerNo);

    @SelectProvider(type = Provider.class, method = "queryCustomerCount")
    int queryCustomerCount(@Param("id")Long id, @Param("customerNo")String customerNo
            , @Param("enterpriseName")String enterpriseName, @Param("salesUserId")Long salesUserId
            , @Param("keywords")String keywords, @Param("statusCode")Integer statusCode);

    @SelectProvider(type = Provider.class, method = "queryCustomerList")
    List<SysCustomer> queryCustomerList(@Param("id")Long id, @Param("customerNo")String customerNo
            , @Param("enterpriseName")String enterpriseName, @Param("salesUserId")Long salesUserId
            ,@Param("keywords")String keywords, @Param("statusCode")Integer statusCode
            , @Param("pageNo")int pageNo, @Param("pageSize")int pageSize);

    class Provider extends AbstractProvider {
        public String getByCustomerNo(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(SysCustomer.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(SysCustomer.class)).append(" a ");
            builder.append(" where a.customer_no = #{customerNo} ");
            return builder.toString();
        }

        public String queryCustomerCount(Map<String, Object> param) {
            Long id = (Long) param.get("id");
            String customerNo = (String) param.get("customerNo");
            String enterpriseName = (String) param.get("enterpriseName");
            Long salesUserId = (Long) param.get("salesUserId");
            String keywords = (String) param.get("keywords");
            Integer statusCode = (Integer) param.get("statusCode");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(SysCustomer.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (id != null) {
                builder.append(" and a.id = #{id} ");
            }
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", "%" + customerNo.trim() + "%");
                builder.append(" and a.customer_no like #{customerNo} ");
            }
            if (enterpriseName != null && !"".equals(enterpriseName.trim())) {
                param.put("enterpriseName", "%" + enterpriseName.trim() + "%");
                builder.append(" and a.enterprise_name like #{enterpriseName} ");
            }
            if (salesUserId != null) {
                builder.append(" and a.sales_user_id = #{salesUserId} ");
            }
            if (keywords != null && !"".equals(keywords.trim())) {
                param.put("keywords", "%" + keywords.trim() + "%");
                builder.append(" and (a.customer_no like #{keywords} or a.enterprise_name like #{keywords} or a.contacts like #{keywords}) ");
            }
            if (statusCode != null) {
                builder.append(" and a.status_code = #{statusCode} ");
            }
            return builder.toString();
        }

        public String queryCustomerList(Map<String, Object> param) {
            Long id = (Long) param.get("id");
            String customerNo = (String) param.get("customerNo");
            String enterpriseName = (String) param.get("enterpriseName");
            Long salesUserId = (Long) param.get("salesUserId");
            String keywords = (String) param.get("keywords");
            Integer statusCode = (Integer) param.get("statusCode");
            int pageNo = (int) param.get("pageNo");
            int pageSize = (int) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(SysCustomer.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(SysCustomer.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (id != null) {
                builder.append(" and a.id = #{id} ");
            }
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", "%" + customerNo.trim() + "%");
                builder.append(" and a.customer_no like #{customerNo} ");
            }
            if (enterpriseName != null && !"".equals(enterpriseName.trim())) {
                param.put("enterpriseName", "%" + enterpriseName.trim() + "%");
                builder.append(" and a.enterprise_name like #{enterpriseName} ");
            }
            if (salesUserId != null) {
                builder.append(" and a.sales_user_id = #{salesUserId} ");
            }
            if (keywords != null && !"".equals(keywords.trim())) {
                param.put("keywords", "%" + keywords.trim() + "%");
                builder.append(" and (a.customer_no like #{keywords} or a.enterprise_name like #{keywords} or a.contacts like #{keywords}) ");
            }
            if (statusCode != null) {
                builder.append(" and a.status_code = #{statusCode} ");
            }
            builder.append(" order by a.id ");
            builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            return builder.toString();
        }
    }
}
