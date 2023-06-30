package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spuͼƬ
 * 
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:05
 */
@Data
@TableName("pms_spu_images")
public class SpuImagesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * ͼƬ
	 */
	private String imgName;
	/**
	 * ͼƬ
	 */
	private String imgUrl;
	/**
	 * ˳
	 */
	private Integer imgSort;
	/**
	 * 
	 */
	private Integer defaultImg;

}
