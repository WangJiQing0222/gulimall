package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.activation.MimeType;

@Controller
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder", produces = {MediaType.TEXT_HTML_VALUE})
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

//        PayVo payVo = new PayVo();
//        payVo.setBody();//订单的备注
//        payVo.setOut_trade_no();//订单号
//        payVo.setSubject();//订单的主题
//        payVo.setTotal_amount();

        PayVo payVo = orderService.getOrderPay(orderSn);

        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);

        return pay;
    }

    @ResponseBody
    @GetMapping(value = "/demo", produces = {MediaType.TEXT_HTML_VALUE})
    public String demo() throws AlipayApiException {
        String res = alipayTemplate.demo();

        return res;
    }
}
