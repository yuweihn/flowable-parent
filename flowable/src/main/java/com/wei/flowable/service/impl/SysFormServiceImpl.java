package com.wei.flowable.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.wei.flowable.domain.dto.SysFormDto;
import com.wei.common.enums.FormType;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.sequence.base.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wei.system.mapper.SysFormMapper;
import com.wei.system.domain.SysForm;
import com.wei.flowable.service.ISysFormService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 流程表单Service业务层处理
 * 
 * @author XuanXuan Xuan
 * @date 2021-04-03
 */
@Service
public class SysFormServiceImpl implements ISysFormService {
    @Autowired
    private SysFormMapper sysFormMapper;
    @Resource
    private Sequence seqSysForm;


    /**
     * 查询流程表单
     * 
     * @param formId 流程表单ID
     * @return 流程表单
     */
    @Override
    public SysFormDto selectSysFormById(Long formId) {
        SysForm frm = sysFormMapper.selectOneById(formId, SysForm.class);
        return toSysFormDto(frm);
    }

    @Override
    public int findFormCount(String formName) {
        return sysFormMapper.queryFormCount(formName);
    }
    /**
     * 查询流程表单列表
     * @return 流程表单
     */
    @Override
    public List<SysFormDto> findFormList(String formName, Integer pageNo, Integer pageSize) {
        List<SysForm> list = sysFormMapper.queryFormList(formName, pageNo, pageSize);
        return list == null || list.size() <= 0
                ? new ArrayList<>()
                : list.stream().map(this::toSysFormDto).collect(Collectors.toList());
    }
    private SysFormDto toSysFormDto(SysForm frm) {
        if (frm == null) {
            return null;
        }
        SysFormDto dto = new SysFormDto();
        dto.setId(frm.getId());
        dto.setFormId(frm.getId());
        dto.setFormName(frm.getFormName());
        dto.setFormType(frm.getFormType());
        dto.setFormTypeName(FormType.getNameByCode(frm.getFormType()));
        dto.setFormContent(frm.getFormContent());
        dto.setRemark(frm.getRemark());
        dto.setCreator(frm.getCreator());
        dto.setCreateTime(frm.getCreateTime() == null ? "" : DateUtil.formatDate(frm.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        dto.setModifier(frm.getModifier());
        dto.setModifyTime(frm.getModifyTime() == null ? "" : DateUtil.formatDate(frm.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
        return dto;
    }

    /**
     * 新增流程表单
     */
    @Override
    public long insertSysForm(String formName, byte formType, String formContent, String remark, String accountNo) {
        SysForm frm = new SysForm();
        frm.setId(seqSysForm.next());
        frm.setFormName(formName == null ? null : formName.trim());
        frm.setFormType(formType);
        frm.setFormContent(formContent == null ? null : formContent.trim());
        frm.setRemark(remark == null ? null : remark.trim());
        frm.setCreator(accountNo);
        frm.setCreateTime(new Date());
        sysFormMapper.insertSelective(frm);
        return frm.getId();
    }

    /**
     * 修改流程表单
     */
    @Transactional
    @Override
    public void updateSysForm(long id, String formName, byte formType, String formContent, String remark, String accountNo) {
        SysForm frm = sysFormMapper.selectOneById(id, SysForm.class);
        if (frm == null) {
            throw new RuntimeException("数据不存在[id=" + id + "]");
        }

        frm.setFormName(formName == null ? null : formName.trim());
        frm.setFormType(formType);
        frm.setFormContent(formContent == null ? null : formContent.trim());
        frm.setRemark(remark == null ? null : remark.trim());
        frm.setModifier(accountNo);
        frm.setModifyTime(new Date());
        int i = sysFormMapper.updateByPrimaryKey(frm);
        if (i < 0) {
            throw new RuntimeException("修改失败!");
        }
    }

    /**
     * 删除流程表单信息
     * 
     * @param formId 流程表单ID
     * @return 结果
     */
    @Override
    public int deleteSysFormById(Long formId) {
        return sysFormMapper.deleteByKey(formId, SysForm.class);
    }
}
