package com.wei.system.service.impl;


import com.alibaba.fastjson2.JSON;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.enums.OrderType;
import com.wei.common.utils.StringUtils;
import com.wei.system.domain.*;
import com.wei.system.domain.vo.*;
import com.wei.system.mapper.*;
import com.wei.system.service.ISysUserService;
import com.wei.system.service.OrderService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.dao.mybatis.where.Criteria;
import com.yuweix.kuafu.dao.mybatis.where.Operator;
import com.yuweix.kuafu.sequence.base.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
@Slf4j
@Service("orderService")
public class OrderServiceImpl implements OrderService {
	@Resource
	private TaskService taskService;
	@Resource
	protected HistoryService historyService;
	@Resource
	private OrderMapper orderMapper;
	@Resource
	private OrderPortMapper orderPortMapper;
	@Resource
	private OrderDeviceMapper orderDeviceMapper;
	@Resource
	private OrderMplsIpsecMapper orderMplsIpsecMapper;
	@Resource
	private OrderSpecialLineMapper orderSpecialLineMapper;
	@Resource
	private OrderInternetMapper orderInternetMapper;
	@Resource
	private OrderIdcMapper orderIdcMapper;
	@Resource
	private OrderSrvMapper orderSrvMapper;
	@Resource
	private OrderIntegrationMapper orderIntegrationMapper;
	@Resource
	private SysCustomerMapper customerMapper;

	@Resource
	private Sequence seqOrder;
	@Resource
	private Sequence seqOrderPort;
	@Resource
	private Sequence seqOrderDevice;
	@Resource
	private Sequence seqOrderMplsIpsec;
	@Resource
	private Sequence seqOrderSpecialLine;
	@Resource
	private Sequence seqOrderInternet;
	@Resource
	private Sequence seqOrderIdc;
	@Resource
	private Sequence seqOrderSrv;
	@Resource
	private Sequence seqOrderIntegration;
	@Resource
	private ISysUserService userService;


	@Transactional
	@Override
	public long createOrder(String procInsId, OrderVo orderVo, long userId, String creator) {
		if (orderVo == null) {
			throw new RuntimeException("订单参数为空");
		}
		Order order = orderMapper.queryOrderByProcInsId(procInsId);
		if (order != null) {
			orderMapper.delete(order);
			this.deleteOrderDetailByOrderId(order.getId());
		}
		/**
		 * 订单主体
		 */
		order = new Order();
		long orderId = seqOrder.next();
		Date now = new Date();
		order.setId(orderId);
		order.setProcInsId(procInsId);
		order.setOrderNo(orderVo.getOrderNo());
		order.setTitle(StringUtils.trim(orderVo.getTitle()));
		order.setOrderType(StringUtils.trim(orderVo.getOrderType()));
		order.setBizType(StringUtils.trim(orderVo.getBizType()));
		order.setRelatedOrderId(orderVo.getOrderId());
		order.setCustomerNo(StringUtils.trim(orderVo.getCustomerNo()));
		order.setCustomerContacts(StringUtils.trim(orderVo.getCustomerContacts()));
		order.setCustomerPhoneNo(StringUtils.trim(orderVo.getCustomerPhoneNo()));
		order.setCustomerEmail(StringUtils.trim(orderVo.getCustomerEmail()));
		order.setInvolveNonStandard(orderVo.getInvolveNonStandard() != null && orderVo.getInvolveNonStandard());
		order.setPreSalesId(orderVo.getPreSalesId());
		order.setInvolveOtherFee(orderVo.getInvolveOtherFee() != null && orderVo.getInvolveOtherFee());
		order.setOtherFeeDesc(StringUtils.trim(orderVo.getOtherFeeDesc()));
		order.setOtherFee(orderVo.getOtherFee());
		order.setOtherFeePayType(StringUtils.trim(orderVo.getOtherFeePayType()));
		order.setCurrencyType(StringUtils.trim(orderVo.getCurrencyType()));
		order.setServiceDuration(orderVo.getServiceDuration());
		order.setUserId(userId);
		order.setRemarks(StringUtils.trim(orderVo.getRemarks()));
		order.setCreator(creator);
		order.setCreateTime(now);
		orderMapper.insert(order);

		/**
		 * 标准产品(端口)
		 */
		createOrderPort(orderId, orderVo.getPortList(), creator);
		/**
		 * 标准产品(设备)
		 */
		createOrderDevice(orderId, orderVo.getDeviceList(), creator);
		/**
		 * 非标准产品(MPLS/IPSec)
		 */
		createOrderMplsIpsec(orderId, orderVo.getMplsIpsecList(), creator);
		/**
		 * 非标准产品(专线)
		 */
		createOrderSpecialLine(orderId, orderVo.getSpecialLineList(), creator);
		/**
		 * 非标准产品(Internet)
		 */
		createOrderInternet(orderId, orderVo.getInternetList(), creator);
		/**
		 * 非标准产品(IDC)
		 */
		createOrderIdc(orderId, orderVo.getIdcList(), creator);
		/**
		 * 非标准产品(服务)
		 */
		createOrderSrv(orderId, orderVo.getSrvList(), creator);
		/**
		 * 非标准产品(集成)
		 */
		createOrderIntegration(orderId, orderVo.getIntegrationList(), creator);
		return orderId;
	}
	private void deleteOrderDetailByOrderId(long orderId) {
		orderPortMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderPort.class);
		orderDeviceMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderDevice.class);
		orderMplsIpsecMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderMplsIpsec.class);
		orderSpecialLineMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderSpecialLine.class);
		orderInternetMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderInternet.class);
		orderIdcMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderIdc.class);
		orderSrvMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderSrv.class);
		orderIntegrationMapper.deleteByCriteria(Criteria.of("order_id", Operator.eq, orderId), OrderIntegration.class);
	}
	private void createOrderPort(long orderId, List<OrderVo.Port> portList, String creator) {
		if (portList == null || portList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Port portVo : portList) {
			createOrderPort(orderId, portVo, creator, now);
		}
	}
	private void createOrderPort(long orderId, OrderVo.Port portVo, String creator, Date createTime) {
		long orderPortId = seqOrderPort.next();
		OrderPort port = new OrderPort();
		port.setId(orderPortId);
		port.setOrderId(orderId);
		port.setPortNo(StringUtils.trim(portVo.getPortNo()));
		port.setSiteAddr(StringUtils.trim(portVo.getSiteAddr()));
		port.setBandwidth(StringUtils.trim(portVo.getBandwidth()));
		port.setPopNo(StringUtils.trim(portVo.getPopNo()));
		port.setQuantity(portVo.getQuantity());
		port.setOneTimeFee(portVo.getOneTimeFee());
		port.setOneTimeMgtFee(portVo.getOneTimeMgtFee());
		port.setPrice(portVo.getPrice());
		port.setExpDeliveryDate(portVo.getExpDeliveryDate() == null || "".equals(portVo.getExpDeliveryDate())
				? null
				: DateUtil.parseDate(portVo.getExpDeliveryDate(), "yyyy-MM-dd"));
		port.setRemarks(StringUtils.trim(portVo.getRemarks()));
		port.setCreator(creator);
		port.setCreateTime(createTime);
		orderPortMapper.insert(port);
	}
	private void createOrderDevice(long orderId, List<OrderVo.Device> deviceList, String creator) {
		if (deviceList == null || deviceList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Device deviceVo : deviceList) {
			createOrderDevice(orderId, deviceVo, creator, now);
		}
	}
	private void createOrderDevice(long orderId, OrderVo.Device deviceVo, String creator, Date createTime) {
		long orderDeviceId = seqOrderDevice.next();
		OrderDevice device = new OrderDevice();
		device.setId(orderDeviceId);
		device.setOrderId(orderId);
		device.setDeviceNo(StringUtils.trim(deviceVo.getDeviceNo()));
		device.setSiteAddr(StringUtils.trim(deviceVo.getSiteAddr()));
		device.setQuantity(deviceVo.getQuantity());
		device.setOneTimeFee(deviceVo.getOneTimeFee());
		device.setOneTimeMgtFee(deviceVo.getOneTimeMgtFee());
		device.setPrice(deviceVo.getPrice());
		device.setRemarks(StringUtils.trim(deviceVo.getRemarks()));
		device.setCreator(creator);
		device.setCreateTime(createTime);
		orderDeviceMapper.insert(device);
	}
	private void createOrderMplsIpsec(long orderId, List<OrderVo.MplsIpsec> mplsIpsecList, String creator) {
		if (mplsIpsecList == null || mplsIpsecList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.MplsIpsec mplsIpsecVo : mplsIpsecList) {
			createOrderMplsIpsec(orderId, mplsIpsecVo, creator, now);
		}
	}
	private void createOrderMplsIpsec(long orderId, OrderVo.MplsIpsec mplsIpsecVo, String creator, Date createTime) {
		long orderMplsIpsecId = seqOrderMplsIpsec.next();
		OrderMplsIpsec mplsIpsec = new OrderMplsIpsec();
		mplsIpsec.setId(orderMplsIpsecId);
		mplsIpsec.setOrderId(orderId);
		mplsIpsec.setProdNo(StringUtils.trim(mplsIpsecVo.getProdNo()));
		mplsIpsec.setIsp(StringUtils.trim(mplsIpsecVo.getIsp()));
		mplsIpsec.setAddr(StringUtils.trim(mplsIpsecVo.getAddr()));
		mplsIpsec.setPopNo(StringUtils.trim(mplsIpsecVo.getPopNo()));
		mplsIpsec.setBandwidth(StringUtils.trim(mplsIpsecVo.getBandwidth()));
		mplsIpsec.setUnitPrice(mplsIpsecVo.getUnitPrice());
		mplsIpsec.setQuantity(mplsIpsecVo.getQuantity());
		mplsIpsec.setOneTimeFee(mplsIpsecVo.getOneTimeFee());
		mplsIpsec.setOneTimeMgtFee(mplsIpsecVo.getOneTimeMgtFee());
		mplsIpsec.setDeliveryDate(mplsIpsecVo.getDeliveryDate() == null || "".equals(mplsIpsecVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(mplsIpsecVo.getDeliveryDate(), "yyyy-MM-dd"));
		mplsIpsec.setRemarks(StringUtils.trim(mplsIpsecVo.getRemarks()));
		mplsIpsec.setPurchaseRemarks(StringUtils.trim(mplsIpsecVo.getPurchaseRemarks()));
		mplsIpsec.setCreator(creator);
		mplsIpsec.setCreateTime(createTime);
		orderMplsIpsecMapper.insert(mplsIpsec);
	}
	private void createOrderSpecialLine(long orderId, List<OrderVo.SpecialLine> specialLineList, String creator) {
		if (specialLineList == null || specialLineList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.SpecialLine specialLineVo : specialLineList) {
			createOrderSpecialLine(orderId, specialLineVo, creator, now);
		}
	}
	private void createOrderSpecialLine(long orderId, OrderVo.SpecialLine specialLineVo, String creator, Date createTime) {
		long orderSpecialLineId = seqOrderSpecialLine.next();
		OrderSpecialLine specialLine = new OrderSpecialLine();
		specialLine.setId(orderSpecialLineId);
		specialLine.setOrderId(orderId);
		specialLine.setProdNo(StringUtils.trim(specialLineVo.getProdNo()));
		specialLine.setIsp(StringUtils.trim(specialLineVo.getIsp()));
		specialLine.setAAddr(StringUtils.trim(specialLineVo.getAAddr()));
		specialLine.setAPortType(StringUtils.trim(specialLineVo.getAPortType()));
		specialLine.setZAddr(StringUtils.trim(specialLineVo.getZAddr()));
		specialLine.setZPortType(StringUtils.trim(specialLineVo.getZPortType()));
		specialLine.setBandwidth(StringUtils.trim(specialLineVo.getBandwidth()));
		specialLine.setUnitPrice(specialLineVo.getUnitPrice());
		specialLine.setQuantity(specialLineVo.getQuantity());
		specialLine.setOneTimeFee(specialLineVo.getOneTimeFee());
		specialLine.setOneTimeMgtFee(specialLineVo.getOneTimeMgtFee());
		specialLine.setDeliveryDate(specialLineVo.getDeliveryDate() == null || "".equals(specialLineVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(specialLineVo.getDeliveryDate(), "yyyy-MM-dd"));
		specialLine.setRemarks(StringUtils.trim(specialLineVo.getRemarks()));
		specialLine.setCreator(creator);
		specialLine.setCreateTime(createTime);
		orderSpecialLineMapper.insert(specialLine);
	}
	private void createOrderInternet(long orderId, List<OrderVo.Internet> internetList, String creator) {
		if (internetList == null || internetList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Internet internetVo : internetList) {
			createOrderInternet(orderId, internetVo, creator, now);
		}
	}
	private void createOrderInternet(long orderId, OrderVo.Internet internetVo, String creator, Date createTime) {
		long orderInternetId = seqOrderInternet.next();
		OrderInternet internet = new OrderInternet();
		internet.setId(orderInternetId);
		internet.setOrderId(orderId);
		internet.setProdNo(StringUtils.trim(internetVo.getProdNo()));
		internet.setIsp(StringUtils.trim(internetVo.getIsp()));
		internet.setAddr(StringUtils.trim(internetVo.getAddr()));
		internet.setPortType(StringUtils.trim(internetVo.getPortType()));
		internet.setBandwidth(StringUtils.trim(internetVo.getBandwidth()));
		internet.setIpCount(internetVo.getIpCount());
		internet.setUpDownSymmetry(internetVo.getUpDownSymmetry() != null && internetVo.getUpDownSymmetry());
		internet.setQuantity(internetVo.getQuantity());
		internet.setUnitPrice(internetVo.getUnitPrice());
		internet.setOneTimeFee(internetVo.getOneTimeFee());
		internet.setOneTimeMgtFee(internetVo.getOneTimeMgtFee());
		internet.setDeliveryDate(internetVo.getDeliveryDate() == null || "".equals(internetVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(internetVo.getDeliveryDate(), "yyyy-MM-dd"));
		internet.setRemarks(StringUtils.trim(internetVo.getRemarks()));
		internet.setCreator(creator);
		internet.setCreateTime(createTime);
		orderInternetMapper.insert(internet);
	}
	private void createOrderIdc(long orderId, List<OrderVo.Idc> idcList, String creator) {
		if (idcList == null || idcList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Idc idcVo : idcList) {
			createOrderIdc(orderId, idcVo, creator, now);
		}
	}
	private void createOrderIdc(long orderId, OrderVo.Idc idcVo, String creator, Date createTime) {
		long orderIdcId = seqOrderIdc.next();
		OrderIdc idc = new OrderIdc();
		idc.setId(orderIdcId);
		idc.setOrderId(orderId);
		idc.setProdNo(StringUtils.trim(idcVo.getProdNo()));
		idc.setRoomNo(StringUtils.trim(idcVo.getRoomNo()));
		idc.setFrameNo(StringUtils.trim(idcVo.getFrameNo()));
		idc.setElectricPower(StringUtils.trim(idcVo.getElectricPower()));
		idc.setAddr(StringUtils.trim(idcVo.getAddr()));
		idc.setBandwidthType(StringUtils.trim(idcVo.getBandwidthType()));
		idc.setIpCount(idcVo.getIpCount());
		idc.setBandwidth(StringUtils.trim(idcVo.getBandwidth()));
		idc.setQuantity(idcVo.getQuantity());
		idc.setUnitPrice(idcVo.getUnitPrice());
		idc.setOneTimeFee(idcVo.getOneTimeFee());
		idc.setOneTimeMgtFee(idcVo.getOneTimeMgtFee());
		idc.setDeliveryDate(idcVo.getDeliveryDate() == null || "".equals(idcVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(idcVo.getDeliveryDate(), "yyyy-MM-dd"));
		idc.setRemarks(StringUtils.trim(idcVo.getRemarks()));
		idc.setCreator(creator);
		idc.setCreateTime(createTime);
		orderIdcMapper.insert(idc);
	}
	private void createOrderSrv(long orderId, List<OrderVo.Srv> srvList, String creator) {
		if (srvList == null || srvList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Srv srvVo : srvList) {
			createOrderSrv(orderId, srvVo, creator, now);
		}
	}
	private void createOrderSrv(long orderId, OrderVo.Srv srvVo, String creator, Date createTime) {
		long orderSrvId = seqOrderSrv.next();
		OrderSrv srv = new OrderSrv();
		srv.setId(orderSrvId);
		srv.setOrderId(orderId);
		srv.setDesc_(StringUtils.trim(srvVo.getDesc()));
		srv.setUnitPrice(srvVo.getUnitPrice());
		srv.setQuantity(srvVo.getQuantity());
		srv.setOneTimeFee(srvVo.getOneTimeFee());
		srv.setOneTimeMgtFee(srvVo.getOneTimeMgtFee());
		srv.setDeliveryDate(srvVo.getDeliveryDate() == null || "".equals(srvVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(srvVo.getDeliveryDate(), "yyyy-MM-dd"));
		srv.setRemarks(StringUtils.trim(srvVo.getRemarks()));
		srv.setCreator(creator);
		srv.setCreateTime(createTime);
		orderSrvMapper.insert(srv);
	}
	private void createOrderIntegration(long orderId, List<OrderVo.Integration> integrationList, String creator) {
		if (integrationList == null || integrationList.size() <= 0) {
			return;
		}
		Date now = new Date();
		for (OrderVo.Integration integrationVo : integrationList) {
			createOrderIntegration(orderId, integrationVo, creator, now);
		}
	}
	private void createOrderIntegration(long orderId, OrderVo.Integration integrationVo, String creator, Date createTime) {
		long orderIntegrationId = seqOrderIntegration.next();
		OrderIntegration integration = new OrderIntegration();
		integration.setId(orderIntegrationId);
		integration.setOrderId(orderId);
		integration.setDeviceModel(StringUtils.trim(integrationVo.getDeviceModel()));
		integration.setDesc_(StringUtils.trim(integrationVo.getDesc()));
		integration.setUnitPrice(integrationVo.getUnitPrice());
		integration.setQuantity(integrationVo.getQuantity());
		integration.setDeliveryDate(integrationVo.getDeliveryDate() == null || "".equals(integrationVo.getDeliveryDate())
				? null
				: DateUtil.parseDate(integrationVo.getDeliveryDate(), "yyyy-MM-dd"));
		integration.setRemarks(StringUtils.trim(integrationVo.getRemarks()));
		integration.setCreator(creator);
		integration.setCreateTime(createTime);
		orderIntegrationMapper.insert(integration);
	}

	@Transactional
	@Override
	public void updateOrder(String procInsId, OrderVo orderVo, String modifier) {
		if (orderVo == null) {
			return;
		}
		Order order = orderMapper.queryOrderByProcInsId(procInsId);
		if (order == null) {
			return;
		}
		/**
		 * 删除订单明细后重新添加
		 */
		this.deleteOrderDetailByOrderId(order.getId());

		/**
		 * 订单主体
		 */
		Date now = new Date();
		order.setTitle(StringUtils.trim(orderVo.getTitle()));
		order.setOrderType(StringUtils.trim(orderVo.getOrderType()));
		order.setBizType(StringUtils.trim(orderVo.getBizType()));
		order.setRelatedOrderId(orderVo.getOrderId());
		order.setCustomerNo(StringUtils.trim(orderVo.getCustomerNo()));
		order.setCustomerContacts(StringUtils.trim(orderVo.getCustomerContacts()));
		order.setCustomerPhoneNo(StringUtils.trim(orderVo.getCustomerPhoneNo()));
		order.setCustomerEmail(StringUtils.trim(orderVo.getCustomerEmail()));
		order.setInvolveNonStandard(orderVo.getInvolveNonStandard() != null && orderVo.getInvolveNonStandard());
		order.setPreSalesId(orderVo.getPreSalesId());
		order.setInvolveOtherFee(orderVo.getInvolveOtherFee() != null && orderVo.getInvolveOtherFee());
		order.setOtherFeeDesc(StringUtils.trim(orderVo.getOtherFeeDesc()));
		order.setOtherFee(orderVo.getOtherFee());
		order.setOtherFeePayType(StringUtils.trim(orderVo.getOtherFeePayType()));
		order.setCurrencyType(StringUtils.trim(orderVo.getCurrencyType()));
		order.setServiceDuration(orderVo.getServiceDuration());
		/*order.setSalesId(userId);*/
		order.setRemarks(StringUtils.trim(orderVo.getRemarks()));
		/*order.setCreator(creator);*/
		order.setModifier(modifier);
		order.setModifyTime(now);
		orderMapper.updateByPrimaryKey(order);

		long orderId = order.getId();
		/**
		 * 标准产品(端口)
		 */
		createOrderPort(orderId, orderVo.getPortList(), modifier);
		/**
		 * 标准产品(设备)
		 */
		createOrderDevice(orderId, orderVo.getDeviceList(), modifier);
		/**
		 * 非标准产品(MPLS/IPSec)
		 */
		createOrderMplsIpsec(orderId, orderVo.getMplsIpsecList(), modifier);
		/**
		 * 非标准产品(专线)
		 */
		createOrderSpecialLine(orderId, orderVo.getSpecialLineList(), modifier);
		/**
		 * 非标准产品(Internet)
		 */
		createOrderInternet(orderId, orderVo.getInternetList(), modifier);
		/**
		 * 非标准产品(IDC)
		 */
		createOrderIdc(orderId, orderVo.getIdcList(), modifier);
		/**
		 * 非标准产品(服务)
		 */
		createOrderSrv(orderId, orderVo.getSrvList(), modifier);
		/**
		 * 非标准产品(集成)
		 */
		createOrderIntegration(orderId, orderVo.getIntegrationList(), modifier);
	}

	@Override
	public List<OrderDropDownVo> getOrderDropDownList(String customerNo, Long userId, String keywords, int pageNo, int pageSize) {
		List<Order> list = orderMapper.queryOrderList(customerNo, userId, keywords, null
				, null, null, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderDropDownVo).collect(Collectors.toList());
	}

	@Override
	public List<OrderDropDownVo> getUnboundContractOrderListByCustomerNo(String customerNo, String title, int pageNo, int pageSize) {
		List<Order> list = orderMapper.getUnboundContractOrderListByCustomerNo(customerNo, title, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderDropDownVo).collect(Collectors.toList());
	}

	@Override
	public List<OrderDropDownVo> getOrderDropDownListByContractId(long contractId, String title, int pageNo, int pageSize) {
		List<Order> list = orderMapper.getOrderListByContractId(contractId, title, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderDropDownVo).collect(Collectors.toList());
	}

	private OrderDropDownVo toOrderDropDownVo(Order order) {
		if (order == null) {
			return null;
		}
		SysCustomer customer = customerMapper.getByCustomerNo(order.getCustomerNo());
		String enterpriseName = customer == null ? order.getCustomerNo() : customer.getEnterpriseName();
		String title0 = getFullOrderTitle(order.getTitle(), order.getOrderType(), enterpriseName);
		return OrderDropDownVo.builder()
				.orderId(order.getId())
				.title(order.getTitle())
				.fullTitle(title0)
				.build();
	}

	@Override
	public Order getOrder(String procInsId) {
		return orderMapper.queryOrderByProcInsId(procInsId);
	}

	@Override
	public String getFullOrderTitle(String procInsId) {
		Order order = orderMapper.queryOrderByProcInsId(procInsId);
		if (order == null) {
			return null;
		}
		SysCustomer customer = customerMapper.getByCustomerNo(order.getCustomerNo());
		String enterpriseName = customer == null ? order.getCustomerNo() : customer.getEnterpriseName();
		return getFullOrderTitle(order.getTitle(), order.getOrderType(), enterpriseName);
	}

	private String getFullOrderTitle(String title, String orderType, String enterpriseName) {
		return "订单"
				+ "+" + OrderType.getNameByCode(orderType)
				+ "+" + enterpriseName
				+ "+" + title;
	}

	@Override
	public int queryOrderCount(String customerNo, Long userId, String keywords, String fuzzyOrderNo
			, Date startTime, Date endTime) {
		return orderMapper.queryOrderCount(customerNo, userId, keywords, fuzzyOrderNo, startTime, endTime);
	}

	@Override
	public List<BizOrderVo> queryOrderList(String customerNo, Long userId, String keywords, String fuzzyOrderNo
			, Date startTime, Date endTime, int pageNo, int pageSize) {
		List<Order> list = orderMapper.queryOrderList(customerNo, userId, keywords, fuzzyOrderNo
				, startTime, endTime, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderVo).collect(Collectors.toList());
	}
	private BizOrderVo toOrderVo(Order o) {
		if (o == null) {
			return null;
		}

		SysCustomer customer = o.getCustomerNo() == null ? null : customerMapper.getByCustomerNo(o.getCustomerNo());
		String enterpriseName = customer == null ? null : customer.getEnterpriseName();

		Order relatedOrder = o.getRelatedOrderId() == null ? null : orderMapper.selectOneById(o.getRelatedOrderId(), Order.class);
		String relatedOrderTitle = relatedOrder == null ? null : this.getFullOrderTitle(relatedOrder.getTitle()
				, relatedOrder.getOrderType(), enterpriseName);

		SysUser preSalesUser = o.getPreSalesId() == null ? null : userService.selectUserById(o.getPreSalesId());
		SysUser user = userService.selectUserById(o.getUserId());

		BizOrderVo vo = new BizOrderVo();
		vo.setId(o.getId());
		vo.setProcInsId(o.getProcInsId());
		vo.setOrderNo(o.getOrderNo());
		vo.setTitle(o.getTitle());
		vo.setBizType(o.getBizType());
		vo.setCreator(o.getCreator());
		vo.setCurrencyType(o.getCurrencyType());
		vo.setCustomerContacts(o.getCustomerContacts());
		vo.setCustomerEmail(o.getCustomerEmail());
		vo.setOrderType(o.getOrderType());
		vo.setOrderId(o.getRelatedOrderId());
		vo.setRelatedOrderTitle(relatedOrderTitle);
		vo.setCustomerNo(o.getCustomerNo());
		vo.setCustomerPhoneNo(o.getCustomerPhoneNo());
		vo.setEnterpriseName(enterpriseName);
		vo.setInvolveNonStandard(o.isInvolveNonStandard());
		vo.setPreSalesId(o.getPreSalesId());
		vo.setPreSalesName(preSalesUser == null ? null : preSalesUser.getUserName());
		vo.setPreSalesNickName(preSalesUser == null ? null : preSalesUser.getNickName());
		vo.setInvolveOtherFee(o.isInvolveOtherFee());
		vo.setOtherFeeDesc(o.getOtherFeeDesc());
		vo.setOtherFee(o.getOtherFee());
		vo.setOtherFeePayType(o.getOtherFeePayType());
		vo.setCurrencyType(o.getCurrencyType());
		vo.setServiceDuration(o.getServiceDuration());
		vo.setUserId(o.getUserId());
		vo.setUserName(user == null ? null : user.getUserName());
		vo.setUserNickName(user == null ? null : user.getNickName());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}
	@Override
	public int queryOrderPortCount(long orderId) {
		return orderPortMapper.queryOrderPortCount(orderId);
	}
	@Override
	public List<BizOrderVo.Port> queryOrderPortList(long orderId, int pageNo, int pageSize) {
		List<OrderPort> list = orderPortMapper.queryOrderPortList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderPortVo).collect(Collectors.toList());
	}
	private BizOrderVo.Port toOrderPortVo(OrderPort o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Port vo = new BizOrderVo.Port();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setPortNo(o.getPortNo());
		vo.setSiteAddr(o.getSiteAddr());
		vo.setBandwidth(o.getBandwidth());
		vo.setPopNo(o.getPopNo());
		vo.setQuantity(o.getQuantity());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setPrice(o.getPrice());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setExpDeliveryDate(o.getExpDeliveryDate() == null ? "" : DateUtil.formatDate(o.getExpDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}
	@Override
	public int queryOrderDeviceCount(long orderId) {
		return orderDeviceMapper.queryOrderDeviceCount(orderId);
	}

	@Override
	public List<BizOrderVo.Device> queryOrderDeviceList(long orderId, int pageNo, int pageSize) {
		List<OrderDevice> list = orderDeviceMapper.queryOrderDeviceList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderDeviceVo).collect(Collectors.toList());
	}
	private BizOrderVo.Device toOrderDeviceVo(OrderDevice o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Device vo = new BizOrderVo.Device();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setDeviceNo(o.getDeviceNo());
		vo.setSiteAddr(o.getSiteAddr());
		vo.setQuantity(o.getQuantity());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setPrice(o.getPrice());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}
	@Override
	public int queryOrderMplsIpsecCount(long orderId) {
		return orderMplsIpsecMapper.queryOrderMplsIpsecCount(orderId);
	}

	@Override
	public List<BizOrderVo.MplsIpsec> queryOrderMplsIpsecList(long orderId, int pageNo, int pageSize) {
		List<OrderMplsIpsec> list = orderMplsIpsecMapper.queryOrderMplsIpsecList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderMplsIpsecVo).collect(Collectors.toList());
	}
	private BizOrderVo.MplsIpsec toOrderMplsIpsecVo(OrderMplsIpsec o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.MplsIpsec vo = new BizOrderVo.MplsIpsec();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setProdNo(o.getProdNo());
		vo.setIsp(o.getIsp());
		vo.setAddr(o.getAddr());
		vo.setPopNo(o.getPopNo());
		vo.setBandwidth(o.getBandwidth());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setQuantity(o.getQuantity());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setRemarks(o.getRemarks());
		vo.setPurchaseRemarks(o.getPurchaseRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryOrderSpecialLineCount(long orderId) {
		return orderSpecialLineMapper.queryOrderSpecialLineCount(orderId);
	}
	@Override
	public List<BizOrderVo.SpecialLine> queryOrderSpecialLineList(long orderId, int pageNo, int pageSize) {
		List<OrderSpecialLine> list = orderSpecialLineMapper.queryOrderSpecialLineList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderSpecialLineVo).collect(Collectors.toList());
	}
	private BizOrderVo.SpecialLine toOrderSpecialLineVo(OrderSpecialLine o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.SpecialLine vo = new BizOrderVo.SpecialLine();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setProdNo(o.getProdNo());
		vo.setIsp(o.getIsp());
		vo.setAAddr(o.getAAddr());
		vo.setZPortType(o.getZPortType());
		vo.setBandwidth(o.getBandwidth());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setQuantity(o.getQuantity());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryOrderInternetCount(long orderId) {
		return orderInternetMapper.queryOrderInternetCount(orderId);
	}
	@Override
	public List<BizOrderVo.Internet> queryOrderInternetList(long orderId, int pageNo, int pageSize) {
		List<OrderInternet> list = orderInternetMapper.queryOrderInternetList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderInternetVo).collect(Collectors.toList());
	}
	private BizOrderVo.Internet toOrderInternetVo(OrderInternet o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Internet vo = new BizOrderVo.Internet();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setProdNo(o.getProdNo());
		vo.setIsp(o.getIsp());
		vo.setAddr(o.getAddr());
		vo.setPortType(o.getPortType());
		vo.setBandwidth(o.getBandwidth());
		vo.setIpCount(o.getIpCount());
		vo.setUpDownSymmetry(o.getUpDownSymmetry());
		vo.setQuantity(o.getQuantity());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryOrderIdcCount(long orderId) {
		return orderIdcMapper.queryOrderIdcCount(orderId);
	}

	@Override
	public List<BizOrderVo.Idc> queryOrderIdcList(long orderId, int pageNo, int pageSize) {
		List<OrderIdc> list = orderIdcMapper.queryOrderIdcList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderIdcVo).collect(Collectors.toList());
	}
	private BizOrderVo.Idc toOrderIdcVo(OrderIdc o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Idc vo = new BizOrderVo.Idc();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setProdNo(o.getProdNo());
		vo.setRoomNo(o.getRoomNo());
		vo.setFrameNo(o.getFrameNo());
		vo.setElectricPower(o.getElectricPower());
		vo.setAddr(o.getAddr());
		vo.setBandwidthType(o.getBandwidthType());
		vo.setIpCount(o.getIpCount());
		vo.setBandwidth(o.getBandwidth());
		vo.setQuantity(o.getQuantity());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryOrderSrvCount(long orderId) {
		return orderSrvMapper.queryOrderSrvCount(orderId);
	}

	@Override
	public List<BizOrderVo.Srv> queryOrderSrvList(long orderId, int pageNo, int pageSize) {
		List<OrderSrv> list = orderSrvMapper.queryOrderSrvList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderSrvVo).collect(Collectors.toList());
	}
	private BizOrderVo.Srv toOrderSrvVo(OrderSrv o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Srv vo = new BizOrderVo.Srv();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setDesc_(o.getDesc_());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setQuantity(o.getQuantity());
		vo.setOneTimeFee(o.getOneTimeFee());
		vo.setOneTimeMgtFee(o.getOneTimeMgtFee());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public int queryOrderIntegrationCount(long orderId) {
		return orderIntegrationMapper.queryOrderIntegrationCount(orderId);
	}

	@Override
	public List<BizOrderVo.Integration> queryOrderIntegrationList(long orderId, int pageNo, int pageSize) {
		List<OrderIntegration> list = orderIntegrationMapper.queryOrderIntegrationList(orderId, pageNo, pageSize);
		return list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toOrderIntegrationVo).collect(Collectors.toList());
	}
	private BizOrderVo.Integration toOrderIntegrationVo(OrderIntegration o) {
		if (o == null) {
			return null;
		}
		BizOrderVo.Integration vo = new BizOrderVo.Integration();
		vo.setId(o.getId());
		vo.setOrderId(o.getOrderId());
		vo.setDeviceModel(o.getDeviceModel());
		vo.setDesc_(o.getDesc_());
		vo.setUnitPrice(o.getUnitPrice());
		vo.setQuantity(o.getQuantity());
		vo.setRemarks(o.getRemarks());
		vo.setCreator(o.getCreator());
		vo.setDeliveryDate(o.getDeliveryDate() == null ? "" : DateUtil.formatDate(o.getDeliveryDate(), "yyyy-MM-dd"));
		vo.setCreateTime(o.getCreateTime() == null ? "" : DateUtil.formatDate(o.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(o.getModifier());
		vo.setModifyTime(o.getModifyTime() == null ? "" : DateUtil.formatDate(o.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return vo;
	}

	@Override
	public void deleteOrder(long orderId) {
		orderMapper.deleteByKey(orderId, Order.class);
		this.deleteOrderDetailByOrderId(orderId);
	}

	private Object getVariableFromProcess(String procInsId, String key) {
		HistoricProcessInstance hisProcIns = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId)
				.includeProcessVariables().singleResult();
		if (Objects.isNull(hisProcIns)) {
			return null;
		}

		Map<String, Object> variables = hisProcIns.getProcessVariables();
		return variables == null ? null : variables.get(key);
	}

	@Override
	public BizOrderVo getOrderInfo(long orderId) {
		Order order = orderMapper.selectOneById(orderId, Order.class);
		Object types = getVariableFromProcess(order.getProcInsId(), "prdTypeList");
		List<String> prdTypeList = null;
		try {
			prdTypeList = types == null ? null : JSON.parseArray(JSON.toJSONString(types), String.class);
		} catch (Exception ignored) {}


		BizOrderVo vo = new BizOrderVo();
		SysCustomer customer = order.getCustomerNo() == null ? null : customerMapper.getByCustomerNo(order.getCustomerNo());
		String enterpriseName = customer == null ? null : customer.getEnterpriseName();
		String customerNo = order.getCustomerNo();
		Order relatedOrder = order.getRelatedOrderId() == null ? null : orderMapper.selectOneById(order.getRelatedOrderId(), Order.class);
		String relatedOrderTitle = relatedOrder == null ? null : this.getFullOrderTitle(relatedOrder.getTitle()
				, relatedOrder.getOrderType(), enterpriseName);

		SysUser preSalesUser = order.getPreSalesId() == null ? null : userService.selectUserById(order.getPreSalesId());
		SysUser user = userService.selectUserById(vo.getUserId());
        vo.setId(order.getId());
		vo.setProcInsId(order.getProcInsId());
		vo.setOrderNo(order.getOrderNo());
		vo.setTitle(order.getTitle());
		vo.setOrderType(order.getOrderType());
		vo.setBizType(order.getBizType());
		vo.setCreator(order.getCreator());
		vo.setOrderId(order.getRelatedOrderId());
		vo.setRelatedOrderTitle(relatedOrderTitle);
		vo.setCustomerNo(order.getCustomerNo());
		vo.setCustomerName("[" + customerNo + "]" + enterpriseName);
		vo.setCustomerContacts(order.getCustomerContacts());
		vo.setCustomerPhoneNo(order.getCustomerPhoneNo());
		vo.setCustomerEmail(order.getCustomerEmail());
		vo.setEnterpriseName(enterpriseName);
		vo.setInvolveNonStandard(order.isInvolveNonStandard());
		vo.setPreSalesId(order.getPreSalesId());
		vo.setPreSalesName(preSalesUser == null ? null : preSalesUser.getUserName());
		vo.setPreSalesNickName(preSalesUser == null ? null : preSalesUser.getNickName());
		vo.setInvolveOtherFee(order.isInvolveOtherFee());
		vo.setOtherFeeDesc(order.getOtherFeeDesc());
		vo.setOtherFee(order.getOtherFee());
		vo.setOtherFeePayType(order.getOtherFeePayType());
		vo.setCurrencyType(order.getCurrencyType());
		vo.setServiceDuration(order.getServiceDuration());
		vo.setUserId(order.getUserId());
		vo.setUserName(user == null ? null : user.getUserName());
		vo.setUserNickName(user == null ? null : user.getNickName());
		vo.setRemarks(order.getRemarks());
		vo.setPrdTypeList(prdTypeList);
		vo.setCreateTime(order.getCreateTime() == null ? "" : DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		vo.setModifier(order.getModifier());
		vo.setModifyTime(order.getModifyTime() == null ? "" : DateUtil.formatDate(order.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));

		List<BizOrderVo.Port> ports = queryOrderPortList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.Device> devices = queryOrderDeviceList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.Idc> idcs = queryOrderIdcList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.Integration> integrations = queryOrderIntegrationList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.Internet> internets = queryOrderInternetList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.MplsIpsec> mplsIpsecs = queryOrderMplsIpsecList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.Srv> srvs = queryOrderSrvList(orderId, 1, Integer.MAX_VALUE);
		List<BizOrderVo.SpecialLine> specialLines = queryOrderSpecialLineList(orderId, 1, Integer.MAX_VALUE);
		vo.setPortList(ports);
		vo.setDeviceList(devices);
		vo.setMplsIpsecList(mplsIpsecs);
		vo.setSpecialLineList(specialLines);
		vo.setInternetList(internets);
		vo.setIdcList(idcs);
		vo.setSrvList(srvs);
		vo.setIntegrationList(integrations);
		return vo;
	}
}
