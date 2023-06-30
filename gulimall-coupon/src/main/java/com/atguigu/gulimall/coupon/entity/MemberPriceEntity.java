package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
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
@TableName("sms_member_price")
public class MemberPriceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * 
	 */
	private Long memberLevelId;
	/**
	 * 
	 */
	private String memberLevelName;
	/**
	 * 
	 */
	private BigDecimal memberPrice;
	/**
	 * 
	 */
	private Integer addOther;

}
