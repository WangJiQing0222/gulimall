package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合Mybatis-Plus
 *      1)、导入依赖
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *      2)、配置
 *          1、配置数据源
 *
 *          2、配置Mybatis-Plus：
 *              1)、使用@MapperScan
 *              2)、告诉Mybatis-Plus，sql映射文件位置
 *
 *  2、MP逻辑删除
 *       logic-delete-value: 1 #deleted
 *       logic-not-delete-value: 0 #not delete
 *       实体类字段添加@TableLogic
 *
 * 3、JSR303 数据校验
 *  @Valid
 *      使用校验注解@NotBlank @NotEmpty @NotNull @Pattern @URL
 *      使用注解@Valid加到校验参数上，开启校验功能，参考http://localhost:88/api/product/brand/save方法
 *      使用BindingResult紧跟对象后，获取校验结果
 *   分组校验：@Validated
 *      新增、修改为两组，再校验注解groups属性中添加，接受json对象前添加注解@validated(groups = {})
 *      没有指定分组的校验注解@NotBlank，在@validated下生效，在@Validated(groups = {})下不生效
 *
 *
 * 4、全局异常处理
 *      @ControllerAdvice+@ExceptionHandler
 * 统一状态码
 *      com.atguigu.common.exception.BizCodeEnume
 *
 * 5、整合Redisson作为分布式锁等功能框架
 *     1）、引依赖
 *         <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *         </dependency>
 *      2）、MyRedissonConfig中配置一个RedissonClient实例即可
 *      3）、使用
 *         参照Github上的官方文档
 *
 *  6、整合spring-cache简化缓存开发
 *      1）、引依赖
 *          spring-boot-starter-cache、spring-boot-starter-data-redis
 *      2）、写配置
 *          （1）、自动配置了哪些
 *                  CacheAutoConfiguration会导入 RedisCacheConfiguration
 *                  自动配好了缓存管理器RedisCacheManager
 *          （2）、配置使用Redis作为缓存
 *              spring.cache.type=redis
 *      3）、测试使用缓存
 *          @Cacheable: Triggers cache population.触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction.触发将数据从缓存删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution.不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method.组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level.在类级别共享缓存的相同配置
 *          （1）、开启缓存
 *              @EnableCaching
 *          （2）、使用注解就能完成缓存操作
 *       4）、原理
 *       CacheAutoConfiguration->RedisCacheConfiguration
 *       如果配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用什么配置
 *       ->如果有redisCacheConfiguration就用已有的，没有就使用默认配置
 *       ->想改缓存的配置，只要给容器中放入一个RedisCacheConfiguration即可
 *       ->就会应用到CacheAutoConfiguration管理的所有缓存中
 *
 *      5）、spring-cache的不足
 *          （1）、读模式
 *              缓存穿透：查询一个null数据。解决：缓存空数据
 *              缓存击穿：大量并发进来同时查询一个过期的数据。解决：加锁？默认是无锁的，sync=true，适合做本地锁
 *              缓存雪崩：大量的key同时过期。解决：加随机时间，（因为不可能同时缓存所有数据）即加过期时间
 *          （2）、写模式（缓存与数据库一致）
 *              读写加锁。
 *              引入canal，感知到MySQL的更新去更新数据库
 *              读多写多，直接去数据库查询就行
 *           总结：
 *              常规数据（读多写少，及时性、一致性要求不高的数据），完全可以使用spring-cache 写模式：（只要缓存的数据有过期时间就行了）
 *              特殊数据：特殊设计
 *       -------读多写少读数据直接用SpringCache,写数据可以加读写锁，读多写多直接查数据库
 *          原理
 *              CacheManager(RedisCacheManager) -> Cache(RedisCache) -> Cache负责缓存的读写
 */
@EnableRedisHttpSession     //开启springsession
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
