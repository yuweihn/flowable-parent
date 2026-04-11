package com.wei.system.mapper;


import com.wei.system.domain.Contract;
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
public interface ContractMapper extends BaseMapper<Contract, Long> {
    @SelectProvider(type = Provider.class, method = "queryContractByProcInsId")
    Contract queryContractByProcInsId(@Param("procInsId")String procInsId);

    @SelectProvider(type = Provider.class, method = "queryContractCount")
    int queryContractCount(@Param("customerNo")String customerNo, @Param("userId")Long userId, @Param("title")String title
            , @Param("fuzzyContractNo") String fuzzyContractNo, @Param("startTime")Date startTime, @Param("endTime")Date endTime);

    @SelectProvider(type = Provider.class, method = "queryContractList")
    List<Contract> queryContractList(@Param("customerNo")String customerNo, @Param("userId")Long userId, @Param("title")String title
            , @Param("fuzzyContractNo") String fuzzyContractNo, @Param("startTime")Date starTime, @Param("endTime")Date end
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryContractByProcInsId(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Contract.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Contract.class)).append(" a ");
            builder.append(" where a.proc_ins_id = #{procInsId} ");
            return builder.toString();
        }
        public String queryContractCount(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            Long userId = (Long) param.get("userId");
            String title = (String) param.get("title");
            String fuzzyContractNo = (String) param.get("fuzzyContractNo");
            Date startTime = (Date) param.get("startTime");
            Date endTime = (Date) param.get("endTime");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(Contract.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (userId != null) {
                builder.append(" and a.user_id = #{userId} ");
            }
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and a.title like #{title} ");
            }
            if (fuzzyContractNo != null && !"".equals(fuzzyContractNo.trim())) {
                param.put("fuzzyContractNo", "%" + fuzzyContractNo.trim() + "%");
                builder.append(" and a.contract_no like #{fuzzyContractNo} ");
            }
            if (startTime != null ) {
                builder.append(" and a.create_time >= #{startTime} ");
            }
            if (endTime != null) {
                builder.append(" and a.create_time <= #{endTime} ");
            }
            return builder.toString();
        }

        public String queryContractList(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            Long userId = (Long) param.get("userId");
            String title = (String) param.get("title");
            String fuzzyContractNo = (String) param.get("fuzzyContractNo");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");
            Date startTime = (Date) param.get("startTime");
            Date endTime = (Date) param.get("endTime");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Contract.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Contract.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (userId != null) {
                builder.append(" and a.user_id = #{userId} ");
            }
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and a.title like #{title} ");
            }
            if (fuzzyContractNo != null && !"".equals(fuzzyContractNo.trim())) {
                param.put("fuzzyContractNo", "%" + fuzzyContractNo.trim() + "%");
                builder.append(" and a.contract_no like #{fuzzyContractNo} ");
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
    }
}
