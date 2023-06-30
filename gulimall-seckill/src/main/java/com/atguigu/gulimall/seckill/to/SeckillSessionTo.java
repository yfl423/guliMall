package com.atguigu.gulimall.seckill.to;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class SeckillSessionTo {
    /**
     * id
     */
    private Long id;
    /**
     *
     */
    private String name;
    /**
     * ÿ
     */
    private Date startTime;
    /**
     * ÿ
     */
    private Date endTime;
    /**
     *
     */
    private Integer status;
    /**
     *
     */
    private Date createTime;

    private List<SeckillSkuRelationEntityTo> skuRelationEntities;
}
