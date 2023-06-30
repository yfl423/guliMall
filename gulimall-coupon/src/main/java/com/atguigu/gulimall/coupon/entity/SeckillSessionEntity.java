package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 16:58:29
 */
@Data
@TableName("sms_seckill_session")
public class SeckillSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@TableId
	private Long id;  // sessionId
	private String name;  // session 名称
	private Date startTime; // session 开启时间
	private Date endTime; // session 结束时间
	@TableField(exist = false)
	private List<SeckillSkuRelationEntity> skuRelationEntities; // 关联的秒杀商品信息 skuId secKillPrice secKillLimitCount ...

}
