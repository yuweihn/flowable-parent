package com.wei.system.mapper;


import com.wei.system.domain.FlowAuthNodeSetting;
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
public interface FlowAuthNodeSettingMapper extends BaseMapper<FlowAuthNodeSetting, Long> {
    @SelectProvider(type = Provider.class, method = "queryNodeSettingCount")
    int queryNodeSettingCount(@Param("procDefId") String procDefId, @Param("nodeKey") String nodeKey);

    @SelectProvider(type = Provider.class, method = "queryNodeSettingList")
    List<FlowAuthNodeSetting> queryNodeSettingList(@Param("procDefId") String procDefId, @Param("nodeKey") String nodeKey
            , @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    @SelectProvider(type = Provider.class, method = "findWNodeSetting")
    FlowAuthNodeSetting findWNodeSetting(@Param("procDefId") String procDefId, @Param("nodeKey") String nodeKey
            , @Param("widgetId") long widgetId);

    @SelectProvider(type = Provider.class, method = "findNodeKeyListByProcDefId")
    List<String> findNodeKeyListByProcDefId(@Param("procDefId") String procDefId);

    @SelectProvider(type = Provider.class, method = "deleteFlowAuthNode")
    void deleteFlowAuthNode(@Param("procDefId") String procDefId, @Param("nodeKey") String nodeKey);

    class Provider extends AbstractProvider {
        public String queryNodeSettingCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthNodeSetting.class)).append(" a ");
            builder.append(" where a.proc_def_id = #{procDefId} and a.node_key = #{nodeKey} ");
            return builder.toString();
        }

        public String queryNodeSettingList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(FlowAuthNodeSetting.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthNodeSetting.class)).append(" a ");
            builder.append(" where a.proc_def_id = #{procDefId} and a.node_key = #{nodeKey} ");
            builder.append(" order by a.id desc ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String findWNodeSetting(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(FlowAuthNodeSetting.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthNodeSetting.class)).append(" a ");
            builder.append(" where a.proc_def_id = #{procDefId} and a.node_key = #{nodeKey} and a.auth_widget_id = #{widgetId} ");
            return builder.toString();
        }

        public String findNodeKeyListByProcDefId(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select distinct node_key ");
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthNodeSetting.class));
            builder.append(" where proc_def_id = #{procDefId} ");
            return builder.toString();
        }

        public String deleteFlowAuthNode(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" delete ");
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthNodeSetting.class));
            builder.append(" where proc_def_id = #{procDefId} and node_key = #{nodeKey} ");
            return builder.toString();
        }
    }
}
