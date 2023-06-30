package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderEntityTo {
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     *
     */
    private String orderSn;
    /**
     *
     */
    private Long couponId;
    /**
     * create_time
     */
    private Date createTime;
    /**
     *
     */
    private String memberUsername;
    /**
     *
     */
    private BigDecimal totalAmount;
    /**
     *
     */
    private BigDecimal payAmount;
    /**
     *
     */
    private BigDecimal freightAmount;
    /**
     *
     */
    private BigDecimal promotionAmount;
    /**
     *
     */
    private BigDecimal integrationAmount;
    /**
     *
     */
    private BigDecimal couponAmount;
    /**
     *
     */
    private BigDecimal discountAmount;
    /**
     * 1->2->3-> 4->
     */
    private Integer payType;
    /**
     * [0->PC1->app]
     */
    private Integer sourceType;
    /**
     * 0->1->2->3->4->5->
     */
    private Integer status;
    /**
     * ()
     */
    private String deliveryCompany;
    /**
     *
     */
    private String deliverySn;
    /**
     *
     */
    private Integer autoConfirmDay;
    /**
     *
     */
    private Integer integration;
    /**
     *
     */
    private Integer growth;
    /**
     * [0->1->2->]
     */
    private Integer billType;
    /**
     *
     */
    private String billHeader;
    /**
     *
     */
    private String billContent;
    /**
     *
     */
    private String billReceiverPhone;
    /**
     *
     */
    private String billReceiverEmail;
    /**
     *
     */
    private String receiverName;
    /**
     *
     */
    private String receiverPhone;
    /**
     *
     */
    private String receiverPostCode;
    /**
     * /
     */
    private String receiverProvince;
    /**
     *
     */
    private String receiverCity;
    /**
     *
     */
    private String receiverRegion;
    /**
     *
     */
    private String receiverDetailAddress;
    /**
     *
     */
    private String note;
    /**
     * [0->1->]
     */
    private Integer confirmStatus;
    /**
     * 0->1->
     */
    private Integer deleteStatus;
    /**
     *
     */
    private Integer useIntegration;
    /**
     *
     */
    private Date paymentTime;
    /**
     *
     */
    private Date deliveryTime;
    /**
     *
     */
    private Date receiveTime;
    /**
     *
     */
    private Date commentTime;
    /**
     *
     */
    private Date modifyTime;
}
