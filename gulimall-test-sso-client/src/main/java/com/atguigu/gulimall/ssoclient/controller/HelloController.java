package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 * client1和client2处于不同服务器，只要访问资源是没登陆，后面附加token=...，即可向认证服务器获取信息，
 * 并将信息设置到session中，下次再次访问资源，就不需要在携带token了
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录就可访问
     *
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/employees")
    public String employees(Model model, HttpSession session,
                            @RequestParam(value = "token", required = false) String token){
        if (!StringUtils.isEmpty(token)) {
            //含有token，表示登录成功
//            session.setAttribute("loginUser", "WangJiQing");
            //用token向服务端获取真正的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://sso.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser", body);
        }


        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登陆，跳转登录服务器登录

            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }else {
            ArrayList<String> emps = new ArrayList<>();
            emps.add("张三儿");
            emps.add("李四儿");

            model.addAttribute("emps", emps);
            return "list";
        }
    }

}
