package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 订单详情页
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        System.out.println("==============>"+confirmVo);
        model.addAttribute("orderConfirmData", confirmVo);
        //展示订单确认的数据
        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes){

        //下单：去创建订单，脸令牌，验价格，锁库存.··
        System.out.println("订单提交得数据：" + vo);
        SubmitOrderResponseVo responseVo =  orderService.submitOrder(vo);
        if (responseVo.getCode() == 0) {
            //下单成功来到支付选择页
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }else {
            //下单失败回至订单确认页重新确认订单信
            String msg="下单失败：";
            switch (responseVo.getCode()) {
                case 1: msg += "订单信息过期，请刷新后再次提交";break;
                case 2: msg += "订单商品价格发生变化，请确认后再次提交";break;
                case 3: msg += "库存锁定失败，商品库存不足";break;
            }

            attributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
