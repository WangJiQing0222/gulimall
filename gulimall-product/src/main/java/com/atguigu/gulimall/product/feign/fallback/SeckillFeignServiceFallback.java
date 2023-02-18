package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId){
        log.debug("远程调用服务getSkuSeckillInfo失败，提供方服务降级了。。。");
        return R.error("远程调用服务getSkuSeckillInfo失败，提供方服务降级了。。。");
    }
}
