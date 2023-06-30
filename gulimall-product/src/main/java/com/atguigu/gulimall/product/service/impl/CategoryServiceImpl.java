package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    /**
     * 树形查询
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream().filter(c -> c.getCatLevel() == 1).map(c -> {
            c.setChildren(getChildren(c, entities));
            return c;
        }).sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort())))
                .collect(Collectors.toList());
    }

    /**
     * 在删除前查看是否有被引用
     *
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 查看引用的功能
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 获取某一分类id的分类路径
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] getCatelogPathByCatelogId(Long catelogId) {
        ArrayList<Long> arrayList = new ArrayList<>();
        CategoryEntity curr = baseMapper.selectById(catelogId);
        CategoryEntity parent = null;
        Integer level = curr.getCatLevel();
        while (level != 1) {
            arrayList.add(curr.getCatId());
            parent = baseMapper.selectById(curr.getParentCid());
            level = parent.getCatLevel();
            curr = parent;
        }
        if (parent == null) {
            arrayList.add(curr.getCatId());
        } else {
            arrayList.add(parent.getCatId());
        }
        Collections.reverse(arrayList);
        Long[] res = arrayList.toArray(new Long[arrayList.size()]);
        return res;
    }

    /**
     * 在修改表项时，同时维护关联表
     *
     * @param category
     */
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateAll(CategoryEntity category) {
        baseMapper.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    @Override
    public String getNameById(Long catelogId) {
        return categoryDao.getNameById(catelogId);
    }


    /**
     * 使用声明式缓存，实现缓存的读逻辑
     *
     * @param x
     * @return
     */
    @Cacheable(value = {"category"}, key = "'getFirstLevelCatelog'")
    @Override
    public List<CategoryEntity> getXLevelCatelog(int x) {
        System.out.println("从数据库查询分类");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", x));
        return categoryEntities;
    }

    /**
     * 使用声明式缓存，实现缓存的读逻辑:也就是说在直接开发我们查询数据库的逻辑即可，也不用先去查缓存，也不用查完数据库后维护更新缓存
     *
     * @param level1Catelogs
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2Vo>> getSubCatlogs(List<CategoryEntity> level1Catelogs) {

        List<CategoryEntity> allData = categoryDao.selectList(null);
        Map<String, List<Catalog2Vo>> collect = null;
        if (level1Catelogs != null && level1Catelogs.size() > 0) {
            collect = level1Catelogs.stream().collect(Collectors.toMap(l1 -> l1.getCatId().toString(), l1 -> {
                List<Catalog2Vo> catalog2Vos = new ArrayList<>();
//                List<CategoryEntity> categoryEntities = this.getBaseMapper().selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l1.getCatId()));
                List<CategoryEntity> categoryEntities = getParent_cid(allData, l1.getCatId());
                if (categoryEntities != null && categoryEntities.size() > 0) {
                    catalog2Vos = categoryEntities.stream().map(l2 -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
//                        List<CategoryEntity> categoryEntities1 = this.getBaseMapper().selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                        List<CategoryEntity> categoryEntities1 = getParent_cid(allData, l2.getCatId());
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                        if (categoryEntities1 != null && categoryEntities1.size() > 0) {
                            catalog3Vos = categoryEntities1.stream().map(l3 -> {
                                Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catalog3Vo;
                            }).collect(Collectors.toList());
                        }
                        catalog2Vo.setCatalog3List(catalog3Vos);
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));
        }
        return collect;
    }

    /**
     * 手写缓存逻辑：先查缓存，查不到去查数据库，并将数据存入缓存 (整合springcach后，使用声明式缓存即可实现对缓存的crud维护)
     *
     * @param level1Catelogs
     * @return
     */
    public Map<String, List<Catalog2Vo>> getSubCatlogs2(List<CategoryEntity> level1Catelogs) {
        // 先从缓存中获取
        String subCatlogs = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(subCatlogs)) {
            // 如果获取不到，就从数据库中查询
            Map<String, List<Catalog2Vo>> subCatlogsFromDb = getSubCatlogsFromDbWithRedissonLock(level1Catelogs);
            return subCatlogsFromDb;
            // 并把数据放入缓存，为保证原子性，这个操作写在查数据库的方法中
        }
        // JSON的好处是跨语言，跨平台
        // 将从缓存中拿到的JSON字符串进行类型转换
        Map<String, List<Catalog2Vo>> res = JSON.parseObject(subCatlogs, new TypeReference<>() {
        });
        return res;
    }

    /**
     * 加分布式锁
     * 所以通过可重入锁解决并发问题，当第一个查询数据库并将数据放入缓存后，其余在此的请求将直接查缓存获取数据
     *
     * @param level1Catelogs
     * @return
     */
    private Map<String, List<Catalog2Vo>> getSubCatlogsFromDbWithRedissonLock(List<CategoryEntity> level1Catelogs) {
        // 注意锁的名字，关乎锁的粒度，越细越好
        RLock lock = redissonClient.getLock("subCatalogs-lock");
        Map<String, List<Catalog2Vo>> catalogsJsonFromDb = null;
        try {
            lock.lock(); // 与ReentrantLock一样的阻塞式等待
            catalogsJsonFromDb = getCatalogsJsonFromDb(level1Catelogs);
        } catch (Exception ignored) {
        } finally {
            lock.unlock();
        }
        return catalogsJsonFromDb;
    }

    /**
     * 手写一个自旋锁
     *
     * @param level1Catelogs
     * @return
     */
    @Deprecated
    private Map<String, List<Catalog2Vo>> getSubCatlogsFromDbWithRedisLock(List<CategoryEntity> level1Catelogs) {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("subCatalogs-lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            // 获取锁成功
            Map<String, List<Catalog2Vo>> catalogsJsonFromDb = null;
            try {
                catalogsJsonFromDb = getCatalogsJsonFromDb(level1Catelogs);
            } catch (Exception ignored) {
            } finally {
                // 删除锁(通过redis的lua脚本，保证查和删的原子性)
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";                                                                          // KEYS        // ARGV
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("subCatalogs"), uuid);
            }
            return catalogsJsonFromDb;
        } else {
            // 没获取到锁的线程可以用自旋和阻塞的方式结合着进行，来自旋查看资源
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getSubCatlogsFromDbWithRedisLock(level1Catelogs);
        }
    }

    /**
     * 查询数据库
     * 交互次数优化：这样的业务逻辑频繁的与db交互，大大的影响了我们的性能，所以我们可以一次就把所有数据都查出来，存在局部变量中，然后之后的模型封装直接从本地取值
     * mysql优化
     * 同时在查询数据库前再查询一次redis，在缓存击穿后大量请求在此等待与数据库交互，我们只需要第一个请求访问数据库获取数据，后面的直接去从缓存中拿数据即可
     *
     * @param level1Catelogs
     * @return
     */
    private Map<String, List<Catalog2Vo>> getCatalogsJsonFromDb(List<CategoryEntity> level1Catelogs) {
        String dataFromCache = stringRedisTemplate.opsForValue().get("level1Catelogs");
        if (StringUtils.isEmpty(dataFromCache)) { // 经典的双重检查
            // 必须要与数据库交互
            List<CategoryEntity> allData = categoryDao.selectList(null);
            Map<String, List<Catalog2Vo>> collect = null;
            if (level1Catelogs != null && level1Catelogs.size() > 0) {
                collect = level1Catelogs.stream().collect(Collectors.toMap(l1 -> l1.getCatId().toString(), l1 -> {
                    List<Catalog2Vo> catalog2Vos = new ArrayList<>();
//                List<CategoryEntity> categoryEntities = this.getBaseMapper().selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l1.getCatId()));
                    List<CategoryEntity> categoryEntities = getParent_cid(allData, l1.getCatId());
                    if (categoryEntities != null && categoryEntities.size() > 0) {
                        catalog2Vos = categoryEntities.stream().map(l2 -> {
                            Catalog2Vo catalog2Vo = new Catalog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
//                        List<CategoryEntity> categoryEntities1 = this.getBaseMapper().selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                            List<CategoryEntity> categoryEntities1 = getParent_cid(allData, l2.getCatId());
                            List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                            if (categoryEntities1 != null && categoryEntities1.size() > 0) {
                                catalog3Vos = categoryEntities1.stream().map(l3 -> {
                                    Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                    return catalog3Vo;
                                }).collect(Collectors.toList());
                            }
                            catalog2Vo.setCatalog3List(catalog3Vos);
                            return catalog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catalog2Vos;
                }));
            }
            // 并且把查询到的结果，以JSON形式放入到缓存中，维护缓存(原子性操作)
            String s = JSON.toJSONString(collect);
            stringRedisTemplate.opsForValue().append("catelogJSON", s);
            return collect;
        } else {
            // 说明缓存已经被更新存在
            return JSON.parseObject(dataFromCache, new TypeReference<>() {
            });
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> allData, Long parent_cid) {
        if (allData != null && allData.size() > 0) {
            List<CategoryEntity> res = allData.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
            return res;
        } else {
            return null;
        }
    }

    /**
     * 递归查询某一分类的子集
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(c -> c.getParentCid() == root.getCatId())
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort())))
                .map(c -> {
                    List<CategoryEntity> sub_children = getChildren(c, all);
                    c.setChildren(sub_children);
                    return c;
                }).collect(Collectors.toList());
        return children;
    }

}