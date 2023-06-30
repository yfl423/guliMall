package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:06
 */
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId(type = IdType.INPUT) // 告诉mybatis这个主键是我们输入的而不是自增的，否则会报异常
	private Long spuId;
	/**
	 * 
	 */
	private String decript;

}
