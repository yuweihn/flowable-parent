package com.wei.system.mapper;


import com.wei.system.domain.SysForm;
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
public interface SysFormMapper extends BaseMapper<SysForm, Long> {
    @SelectProvider(type = Provider.class, method = "queryFormCount")
    int queryFormCount(@Param("formName")String formName);

    @SelectProvider(type = Provider.class, method = "queryFormList")
    List<SysForm> queryFormList(@Param("formName")String formName, @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);


    class Provider extends AbstractProvider {
        public String queryFormCount(Map<String, Object> param) {
            String formName = (String) param.get("formName");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(SysForm.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (formName != null && !"".equals(formName.trim())) {
                param.put("formName", "%" + formName.trim() + "%");
                builder.append(" and a.form_name like #{formName} ");
            }
            return builder.toString();
        }

        public String queryFormList(Map<String, Object> param) {
            String formName = (String) param.get("formName");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(SysForm.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(SysForm.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (formName != null && !"".equals(formName.trim())) {
                param.put("formName", "%" + formName.trim() + "%");
                builder.append(" and a.form_name like #{formName} ");
            }
            builder.append(" order by a.id ");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
