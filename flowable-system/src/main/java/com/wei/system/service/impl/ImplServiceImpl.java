package com.wei.system.service.impl;


import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.enums.OrderType;
import com.wei.common.utils.FlowBeanUtil;
import com.wei.common.utils.StringUtils;
import com.wei.system.domain.*;
import com.wei.system.domain.vo.BizImplVo;
import com.wei.system.domain.vo.ContractVo;
import com.wei.system.domain.vo.ImplVo;
import com.wei.system.mapper.ImplMapper;
import com.wei.system.mapper.ImplOrderMapper;
import com.wei.system.mapper.OrderMapper;
import com.wei.system.mapper.SysCustomerMapper;
import com.wei.system.service.ISysUserService;
import com.wei.system.service.ImplService;
import com.wei.system.service.OrderService;
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
@Service("implService")
public class ImplServiceImpl implements ImplService {
	@Resource
	private ImplMapper implMapper;
	@Resource
	private ImplOrderMapper implOrderMapper;
	@Resource
	private SysCustomerMapper customerMapper;

	@Resource
	private ISysUserService sysUserService;

	@Resource
	private OrderMapper orderMapper;
	@Resource
	private  OrderService orderService;

	@Resource
	private Sequence seqImpl;
	@Resource
	private Sequence seqImplOrder;


	@Transactional
	@Override
	public long createImpl(String procInsId, ImplVo implVo, long userId, String creator) {
		if (implVo == null) {
			throw new RuntimeException("实施启动单参数为空");
		}
		Impl impl = implMapper.queryImplByProcInsId(procInsId);
		if (impl != null) {
			implMapper.delete(impl);
			implOrderMapper.deleteByCriteria(Criteria.of("impl_id", Operator.eq, impl.getId()), ImplOrder.class);
		}

		SysUser sysUser = implVo.getPrjManagerId() == null ? null : sysUserService.selectUserById(implVo.getPrjManagerId());
		/**
		 * 实施启动单主体
		 */
		impl = new Impl();
		long implId = seqImpl.next();
		Date now = new Date();
		impl.setId(implId);
		impl.setProcInsId(procInsId);
		impl.setImplNo(implVo.getImplNo());
		impl.setTitle(StringUtils.trim(implVo.getTitle()));
		impl.setCustomerNo(StringUtils.trim(implVo.getCustomerNo()));
		impl.setPrjCode(StringUtils.trim(implVo.getPrjCode()));
		impl.setPreSalesId(implVo.getPreSalesId());
		impl.setPrjManagerId(implVo.getPrjManagerId());
		impl.setPrjManagerName(sysUser == null ? null : sysUser.getUserName());
		impl.setSolution(implVo.getSolution());
		impl.setCreator(creator);
		impl.setCreateTime(now);
		implMapper.insert(impl);

		/**
		 * 实施启动单_订单
		 */
		createImplOrder(implId, implVo.getOrderList(), creator);
		return implId;
	}
	private void createImplOrder(long implId, List<ImplVo.Order> orderList, String creator) {
		if (orderList == null || orderList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (ImplVo.Order orderVo : orderList) {
			createImplOrder(implId, orderVo, creator, now);
		}
	}
	private void createImplOrder(long implId, ImplVo.Order orderVo, String creator, Date createTime) {
		long implOrderId = seqImplOrder.next();
		ImplOrder iOrder = new ImplOrder();
		iOrder.setId(implOrderId);
		iOrder.setImplId(implId);
		iOrder.setOrderId(orderVo.getOrderId());
		iOrder.setCreator(creator);
		iOrder.setCreateTime(createTime);
		implOrderMapper.insert(iOrder);
	}

	@Transactional
	@Override
	public void updateImpl(String procInsId, ImplVo implVo, String modifier) {
		if (implVo == null) {
			return;
		}
		Impl impl = implMapper.queryImplByProcInsId(procInsId);
		if (impl == null) {
			return;
		}

		SysUser sysUser = implVo.getPrjManagerId() == null ? null : sysUserService.selectUserById(implVo.getPrjManagerId());
		/**
		 * 实施启动单主体
		 */
		Date now = new Date();
		impl.setTitle(StringUtils.trim(implVo.getTitle()));
		impl.setCustomerNo(StringUtils.trim(implVo.getCustomerNo()));
		impl.setPrjCode(StringUtils.trim(implVo.getPrjCode()));
		impl.setPreSalesId(implVo.getPreSalesId());
		impl.setPrjManagerId(implVo.getPrjManagerId());
		impl.setPrjManagerName(sysUser == null ? null : sysUser.getUserName());
		impl.setSolution(implVo.getSolution());
		impl.setModifier(modifier);
		impl.setModifyTime(now);
		implMapper.updateByPrimaryKey(impl);

		long implId = impl.getId();
		/**
		 * 实施启动单_订单
		 */
		updateImplOrder(implId, implVo.getOrderList(), modifier);
	}
	private void updateImplOrder(long implId, List<ImplVo.Order> orderList, String modifier) {
		List<ImplOrder> existList = implOrderMapper.queryImplOrderList(implId, null, null);
		/**
		 * 之前已经存在的记录，如果仍在待更新的记录列表中，则保留，否则删除
		 */
		List<Long> orderIdList = (orderList == null || orderList.size() <= 0)
				? new ArrayList<>()
				: orderList.stream().map(ImplVo.Order::getOrderId).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(existList)) {
			for (ImplOrder itm: existList) {
				boolean exists = FlowBeanUtil.exists(orderIdList, itm.getOrderId());
				if (!exists) {
					implOrderMapper.delete(itm);
				}
			}
		}

		/**
		 * 增加新纪录
		 */
		if (orderList != null && orderList.size() > 0) {
			Date now = new Date();
			for (ImplVo.Order orderVo : orderList) {
				ImplOrder iOrder = implOrderMapper.findImplOrder(implId, orderVo.getOrderId());
				if (iOrder != null) {
					this.updateImplOrder(iOrder, orderVo, modifier, now);
				} else {
					this.createImplOrder(implId, orderVo, modifier, now);
				}
			}
		}
	}
	private void updateImplOrder(ImplOrder iOrder, ImplVo.Order orderVo, String modifier, Date modifyTime) {
		iOrder.setOrderId(orderVo.getOrderId());
		iOrder.setModifier(modifier);
		iOrder.setModifyTime(modifyTime);
		implOrderMapper.updateByPrimaryKey(iOrder);
	}

	@Override
	public Impl getImpl(String procInsId) {
		return implMapper.queryImplByProcInsId(procInsId);
	}

	@Override
	public String getFullImplTitle(String procInsId) {
		Impl impl = implMapper.queryImplByProcInsId(procInsId);
		if (impl == null) {
			return null;
		}
		SysCustomer customer = customerMapper.getByCustomerNo(impl.getCustomerNo());
		String enterpriseName = customer == null ? impl.getCustomerNo() : customer.getEnterpriseName();
		return getFullImplTitle(impl.getTitle(), enterpriseName);
	}
	private String getFullImplTitle(String title, String enterpriseName) {
		return "实施启动单"
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
	public int queryImplCount(String customerNo, Long preSalesId, String title, String fuzzyImplNo
			, Date startTime, Date endTime) {
		return implMapper.queryImplCount(customerNo, title, fuzzyImplNo, preSalesId, null, startTime, endTime);
	}

	@Override
	public List<BizImplVo> queryImplList(String customerNo, Long preSalesId, String title, String fuzzyImplNo
			, Date startTime, Date endTime, int pageNo, int pageSize) {
		List<Impl> list = implMapper.queryImplList(customerNo, title, fuzzyImplNo, preSalesId, null
				, startTime, endTime, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toBizImplVo).collect(Collectors.toList());
	}

	public BizImplVo toBizImplVo(Impl impl) {
		if (impl == null) {
			return null;
		}

		SysCustomer customer = impl.getCustomerNo() == null ? null : customerMapper.getByCustomerNo(impl.getCustomerNo());
		String enterpriseName = customer == null ? null : customer.getEnterpriseName();
		SysUser preSalesUser = sysUserService.selectUserById(impl.getPreSalesId());
		String customerNo = customer == null ? null : customer.getCustomerNo();
		BizImplVo vo = new BizImplVo();
		vo.setId(impl.getId());
		vo.setProcInsId(impl.getProcInsId());
		vo.setImplNo(impl.getImplNo());
		vo.setTitle(impl.getTitle());
		vo.setCustomerNo(impl.getCustomerNo());
		vo.setCustomerName("[" + customerNo + "]" + enterpriseName);
		vo.setEnterpriseName(enterpriseName);
		vo.setPrjCode(impl.getPrjCode());
		vo.setPreSalesId(impl.getPreSalesId());
		vo.setPreSalesName(preSalesUser == null ? null : preSalesUser.getUserName());
		vo.setPreSalesNickName(preSalesUser == null ? null : preSalesUser.getNickName());
		vo.setPrjManagerId(impl.getPrjManagerId());
		vo.setPrjManagerName(impl.getPrjManagerName());
		vo.setSolution(impl.getSolution());
		vo.setCreator(impl.getCreator());
		vo.setCreateTime(impl.getCreateTime() == null ? "" : DateUtil.formatDate(impl.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(impl.getModifier());
		vo.setModifyTime(impl.getModifyTime() == null ? "" : DateUtil.formatDate(impl.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryImplOrderCount(long implId) {
		return implOrderMapper.queryImplOrderCount(implId);
	}

	@Override
	public List<BizImplVo.Order> queryImplOrderList(long implId, int pageNo, int pageSize) {
		List<ImplOrder> list = implOrderMapper.queryImplOrderList(implId, pageNo, pageSize);
		return list == null || list.size()<=0
				? new ArrayList<>()
				: list.stream().map(this::toBizOrderVo).collect(Collectors.toList());
	}
	public BizImplVo.Order toBizOrderVo(ImplOrder implOrder) {
		if (implOrder == null) {
			return null;
		}
		Impl impl = implMapper.selectOneById(implOrder.getImplId(), Impl.class);
		SysCustomer customer = customerMapper.getByCustomerNo(impl.getCustomerNo());
		Order order = orderMapper.selectOneById(implOrder.getOrderId(), Order.class);
		String fullTitle = order.getOrderType() == null ? null : getFullOrderTitle(order.getTitle(), order.getOrderType()
				, customer.getEnterpriseName());
		BizImplVo.Order vo = new BizImplVo.Order();
		vo.setOrderId(implOrder.getOrderId());
		vo.setTitle(fullTitle);
		vo.setCreator(order.getCreator());
		vo.setCreateTime(implOrder.getCreateTime() == null ? "" : DateUtil.formatDate(implOrder.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(implOrder.getModifier());
		vo.setModifyTime(implOrder.getModifyTime() == null ? "" : DateUtil.formatDate(implOrder.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Transactional
	@Override
	public void deleteImpl(long implId) {
		implMapper.deleteByKey(implId,Impl.class);
		implOrderMapper.deleteByCriteria(Criteria.of("impl_id", Operator.eq, implId), ImplOrder.class);
	}

	@Override
	public BizImplVo queryImplById(long implId) {
		Impl impl = implMapper.selectOneById(implId, Impl.class);
		return toBizImplVo(impl);
	}

	@Override
	public BizImplVo getImplInfo(long implId) {
		Impl impl = implMapper.selectOneById(implId, Impl.class);
		List<BizImplVo.Order> orders = queryImplOrderList(implId, 1, Integer.MAX_VALUE);
		BizImplVo bizImplVo = toBizImplVo(impl);
		bizImplVo.setOrderList(orders);
		return bizImplVo;
	}
}
