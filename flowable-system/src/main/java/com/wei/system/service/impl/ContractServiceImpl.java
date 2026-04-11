package com.wei.system.service.impl;


import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.enums.AgsCompany;
import com.wei.common.enums.ContractAttachType;
import com.wei.common.enums.ContractType;
import com.wei.common.enums.OrderType;
import com.wei.common.utils.FlowBeanUtil;
import com.wei.common.utils.StringUtils;
import com.wei.system.domain.*;
import com.wei.system.domain.vo.*;
import com.wei.system.mapper.*;
import com.wei.system.service.ContractService;
import com.wei.system.service.ISysUserService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.dao.mybatis.where.Criteria;
import com.yuweix.kuafu.dao.mybatis.where.Operator;
import com.yuweix.kuafu.sequence.base.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
@Slf4j
@Service("contractService")
public class ContractServiceImpl implements ContractService {
	@Resource
	private ContractMapper contractMapper;
	@Resource
	private ContractOrderMapper contractOrderMapper;
	@Resource
	private ContractAttachMapper contractAttachMapper;
	@Resource
	private SysCustomerMapper customerMapper;
	@Resource
	private OrderMapper orderMapper;
	@Resource
	private ISysUserService userService;

	@Resource
	private Sequence seqContract;
	@Resource
	private Sequence seqContractOrder;
	@Resource
	private Sequence seqContractAttach;


	@Transactional
	@Override
	public long createContract(String procInsId, ContractVo contractVo, long userId, String creator) {
		if (contractVo == null) {
			throw new RuntimeException("合同参数为空");
		}
		Contract contract = contractMapper.queryContractByProcInsId(procInsId);
		if (contract != null) {
			contractMapper.delete(contract);
			contractOrderMapper.deleteByCriteria(Criteria.of("contract_id", Operator.eq, contract.getId()), ContractOrder.class);
			contractAttachMapper.deleteByCriteria(Criteria.of("contract_id", Operator.eq, contract.getId()), ContractAttach.class);
		}
		/**
		 * 合同主体
		 */
		contract = new Contract();
		long contractId = seqContract.next();
		Date now = new Date();
		contract.setId(contractId);
		contract.setProcInsId(procInsId);
		contract.setContractNo(contractVo.getContractNo());
		contract.setTitle(StringUtils.trim(contractVo.getTitle()));
		contract.setContractType(StringUtils.trim(contractVo.getContractType()));
		contract.setCustomerNo(StringUtils.trim(contractVo.getCustomerNo()));
		contract.setCustomerContacts(StringUtils.trim(contractVo.getCustomerContacts()));
		contract.setCustomerPhoneNo(StringUtils.trim(contractVo.getCustomerPhoneNo()));
		contract.setCustomerEmail(StringUtils.trim(contractVo.getCustomerEmail()));
		contract.setAgsCompanyCode(contractVo.getAgsCompanyCode());
		contract.setAuthSigner(StringUtils.trim(contractVo.getAuthSigner()));
		contract.setAgsPhoneNo(StringUtils.trim(contractVo.getAgsPhoneNo()));
		contract.setSignDate(contractVo.getSignDate() == null || "".equals(contractVo.getSignDate())
				? null
				: DateUtil.parseDate(contractVo.getSignDate(), "yyyy-MM-dd"));
		contract.setContractStartDate(contractVo.getContractStartDate() == null || "".equals(contractVo.getContractStartDate())
				? null
				: DateUtil.parseDate(contractVo.getContractStartDate(), "yyyy-MM-dd"));
		contract.setContractEndDate(contractVo.getContractEndDate() == null || "".equals(contractVo.getContractEndDate())
				? null
				: DateUtil.parseDate(contractVo.getContractEndDate(), "yyyy-MM-dd"));
		contract.setRemarks(StringUtils.trim(contractVo.getRemarks()));
		contract.setFpTitle(StringUtils.trim(contractVo.getFpTitle()));
		contract.setFpBankName(StringUtils.trim(contractVo.getFpBankName()));
		contract.setFpTaxNo(StringUtils.trim(contractVo.getFpTaxNo()));
		contract.setFpBankAccNo(StringUtils.trim(contractVo.getFpBankAccNo()));
		contract.setFpAddr(StringUtils.trim(contractVo.getFpAddr()));
		contract.setFpPhoneNo(StringUtils.trim(contractVo.getFpPhoneNo()));
		contract.setSuppleAgreement(StringUtils.trim(contractVo.getSuppleAgreement()));
		contract.setUserId(userId);
		contract.setCreator(creator);
		contract.setCreateTime(now);
		contractMapper.insert(contract);

		/**
		 * 合同订单
		 */
		createContractOrder(contractId, contractVo.getOrderList(), creator);
		/**
		 * 合同附件
		 */
		createContractAttach(contractId, contractVo.getContractAttaches(), contractVo.getOrderAttaches(), creator);
		return contractId;
	}
	private void createContractOrder(long contractId, List<ContractVo.Order> orderList, String creator) {
		if (orderList == null || orderList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (ContractVo.Order orderVo : orderList) {
			createContractOrder(contractId, orderVo, creator, now);
		}
	}
	private void createContractOrder(long contractId, ContractVo.Order orderVo, String creator, Date createTime) {
		long contractOrderId = seqContractOrder.next();
		ContractOrder cOrder = new ContractOrder();
		cOrder.setId(contractOrderId);
		cOrder.setContractId(contractId);
		cOrder.setOrderId(orderVo.getOrderId());
		cOrder.setCreator(creator);
		cOrder.setCreateTime(createTime);
		contractOrderMapper.insert(cOrder);
	}
	private void createContractAttach(long contractId, List<ContractVo.Attach> contractAttaches
			, List<ContractVo.Attach> orderAttaches, String creator) {
		Date now = new Date();
		if (contractAttaches != null && contractAttaches.size() > 0) {
			for (ContractVo.Attach attach : contractAttaches) {
				createContractAttach(contractId, attach, ContractAttachType.CONTRACT.getCode(), creator, now);
			}
		}
		if (orderAttaches != null && orderAttaches.size() > 0) {
			for (ContractVo.Attach attach : orderAttaches) {
				createContractAttach(contractId, attach, ContractAttachType.ORDER.getCode(), creator, now);
			}
		}
	}
	private void createContractAttach(long contractId, ContractVo.Attach attach, byte attachType, String creator, Date createTime) {
		long contractAttachId = seqContractAttach.next();
		ContractAttach cAttach= new ContractAttach();
		cAttach.setId(contractAttachId);
		cAttach.setContractId(contractId);
		cAttach.setAttachUrl(attach.getUrl().trim());
		cAttach.setFileName(attach.getFileName() == null ? null : attach.getFileName().trim());
		cAttach.setType_(attachType);
		cAttach.setCreator(creator);
		cAttach.setCreateTime(createTime);
		contractAttachMapper.insert(cAttach);
	}

	@Transactional
	@Override
	public void updateContract(String procInsId, ContractVo contractVo, String modifier) {
		if (contractVo == null) {
			return;
		}
		Contract contract = contractMapper.queryContractByProcInsId(procInsId);
		if (contract == null) {
			return;
		}
		/**
		 * 合同主体
		 */
		Date now = new Date();
		contract.setTitle(StringUtils.trim(contractVo.getTitle()));
		contract.setContractType(StringUtils.trim(contractVo.getContractType()));
		contract.setCustomerNo(StringUtils.trim(contractVo.getCustomerNo()));
		contract.setCustomerContacts(StringUtils.trim(contractVo.getCustomerContacts()));
		contract.setCustomerPhoneNo(StringUtils.trim(contractVo.getCustomerPhoneNo()));
		contract.setCustomerEmail(StringUtils.trim(contractVo.getCustomerEmail()));
		contract.setAgsCompanyCode(contractVo.getAgsCompanyCode());
		contract.setAuthSigner(StringUtils.trim(contractVo.getAuthSigner()));
		contract.setAgsPhoneNo(StringUtils.trim(contractVo.getAgsPhoneNo()));
		contract.setSignDate(contractVo.getSignDate() == null || "".equals(contractVo.getSignDate())
				? null
				: DateUtil.parseDate(contractVo.getSignDate(), "yyyy-MM-dd"));
		contract.setContractStartDate(contractVo.getContractStartDate() == null || "".equals(contractVo.getContractStartDate())
				? null
				: DateUtil.parseDate(contractVo.getContractStartDate(), "yyyy-MM-dd"));
		contract.setContractEndDate(contractVo.getContractEndDate() == null || "".equals(contractVo.getContractEndDate())
				? null
				: DateUtil.parseDate(contractVo.getContractEndDate(), "yyyy-MM-dd"));
		contract.setRemarks(StringUtils.trim(contractVo.getRemarks()));
		contract.setFpTitle(StringUtils.trim(contractVo.getFpTitle()));
		contract.setFpBankName(StringUtils.trim(contractVo.getFpBankName()));
		contract.setFpTaxNo(StringUtils.trim(contractVo.getFpTaxNo()));
		contract.setFpBankAccNo(StringUtils.trim(contractVo.getFpBankAccNo()));
		contract.setFpAddr(StringUtils.trim(contractVo.getFpAddr()));
		contract.setFpPhoneNo(StringUtils.trim(contractVo.getFpPhoneNo()));
		contract.setSuppleAgreement(StringUtils.trim(contractVo.getSuppleAgreement()));
		contract.setModifier(modifier);
		contract.setModifyTime(now);
		contractMapper.updateByPrimaryKey(contract);

		long contractId = contract.getId();
		/**
		 * 合同订单
		 */
		updateContractOrder(contractId, contractVo.getOrderList(), modifier);
		/**
		 * 合同附件
		 */
		updateContractAttach(contractId, contractVo.getContractAttaches(), contractVo.getOrderAttaches(), modifier);
	}
	private void updateContractOrder(long contractId, List<ContractVo.Order> orderList, String modifier) {
		List<ContractOrder> existList = contractOrderMapper.queryContractOrderList(contractId, null, null);
		/**
		 * 之前已经存在的记录，如果仍在待更新的记录列表中，则保留，否则删除
		 */
		List<Long> orderIdList = (orderList == null || orderList.size() <= 0)
				? new ArrayList<>()
				: orderList.stream().map(ContractVo.Order::getOrderId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(existList)) {
			for (ContractOrder itm: existList) {
				boolean exists = FlowBeanUtil.exists(orderIdList, itm.getOrderId());
				if (!exists) {
					contractOrderMapper.delete(itm);
				}
			}
		}

		/**
		 * 增加新纪录
		 */
		if (orderList != null && orderList.size() > 0) {
			Date now = new Date();
			for (ContractVo.Order orderVo : orderList) {
				ContractOrder cOrder = contractOrderMapper.findContractOrderByOrderId(orderVo.getOrderId());
				if (cOrder == null) {
					this.createContractOrder(contractId, orderVo, modifier, now);
				}
			}
		}
	}
	private void updateContractAttach(long contractId, List<ContractVo.Attach> contractAttaches
			, List<ContractVo.Attach> orderAttaches, String modifier) {
		updateContractAttach(contractId, contractAttaches, ContractAttachType.CONTRACT.getCode(), modifier);
		updateContractAttach(contractId, orderAttaches, ContractAttachType.ORDER.getCode(), modifier);
	}
	private void updateContractAttach(long contractId, List<ContractVo.Attach> attaches, byte type, String modifier) {
		List<ContractAttach> existList = contractAttachMapper.queryContractAttachList(contractId, null, type
				, null, null);
		/**
		 * 之前已经存在的记录，如果仍在待更新的记录列表中，则保留，否则删除
		 */
		List<String> urlList = (attaches == null || attaches.size() <= 0)
				? new ArrayList<>()
				: attaches.stream().map(ContractVo.Attach::getUrl).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(existList)) {
			for (ContractAttach itm: existList) {
				boolean exists = FlowBeanUtil.exists(urlList, itm.getAttachUrl());
				if (!exists) {
					contractAttachMapper.delete(itm);
				}
			}
		}

		/**
		 * 增加新纪录
		 */
		if (attaches != null && attaches.size() > 0) {
			Date now = new Date();
			for (ContractVo.Attach attach : attaches) {
				int cnt = contractAttachMapper.queryContractAttachCount(contractId, attach.getUrl(), type);
				if (cnt <= 0) {
					this.createContractAttach(contractId, attach, type, modifier, now);
				}
			}
		}
	}

	@Override
	public List<ContractDropDownVo> getContractDropDownList(String customerNo, Long userId, String title, int pageNo, int pageSize) {
		List<Contract> list = contractMapper.queryContractList(customerNo, userId, title, null
				, null, null, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(contract -> {
			SysCustomer customer = customerMapper.getByCustomerNo(contract.getCustomerNo());
			String enterpriseName = customer == null ? contract.getCustomerNo() : customer.getEnterpriseName();
			String title0 = getFullContractTitle(contract.getTitle(), contract.getContractType(), enterpriseName);
			return ContractDropDownVo.builder()
					.contractId(contract.getId())
					.title(title0)
					.build();
		}).collect(Collectors.toList());
	}

	@Override
	public Contract getContract(String procInsId) {
		return contractMapper.queryContractByProcInsId(procInsId);
	}

	@Override
	public String getFullContractTitle(String procInsId) {
		Contract contract = contractMapper.queryContractByProcInsId(procInsId);
		if (contract == null) {
			return null;
		}
		SysCustomer customer = customerMapper.getByCustomerNo(contract.getCustomerNo());
		String enterpriseName = customer == null ? contract.getCustomerNo() : customer.getEnterpriseName();
		return getFullContractTitle(contract.getTitle(), contract.getContractType(), enterpriseName);
	}

	private String getFullContractTitle(String title, String contractType, String enterpriseName) {
		return "合同"
				+ "+" + ContractType.getNameByCode(contractType)
				+ "+" + enterpriseName
				+ "+" + title;
	}
	private String getFullOrderTitle(String title, String orderType, String enterpriseName) {
		return "订单"
				+ "+" + OrderType.getNameByCode(orderType)
				+ "+" + enterpriseName
				+ "+" + title;
	}
	@Override
	public int queryContractCount(String customerNo, Long userId, String title, String fuzzyContractNo
			, Date startTime, Date endTime) {
		return contractMapper.queryContractCount(customerNo, userId, title, fuzzyContractNo, startTime, endTime);
	}

	@Override
	public List<BizContractVo> queryContractList(String customerNo, Long userId, String title, String fuzzyContractNo
			, Date startTime, Date endTime, int pageNo, int pageSize) {
		List<Contract> list = contractMapper.queryContractList(customerNo, userId, title, fuzzyContractNo
				, startTime, endTime, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toContract).collect(Collectors.toList());
	}
	private BizContractVo toContract(Contract contract) {
		if (contract == null) {
			return null;
		}

		SysCustomer customer = contract.getCustomerNo() == null ? null : customerMapper.getByCustomerNo(contract.getCustomerNo());
		String enterpriseName = customer == null ? null : customer.getEnterpriseName();
		String customerNo = customer.getCustomerNo();
		SysUser user = userService.selectUserById(contract.getUserId());
		BizContractVo vo = new BizContractVo();
		vo.setId(contract.getId());
		vo.setProcInsId(contract.getProcInsId());
		vo.setContractNo(contract.getContractNo());
		vo.setTitle(contract.getTitle());
		vo.setContractType(contract.getContractType());
		vo.setCustomerNo(contract.getCustomerNo());
		vo.setCustomerName("[" + customerNo + "]" + enterpriseName);
		vo.setCustomerContacts(contract.getCustomerContacts());
		vo.setCustomerPhoneNo(contract.getCustomerPhoneNo());
		vo.setCustomerEmail(contract.getCustomerEmail());
		vo.setEnterpriseName(enterpriseName);
		vo.setAgsCompanyCode(contract.getAgsCompanyCode());
		vo.setAgsCompanyName(AgsCompany.getNameByCode(contract.getAgsCompanyCode()));
		vo.setAuthSigner(contract.getAuthSigner());
		vo.setAgsPhoneNo(contract.getAgsPhoneNo());
		vo.setSignDate(contract.getSignDate() == null ? "" : DateUtil.formatDate(contract.getSignDate(), "yyyy-MM-dd HH:mm:ss"));
		vo.setContractStartDate(contract.getContractStartDate() == null ? "" : DateUtil.formatDate(contract.getContractStartDate(), "yyyy-MM-dd HH:mm:ss"));
		vo.setContractEndDate(contract.getContractEndDate() == null ? "" : DateUtil.formatDate(contract.getContractEndDate(), "yyyy-MM-dd HH:mm:ss"));
		vo.setRemarks(contract.getRemarks());
		vo.setFpTaxNo(contract.getFpTaxNo());
		vo.setFpTitle(contract.getFpTitle());
		vo.setFpBankName(contract.getFpBankName());
		vo.setFpBankAccNo(contract.getFpBankAccNo());
		vo.setFpAddr(contract.getFpAddr());
		vo.setFpPhoneNo(contract.getFpPhoneNo());
		vo.setSuppleAgreement(contract.getSuppleAgreement());
		vo.setUserId(contract.getUserId());
		vo.setUserName(user == null ? null : user.getUserName());
		vo.setUserNickName(user == null ? null : user.getNickName());
		vo.setCreator(contract.getCreator());
		vo.setCreateTime(contract.getCreateTime() == null ? "" : DateUtil.formatDate(contract.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(contract.getModifier());
		vo.setModifyTime(contract.getModifyTime() == null ? "" : DateUtil.formatDate(contract.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public void deleteContract(long contractId) {
		contractMapper.deleteByKey(contractId, Contract.class);
		contractAttachMapper.deleteByCriteria(Criteria.of("contract_id", Operator.eq, contractId), ContractAttach.class);
		contractOrderMapper.deleteByCriteria(Criteria.of("contract_id", Operator.eq, contractId), ContractOrder.class);
	}

	@Override
	public int queryContractAttachCount(long contractId) {
		return contractAttachMapper.queryContractAttachCount(contractId, null, null);
	}

	@Override
	public List<BizContractVo.Attach> queryContractAttachList(long contractId, int pageNo, int pageSize) {
		List<ContractAttach> list = contractAttachMapper.queryContractAttachList(contractId, null, null, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toContractAttachVo).collect(Collectors.toList());
	}
	private BizContractVo.Attach toContractAttachVo(ContractAttach contract) {
		if (contract == null) {
			return null;
		}
		BizContractVo.Attach vo = new BizContractVo.Attach();
		vo.setId(contract.getId());
		vo.setAttachUrl(contract.getAttachUrl());
		vo.setFileName(contract.getFileName());
		vo.setContractId(contract.getContractId());
		vo.setType(contract.getType_());
		vo.setTypeName(ContractAttachType.getNameByCode(vo.getType()));
		vo.setCreator(contract.getCreator());
		vo.setCreateTime(contract.getCreateTime() == null ? "" : DateUtil.formatDate(contract.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(contract.getModifier());
		vo.setModifyTime(contract.getModifyTime() == null ? "" : DateUtil.formatDate(contract.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}
	@Override
	public int queryContractOrderCount(long contractId) {
		return contractOrderMapper.queryContractOrderCount(contractId);
	}

	@Override
	public List<BizContractVo.Order> queryContractOrderList(long contractId, int pageNo, int pageSize) {
		List<ContractOrder> list = contractOrderMapper.queryContractOrderList(contractId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toContractOrderVo).collect(Collectors.toList());
	}

	@Override
	public BizContractVo findContractById(long contractId) {
		Contract contract = contractMapper.selectOneById(contractId, Contract.class);
		return toContract(contract);
	}

	private BizContractVo.Order toContractOrderVo(ContractOrder contract) {
		if (contract == null) {
			return null;
		}

		Contract one = contractMapper.selectOneById(contract.getContractId(), Contract.class);
		SysCustomer customer = customerMapper.getByCustomerNo(one.getCustomerNo());
		Order order = orderMapper.selectOneById(contract.getOrderId(), Order.class);
		String fullTitle = order.getOrderType()== null ? null : getFullOrderTitle(order.getTitle(), order.getOrderType()
				, customer.getEnterpriseName());
		BizContractVo.Order vo = new BizContractVo.Order();
		vo.setTitle(fullTitle);
		vo.setRelationTitle(order.getTitle());
		vo.setCreator(order.getCreator());
		vo.setCreateTime(contract.getCreateTime() == null ? "" : DateUtil.formatDate(contract.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(contract.getModifier());
		vo.setModifyTime(contract.getModifyTime() == null ? "" : DateUtil.formatDate(contract.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public BizContractVo getContractInfo(long contractId) {
		Contract contract = contractMapper.selectOneById(contractId, Contract.class);
		List<BizContractVo.Attach> attaches = queryContractAttachList(contractId, 1, Integer.MAX_VALUE);
		List<BizContractVo.Order> orders = queryContractOrderList(contractId, 1, Integer.MAX_VALUE);
		BizContractVo bizContractVo = toContract(contract);
		bizContractVo.setAttachList(attaches);
		bizContractVo.setOrderList(orders);

        return bizContractVo;
	}
}
