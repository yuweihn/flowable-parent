package com.wei.system.domain;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @author yuwei
 */
@Table(name = "t_order_integration")
@Data
public class OrderIntegration implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(generator = "hehe")
	private long id;

	@Column(name = "order_id")
	private long orderId;

	@Column(name = "device_model")
	private String deviceModel;

	@Column(name = "desc_")
	private String desc_;

	@Column(name = "unit_price")
	private Double unitPrice;

	@Column(name = "quantity")
	private Integer quantity;

	@Column(name = "delivery_date")
	private Date deliveryDate;

	@Column(name = "remarks")
	private String remarks;


	@Version
	@Column(name = "version")
	private int version;

	@Column(name = "creator")
	private String creator;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "modifier")
	private String modifier;

	@Column(name = "modify_time")
	private Date modifyTime;
}
