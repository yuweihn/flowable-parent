package com.wei.system.mapper;


import com.wei.system.domain.Impl;
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
public interface ImplMapper extends BaseMapper<Impl, Long> {
    @SelectProvider(type = Provider.class, method = "queryImplByProcInsId")
    Impl queryImplByProcInsId(@Param("procInsId")String procInsId);

    @SelectProvider(type = Provider.class, method = "queryImplCount")
    int queryImplCount(@Param("customerNo")String customerNo, @Param("title")String title
            , @Param("fuzzyImplNo") String fuzzyImplNo, @Param("preSalesId")Long preSalesId
            , @Param("prjCode")String prjCode, @Param("startTime")Date startTime, @Param("endTime")Date endTime);

    @SelectProvider(type = Provider.class, method = "queryImplList")
    List<Impl> queryImplList(@Param("customerNo")String customerNo, @Param("title")String title
            , @Param("fuzzyImplNo") String fuzzyImplNo, @Param("preSalesId")Long preSalesId
            , @Param("prjCode")String prjCode, @Param("startTime")Date startTime, @Param("endTime")Date endTime
            , @Param("pageNo")Integer pageNo, @Param("pageSize")Integer pageSize);

    class Provider extends AbstractProvider {
        public String queryImplByProcInsId(Map<String, Object> param) {
            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Impl.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Impl.class)).append(" a ");
            builder.append(" where a.proc_ins_id = #{procInsId} ");
            return builder.toString();
        }

        public String queryImplCount(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            String title = (String) param.get("title");
            String fuzzyImplNo = (String) param.get("fuzzyImplNo");
            String prjCode = (String) param.get("prjCode");
            Long preSalesId = (Long) param.get("preSalesId");
            Date startTime= (Date) param.get("startTime");
            Date endTime= (Date) param.get("endTime");
            StringBuilder builder = new StringBuilder("");
            builder.append(" select count(a.id) as cnt ");
            builder.append(" from ").append(PersistUtil.getTableName(Impl.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and a.title like #{title} ");
            }
            if (fuzzyImplNo != null && !"".equals(fuzzyImplNo.trim())) {
                param.put("fuzzyImplNo", "%" + fuzzyImplNo.trim() + "%");
                builder.append(" and a.impl_no like #{fuzzyImplNo} ");
            }
            if (prjCode != null) {
                builder.append(" and a.prj_code = #{prjCode} ");
            }
            if (preSalesId != null) {
                builder.append(" and a.pre_sales_id = #{preSalesId} ");
            }
            if (startTime != null ) {
                builder.append(" and a.create_time >= #{startTime} ");
            }
            if (endTime != null) {
                builder.append(" and a.create_time <= #{endTime} ");
            }

            return builder.toString();
        }

        public String queryImplList(Map<String, Object> param) {
            String customerNo = (String) param.get("customerNo");
            String title = (String) param.get("title");
            String fuzzyImplNo = (String) param.get("fuzzyImplNo");
            String prjCode = (String) param.get("prjCode");
            Long preSalesId = (Long) param.get("preSalesId");
            Date startTime = (Date) param.get("startTime");
            Date endTime = (Date) param.get("endTime");
            Integer pageNo = (Integer) param.get("pageNo");
            Integer pageSize = (Integer) param.get("pageSize");

            StringBuilder builder = new StringBuilder("");
            builder.append(" select ").append(PersistUtil.getAllColumnSql(Impl.class, "a"));
            builder.append(" from ").append(PersistUtil.getTableName(Impl.class)).append(" a ");
            builder.append(" where 1 = 1 ");
            if (customerNo != null && !"".equals(customerNo.trim())) {
                param.put("customerNo", customerNo.trim());
                builder.append(" and a.customer_no = #{customerNo} ");
            }
            if (title != null && !"".equals(title.trim())) {
                param.put("title", "%" + title.trim() + "%");
                builder.append(" and a.title like #{title} ");
            }
            if (fuzzyImplNo != null && !"".equals(fuzzyImplNo.trim())) {
                param.put("fuzzyImplNo", "%" + fuzzyImplNo.trim() + "%");
                builder.append(" and a.impl_no like #{fuzzyImplNo} ");
            }
            if (preSalesId != null) {
                builder.append(" and a.pre_sales_id = #{preSalesId} ");
            }
            if (prjCode != null) {
                builder.append(" and a.prj_code = #{prjCode} ");
            }
            if (startTime != null ) {
                builder.append(" and a.create_time >= #{startTime} ");
            }
            if (endTime != null) {
                builder.append(" and a.create_time <= #{endTime} ");
            }
            builder.append(" order by a.id desc");
            if (pageNo != null && pageSize != null) {
                builder.append(" limit ").append((pageNo - 1) * pageSize).append(", ").append(pageSize);
            }
            return builder.toString();
        }
    }
}
