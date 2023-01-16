package com.atguigu.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.atguigu.gulimall.ware.dao")
@EnableTransactionManagement
@Configuration
public class WareMybatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
//        //设置请求的页面大于最后一页，true跳转到第一页，false继续请求
//        paginationInterceptor.setOverflow(true);
//        //设置最大单页请求限制，-1表示不受限制
//        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
