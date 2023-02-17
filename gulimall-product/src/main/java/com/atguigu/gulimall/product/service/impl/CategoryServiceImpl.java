package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //可以使用ServiceImpl中的baseMapper
//    @Autowired
//    private CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        baseMapper.selectList(new QueryWrapper<CategoryEntity>());
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2、组装成父子的树形结构
        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menu = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map(menu -> {
            //设置一级分类的子分类
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return level1Menu;
    }

    /**
     *
     * @param asList
     */
    @Override
    public void removeMunuByIds(List<Long> asList) {
        //TODO 检查当前菜单是否被别的地方引用
        //1、逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2, 34, 225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPaths = findParentPath(catelogId, paths);

        Collections.reverse(parentPaths);//反转，父类id在前

        return parentPaths.toArray(new Long[parentPaths.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * 失效模式： @CacheEvict
     * 1、同时进行多种缓存操作 @Caching
     * 2、删除指定的某个分支下的所有数据： @CacheEvict(value = "category", allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存前缀
     * @param category
     */
//    @CacheEvict(value = "category", key = "'getLevel1Categorys'")

//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'"),
//    })

    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        //更新中间表数据
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 每一个缓存到数据都放到指定的分区【按照业务类型】
     * @Cacheable({"category"})
     *      代表当前方法的结果需要缓存，缓存中有，方法不被调用。
     *      缓存中没有，调用方法，方法结果放到缓存中
     *      分区key是默认为空  String key() default "";
     *      缓存的数据默认使用jdk序列化机制，将序列化后的数据存到redis
     *      默认ttl时间 -1，可以在配置文件中设置 spring.cache.redis.time-to-live=3600000
     * 自定义
     *      指定生产的缓存使用的key，key属性指定，接受一个spel
     *      指定缓存数据的存活时间，配置文件中修改ttl
     *      将数据保存为json格式
     */

    @Cacheable(value = {"category"}, key = "#root.method.name")//代表当前方法的结果需要缓存，缓存中有，方法不被调用。缓存中没有，调用方法，方法结果放到缓存中
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("index/catalog.json 查询数据库了。。。。");
        //TODO 优化，一次查出所有数据，减少数据库访问
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parent_id = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //查询每个一级分类的二级分类
            List<CategoryEntity> level2Catelog = getParent_cid(selectList, v.getCatId());

            //封装categoryEntities为Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (level2Catelog != null) {
                catelog2Vos = level2Catelog.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //封装2级分类的 3级子分类(catalog3List)
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        catelog2Vo.setCatalog3List(catelog3Vos);//设置2级分类的 3级子分类
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parent_id;
    }

    /**
     * OutOfDirectMemoryError: failed to allocate 46137344 byte(s) of direct memory (used: 58720256, max: 100663296)
     * 1）、springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
     * 2）、lettuce的bug导致堆外内存溢出，netty如果没有指定堆外内存，默认使用-Xmx100m
     *      可以通过-Dio.netty.maxDirectMemory设置
     * 解决方案：不能直接使用-Dio.netty.maxDirectMemory只去调大堆外内存
     *       1）、升级lettuce客户端  2）、切换使用jedis
     * @return
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2(){
        //给缓存中放入json字符串，拿出json字符串，还能逆转为能用的对象类型，[序列化与反序列化]

        //1、加入缓存逻辑，缓存中的数据json字符串
        //json跨语言，跨平台兼容，php取出来了的数据也是json字符串
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (catalogJson == null) {
            //2、缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();

            //数据放到缓存需要放到同步代码块里 保证数据只被查询一次
            /*//3、查到的数据再放入缓存，将对象转为json放在缓存中
            String s = JSON.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJson", s);*/
            return catalogJsonFromDb;
        }

        //转为指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return result;
    }

    /**
     * 缓存里的数据如何和数据库保持一致
     * 缓存数据一致性
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock(){
        //1、锁的名字。锁得粒度，越细越快
        //锁的粒度，具体缓存的是某个具体数据，11-商品： product-11-lock product-12-lock

        RLock lock = redissonClient.getLock("catalockJson-lock");
        lock.lock();//加锁

        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        }finally {
            lock.unlock();//解锁
        }

        return dataFromDb;
    }


    /**
     * 从数据库总查询并封装分类数据：分布式锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock(){
        //占分布式锁，去redis填坑
        String uuid = UUID.randomUUID().toString();//
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if(lock){
            System.out.println("获取分布式锁成功。。。");
            //加锁成功，执行业务
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            }finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            /*//获取值对比+对比成功删除=原子操作，使用lua脚本解锁
            String lockValue = redisTemplate.opsForValue().get("lock");
            if(uuid.equals(lockValue)){
                //删除我自己的锁
                redisTemplate.delete("lock");//删除锁
            }*/

            return dataFromDb;
        }else {
            //加锁失败，重试，synchronized()
            //休眠100ms重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("获取分布式锁失败，等待重试。。。");
            return getCatalogJsonFromDbWithRedisLock();//自旋
        }
    }

    /**
     * 从数据库总查询并封装分类数据：本地锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //SpringBoot所有组件在容器中都是单例的
        //TODO  本地锁：synchronized，juc(lock) 在分布式场景下，想要锁住所有，必须使用分布式锁

        synchronized (this){
            return getDataFromDb();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //类似双重校验锁，外if判断是否null，内if确定缓存是否有数据，避免查数据库
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //获取到锁，缓存中有数据，直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        System.out.println("index/catalog.json 查询数据库了。。。。");
        //TODO 优化，一次查出所有数据，减少数据库访问
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parent_id = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //查询每个一级分类的二级分类
            List<CategoryEntity> level2Catelog = getParent_cid(selectList, v.getCatId());

            //封装categoryEntities为Catelog2Vo
            List<Catelog2Vo> catelog2Vos = null;
            if (level2Catelog != null) {
                catelog2Vos = level2Catelog.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //封装2级分类的 3级子分类(catalog3List)
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        catelog2Vo.setCatalog3List(catelog3Vos);//设置2级分类的 3级子分类
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));


        //3、查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_id);
        redisTemplate.opsForValue().set("catalogJson", s);
        return parent_id;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    //[225, 34, 2]
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);//收集当前结点id

        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);//递归，向上找
        }

        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}