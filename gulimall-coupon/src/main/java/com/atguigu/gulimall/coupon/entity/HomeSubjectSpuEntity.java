package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ר
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 16:58:29
 */
@Data
@TableName("sms_home_subject_spu")
public class HomeSubjectSpuEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * ר
	 */
	private String name;
	/**
	 * ר
	 */
	private Long subjectId;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * 
	 */
	private Integer sort;

}
