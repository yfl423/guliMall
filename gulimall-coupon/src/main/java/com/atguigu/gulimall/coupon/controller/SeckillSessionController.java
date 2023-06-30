package com.atguigu.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * @author fenglong yang
 * @email yfl423@tamu.edu
 * @date 2021-01-18 16:58:29
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {
    @Autowired
    private SeckillSessionService seckillSessionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSessionService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 获取最近三天内将要开始的秒杀session
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/get3LatestDaysSession")
    public R get3LatestDaysSession(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        List<SeckillSessionEntity> entityList = seckillSessionService.get3LatestDaysSession(startTime, endTime);
        return R.ok().put("data", entityList);
    }
}
