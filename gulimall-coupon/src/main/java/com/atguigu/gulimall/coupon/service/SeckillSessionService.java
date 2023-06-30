package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 16:58:29
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取当前时间起三天内开始的session
     * @param startTime
     * @param endTime
     * @return
     */
    List<SeckillSessionEntity> get3LatestDaysSession(String startTime, String endTime);
}

