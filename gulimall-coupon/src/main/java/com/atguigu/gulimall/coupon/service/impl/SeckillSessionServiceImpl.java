package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import org.springframework.util.StringUtils;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> get3LatestDaysSession(String startTime, String endTime) {
        List<SeckillSessionEntity> entityList = baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime, endTime));
        List<SeckillSessionEntity> collect = null;
        if (entityList != null && entityList.size() > 0) {
            collect = entityList.stream().map(seckillSessionEntity -> {
                Long sessionId = seckillSessionEntity.getId();
                List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.getBySessionId(sessionId);
                seckillSessionEntity.setSkuRelationEntities(skuRelationEntities);
                return seckillSessionEntity;
            }).collect(Collectors.toList());
        }
        return collect;
    }
}