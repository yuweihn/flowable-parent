package com.wei.system.mapper;


import com.wei.system.domain.FlowAuthWidget;
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
public interface FlowAuthWidgetMapper extends BaseMapper<FlowAuthWidget, Long> {
    @SelectProvider(type = Provider.class, method = "queryWidgetCount")
    int queryWidgetCount(@Param("procCategory") String procCategory);

    @SelectProvider(type = Provider.class, method = "queryWidgetList")
    List<FlowAuthWidget> queryWidgetList(@Param("procCategory") String procCategory
            , @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    @SelectProvider(type = Provider.class, method = "findWidget")
    FlowAuthWidget findWidget(@Param("procCategory") String procCategory, @Param("widgetCode") String widgetCode);

    class Provider extends AbstractProvider {
        public String queryWidgetCount(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthWidget.class)).append(" a ");
            builder.append(" where a.proc_category = #{procCategory} ");
            return builder.toString();
        }

        public String queryWidgetList(Map<String, Object> param) {
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(FlowAuthWidget.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthWidget.class)).append(" a ");
            builder.append(" where a.proc_category = #{procCategory} ");
            builder.append(" order by a.id desc ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }

        public String findWidget(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(FlowAuthWidget.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(FlowAuthWidget.class)).append(" a ");
            builder.append(" where a.proc_category = #{procCategory} and a.widget_code = #{widgetCode} ");
            return builder.toString();
        }
    }
}
