package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {
    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/userInfo")
    @ResponseBody
    public String userInfo(@RequestParam("token") String token){
        String s = redisTemplate.opsForValue().get(token);
        return s;
    }

    @GetMapping("/login.html")
    public String loginPage(Model model, @RequestParam("redirect_url") String url,
                            @CookieValue(value = "sso_token", required = false) String sso_token){

        if(!StringUtils.isEmpty(sso_token)){
            //之前有人登录过留下了痕迹
            return "redirect:" + url + "?token=" + sso_token;
        }

        model.addAttribute("redirect_url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("redirect_url") String redirect_url,
                          HttpServletResponse response){
        //登录成功跳转，跳回到之前的页面;
        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){
            //登录成功，把登录成功的用户存起来
            String token = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(token, username);

            response.addCookie(new Cookie("ss_token", token));//添加cookie

            return "redirect:" + redirect_url + "?token=" + token;
        }
        //登陆失败，展示登录页
        return "login";
    }

}
