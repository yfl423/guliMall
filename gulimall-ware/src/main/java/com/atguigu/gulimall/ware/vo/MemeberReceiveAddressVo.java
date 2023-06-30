package com.atguigu.gulimall.ware.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MemeberReceiveAddressVo {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String phone;
    /**
     *
     */
    private String postCode;
    /**
     * ʡ
     */
    private String province;
    /**
     *
     */
    private String city;
    /**
     *
     */
    private String region;
    /**
     *
     */
    private String detailAddress;
    /**
     * ʡ
     */
    private String areacode;
    /**
     *
     */
    private Integer defaultStatus;
}
