package com.wei.system.service.impl;


import com.wei.common.constant.Constants;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.system.domain.SysCustomer;
import com.wei.system.domain.vo.CustomerDropDownVo;
import com.wei.system.domain.vo.CustomerVo;
import com.wei.system.mapper.SysCustomerMapper;
import com.wei.system.mapper.SysUserMapper;
import com.wei.system.service.ISysConfigService;
import com.wei.system.service.ISysCustomerService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.sequence.base.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 岗位信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysCustomerServiceImpl implements ISysCustomerService {
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysCustomerMapper customerMapper;
    @Autowired
    private ISysConfigService configService;

    @Resource
    private Sequence seqCustomer;


    @Override
    public int queryCustomerCount(Long id, String customerNo, String enterpriseName, Long salesUserId
            , String keywords, Integer statusCode) {
        return customerMapper.queryCustomerCount(id, customerNo, enterpriseName, salesUserId
                , keywords, statusCode);
    }

    @Override
    public List<CustomerVo> queryCustomerList(Long id, String customerNo, String enterpriseName, Long salesUserId
            , String keywords, Integer statusCode, int pageNo, int pageSize) {
        List<SysCustomer> list = customerMapper.queryCustomerList(id, customerNo, enterpriseName, salesUserId
                , keywords, statusCode, pageNo, pageSize);
        return list == null || list.size() <= 0
                ? new ArrayList<>()
                : list.stream().map(this::toCustomerVo).collect(Collectors.toList());
    }
    private CustomerVo toCustomerVo(SysCustomer c) {
        if (c == null) {
            return null;
        }
        Long salesUserId = c.getSalesUserId();
        SysUser sysUser = salesUserId == null ? null : userMapper.selectUserById(salesUserId);

        CustomerVo vo = new CustomerVo();
        vo.setId(c.getId());
        vo.setCustomerNo(c.getCustomerNo());
        vo.setEnterpriseName(c.getEnterpriseName());
        vo.setContacts(c.getContacts());
        vo.setPhoneNo(c.getPhoneNo());
        vo.setEmail(c.getEmail());
        vo.setContacts2(c.getContacts2());
        vo.setPhoneNo2(c.getPhoneNo2());
        vo.setEmail2(c.getEmail2());
        vo.setTenantId(c.getTenantId());
        vo.setStatusCode(c.getStatusCode() == null ? null : String.valueOf(c.getStatusCode()));
        vo.setTypeCode(c.getTypeCode() == null ? null : String.valueOf(c.getTypeCode()));
        vo.setIndustryCode(c.getIndustryCode() == null ? null : String.valueOf(c.getIndustryCode()));
        vo.setAddr(c.getAddr());
        vo.setFpTitle(c.getFpTitle());
        vo.setFpBankName(c.getFpBankName());
        vo.setFpBankAccNo(c.getFpBankAccNo());
        vo.setFpPhoneNo(c.getFpPhoneNo());
        vo.setFpTaxNo(c.getFpTaxNo());
        vo.setFpAddr(c.getFpAddr());
        vo.setSalesUserId(salesUserId);
        vo.setSalesUserName(sysUser == null ? null : sysUser.getUserName());
        vo.setSalesNickName(sysUser == null ? null : sysUser.getNickName());
        vo.setSalesUserPhone(sysUser == null ? null : sysUser.getPhonenumber());
        vo.setSalesUserEmail(sysUser == null ? null : sysUser.getEmail());
        vo.setRemark(c.getRemark());
        vo.setCreator(c.getCreator());
        vo.setCreateTime(c.getCreateTime() == null ? "" : DateUtil.formatDate(c.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        vo.setModifier(c.getModifier());
        vo.setModifyTime(c.getModifyTime() == null ? "" : DateUtil.formatDate(c.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
        return vo;
    }

    @Override
    public List<CustomerDropDownVo> getCustomerOptionList(Long salesId, String keywords, int pageNo, int pageSize) {
        List<SysCustomer> list = customerMapper.queryCustomerList(null, null, null, salesId
                , keywords, null, pageNo, pageSize);
        return list == null || list.size() <= 0
                ? new ArrayList<>()
                : list.stream().map(c -> {
                    String customerNo = c.getCustomerNo();
                    String enterpriseName = c.getEnterpriseName();

                    return CustomerDropDownVo.builder().value(customerNo)
                            .label("[" + customerNo + "]" + enterpriseName)
                            .contacts(c.getContacts())
                            .phoneNo(c.getPhoneNo())
                            .email(c.getEmail())
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    public CustomerVo findCustomerById(long id) {
        SysCustomer c = customerMapper.selectOneById(id, SysCustomer.class);
        return toCustomerVo(c);
    }

    @Override
    public CustomerVo findCustomerByNo(String customerNo) {
        SysCustomer cust = customerMapper.getByCustomerNo(customerNo);
        return cust == null ? null : toCustomerVo(cust);
    }

    @Transactional
    @Override
    public long createCustomer(String enterpriseName, String contacts, String phoneNo, String email
            , String contacts2, String phoneNo2, String email2
            , String tenantId, Integer statusCode, Integer typeCode, Integer industryCode, String addr
            , String fpTitle, String fpBankName, String fpBankAccNo, String fpPhoneNo, String fpTaxNo
            , String fpAddr, Long salesUserId, String remark, String accountNo) {
        SysCustomer c = new SysCustomer();
        c.setId(seqCustomer.next());
        c.setCustomerNo(generateCustomerNo());
        c.setEnterpriseName(enterpriseName);
        c.setContacts(contacts);
        c.setPhoneNo(phoneNo);
        c.setEmail(email);
        c.setContacts2(contacts2);
        c.setPhoneNo2(phoneNo2);
        c.setEmail2(email2);
        c.setTenantId(tenantId);
        c.setStatusCode(statusCode);
        c.setTypeCode(typeCode);
        c.setIndustryCode(industryCode);
        c.setAddr(addr);
        c.setFpTitle(fpTitle);
        c.setFpBankName(fpBankName);
        c.setFpBankAccNo(fpBankAccNo);
        c.setFpPhoneNo(fpPhoneNo);
        c.setFpTaxNo(fpTaxNo);
        c.setFpAddr(fpAddr);
        c.setSalesUserId(salesUserId);
        c.setRemark(remark);
        c.setCreator(accountNo);
        c.setCreateTime(new Date());
        customerMapper.insertSelective(c);
        return c.getId();
    }

    @Transactional
    @Override
    public void updateCustomer(long id, String enterpriseName, String contacts, String phoneNo, String email
            , String contacts2, String phoneNo2, String email2
            , String tenantId, Integer statusCode, Integer typeCode, Integer industryCode, String addr
            , String fpTitle, String fpBankName, String fpBankAccNo, String fpPhoneNo, String fpTaxNo
            , String fpAddr,  String remark, String accountNo) {
        SysCustomer c = customerMapper.selectOneById(id, SysCustomer.class);
        if (c == null) {
            throw new RuntimeException("数据不存在[id=" + id + "]");
        }

        c.setEnterpriseName(enterpriseName);
        c.setContacts(contacts);
        c.setPhoneNo(phoneNo);
        c.setEmail(email);
        c.setContacts2(contacts2);
        c.setPhoneNo2(phoneNo2);
        c.setEmail2(email2);
        c.setTenantId(tenantId);
        c.setStatusCode(statusCode);
        c.setTypeCode(typeCode);
        c.setIndustryCode(industryCode);
        c.setAddr(addr);
        c.setFpTitle(fpTitle);
        c.setFpBankName(fpBankName);
        c.setFpBankAccNo(fpBankAccNo);
        c.setFpPhoneNo(fpPhoneNo);
        c.setFpTaxNo(fpTaxNo);
        c.setFpAddr(fpAddr);
        c.setRemark(remark);
        c.setModifyTime(new Date());
        c.setModifier(accountNo);
        int i = customerMapper.updateByPrimaryKey(c);
        if (i < 0) {
            throw new RuntimeException("修改失败!");
        }
    }

    @Transactional
    @Override
    public void deleteCustomer(long id) {
        customerMapper.deleteByKey(id, SysCustomer.class);
    }


    public String generateCustomerNo() {
        String key = Constants.SYS_CONFIG_KEY_MAX_CUSTOMER_NO;
        String oldVal = configService.findValueByKey(key);
        if (oldVal == null) {
            throw new RuntimeException("客户当前最大编号未配置");
        }
        long maxNo = Long.parseLong(oldVal);
        maxNo = maxNo + 1;
        boolean updateSuccess = configService.updateConfigByKV(key, "" + maxNo, oldVal, "system");
        if (!updateSuccess) {
            throw new RuntimeException("生成客户编号失败，请重试！");
        }
        return "A" + String.format("%06d", maxNo);
    }
}
