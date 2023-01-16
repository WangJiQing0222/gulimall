package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.debug("数据校验出现问题：{}，出现问题异常为:{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item) -> {
           map.put(item.getField(), item.getDefaultMessage());
        });
        //使用枚举
        return R.error(BizCodeEnume.VALID_EXCETPION.getCode(), BizCodeEnume.VALID_EXCETPION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable t){
        log.debug("未知异常。。。{}", t.getStackTrace());
        log.debug("原因：{}", t.getMessage());
        return R.error(BizCodeEnume.UNKNOW_EXCETPION.getCode(), BizCodeEnume.UNKNOW_EXCETPION.getMsg());
    }
}
