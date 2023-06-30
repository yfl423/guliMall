package com.atguigu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 17:46:17
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据会员id查询地址信息
     */
    List<MemberReceiveAddressEntity> getAddressInfo(Long memberId);
}

