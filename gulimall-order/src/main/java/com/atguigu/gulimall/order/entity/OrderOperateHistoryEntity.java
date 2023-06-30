package com.atguigu.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 18:06:42
 */
@Data
@TableName("oms_order_operate_history")
public class OrderOperateHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * id
	 */
	private Long orderId;
	/**
	 * []
	 */
	private String operateMan;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 0->1->2->3->4->5->
	 */
	private Integer orderStatus;
	/**
	 * 
	 */
	private String note;

}
