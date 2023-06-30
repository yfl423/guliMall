package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * Ʒ
 *
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-17 22:43:06
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Ʒ
     */
    @NotNull(message = "修改品牌时品牌id不能为空！", groups = {UpdateGroup.class})
    @Null(message = "新增品牌时不能添加品牌id!", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * Ʒ
     */
    @NotBlank(message = "品牌名不能为空!", groups = {AddGroup.class})
    private String name;
    /**
     * Ʒ
     */
    @NotBlank(message = "logo不能为空!", groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     *
     */
    @NotBlank(message = "介绍不能为空!", groups = {AddGroup.class})
    private String descript;
    /**
     *
     */
    // 自定义校验器
    @NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0,1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     *
     */
    @NotBlank(message = "检索首字母不能为空!", groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须a-z或A-Z")
    private String firstLetter;
    /**
     *
     */
    @NotNull(message = "排序字段不能为空!", groups = {AddGroup.class})
    @Min(value = 0, message = "排序字段必须是正整数", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
