package com.wei.system.mapper;


import com.wei.system.domain.ContractAttach;
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
public interface ContractAttachMapper extends BaseMapper<ContractAttach, Long> {
    @SelectProvider(type = Provider.class, method = "queryContractAttachCount")
    int queryContractAttachCount(@Param("contractId")long contractId, @Param("attachUrl")String attachUrl
            , @Param("type")Byte type);

    @SelectProvider(type = Provider.class, method = "queryContractAttachList")
    List<ContractAttach> queryContractAttachList(@Param("contractId")long contractId, @Param("attachUrl")String attachUrl
            , @Param("type")Byte type
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);



    class Provider extends AbstractProvider {
        public String queryContractAttachCount(Map<String, Object> param) {
            String attachUrl = (String) param.get("attachUrl");
            Byte type = (Byte) param.get("type");
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(ContractAttach.class)).append(" a ");
            builder.append(" where a.contract_id = #{contractId} ");
            if (attachUrl != null && !"".equals(attachUrl.trim())) {
                param.put("attachUrl", attachUrl.trim());
                builder.append(" and a.attach_url = #{attachUrl} ");
            }
            if (type != null) {
                builder.append(" and a.type_ = #{type} ");
            }

            return builder.toString();
        }

        public String queryContractAttachList(Map<String, Object> param) {
            String attachUrl = (String) param.get("attachUrl");
            Byte type = (Byte) param.get("type");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(ContractAttach.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(ContractAttach.class)).append(" a ");
            builder.append(" where a.contract_id = #{contractId} ");
            if (attachUrl != null && !"".equals(attachUrl.trim())) {
                param.put("attachUrl", attachUrl.trim());
                builder.append(" and a.attach_url = #{attachUrl} ");
            }
            if (type != null) {
                builder.append(" and a.type_ = #{type} ");
            }
            builder.append(" order by a.id desc");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
