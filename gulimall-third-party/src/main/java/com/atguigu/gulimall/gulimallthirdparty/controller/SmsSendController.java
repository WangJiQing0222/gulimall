package com.atguigu.gulimall.gulimallthirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallthirdparty.component.AliyunSms;
import com.atguigu.gulimall.gulimallthirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("sms")
public class SmsSendController {

    @Autowired
    private AliyunSms sms;

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) throws ExecutionException, InterruptedException {
        sms.sendCode(phone, code);
        return R.ok();
    }

}
