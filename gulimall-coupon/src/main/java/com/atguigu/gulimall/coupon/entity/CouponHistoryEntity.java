package com.atguigu.gulimall.coupon.entity;

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
 * @date 2021-01-18 16:58:29
 */
@Data
@TableName("sms_coupon_history")
public class CouponHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long couponId;
	/**
	 * 
	 */
	private Long memberId;
	/**
	 * 
	 */
	private String memberNickName;
	/**
	 * 
	 */
	private Integer getType;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * ʹ
	 */
	private Integer useType;
	/**
	 * ʹ
	 */
	private Date useTime;
	/**
	 * 
	 */
	private Long orderId;
	/**
	 * 
	 */
	private Long orderSn;

}
