package com.atguigu.gulimall.product;

import com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 整合Mybatis-Plus
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
 *  MP逻辑删除
 *       logic-delete-value: 1 #deleted
 *       logic-not-delete-value: 0 #not delete
 *       实体类字段添加@TableLogic
 *
 * JSR303 数据校验
 *  @Valid
 *      使用校验注解@NotBlank @NotEmpty @NotNull @Pattern @URL
 *      使用注解@Valid加到校验参数上，开启校验功能，参考http://localhost:88/api/product/brand/save方法
 *      使用BindingResult紧跟对象后，获取校验结果
 *   分组校验：@Validated
 *      新增、修改为两组，再校验注解groups属性中添加，接受json对象前添加注解@validated(groups = {})
 *      没有指定分组的校验注解@NotBlank，在@validated下生效，在@Validated(groups = {})下不生效
 *
 *
 * 全局异常处理
 *      @ControllerAdvice+@ExceptionHandler
 * 同一状态码
 *      com.atguigu.common.exception.BizCodeEnume
 */
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
