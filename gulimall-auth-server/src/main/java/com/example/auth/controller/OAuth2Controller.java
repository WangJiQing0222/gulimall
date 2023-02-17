package com.example.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.example.auth.feign.MemberFeignService;
import com.atguigu.common.vo.MemberRespVo;
import com.example.auth.vo.SocialGiteeUser;
import com.example.auth.vo.SocialWeiboUser;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;


/*    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "");
        map.put("client_secret", "");
        map.put("grant_type", "");
        map.put("code", "");
        map.put("redirect_uri", "");
        //1、根据用户授权返回的code换取access_token
        HttpResponse post = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, null, map);

        //2、处理
        return "http://gulimall.com";
    }*/

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "413416ddbcc17bed59aedd026ebe611619f98e25d1f88f0c6dc4b2ee716fe135");
        map.put("client_secret", "26e99ecd200e91f0b0efcf1bf3f574f8f6cc8b137b8a113fdd2f0929831a65be");
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        //1、根据用户授权返回的code换取access_token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());
        if (response.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(response.getEntity());
            SocialGiteeUser socialUser = JSON.parseObject(json, SocialGiteeUser.class);

            //知道了哪个社交用户
            //1）、当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息，以后这个社交账号就对应指定的会员）
            //登录或者注册这个社交用户

            System.out.println("Gitee token entity===========>" + socialUser);
            //调用远程服务
            //TODO 远程调用出现ReadTimeOut 配置文件修改feign超时时间
            R r = memberFeignService.oauthloginGitee(socialUser);
            if(r.getCode() == 0){
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                System.out.println("登录成功， 用户信息为：, " + data.toString());

                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                System.out.println("session中的信息为：" + session.getAttribute(AuthServerConstant.LOGIN_USER));
                //2、登录成功跳回首页
                return "redirect:http://gulimall.com";
            }else {
                log.error("memberFeignService远程调用oauthloginGitee失败。。。");
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            log.error("用户授权返回的code去换取token失败。。。");
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "3452242740");
        map.put("client_secret", "6fdffb558f781badc4a3b5e41b65161a");
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        //1、根据用户授权返回的code换取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        if (response.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(response.getEntity());
            SocialWeiboUser weiboUser = JSON.parseObject(json, SocialWeiboUser.class);

            //知道了哪个社交用户
            //1）、当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息，以后这个社交账号就对应指定的会员）
            //登录或者注册这个社交用户

            System.out.println("Gitee token entity===========>" + weiboUser);
            //调用远程服务
            //TODO 远程调用出现ReadTimeOut 配置文件修改feign超时时间
            R r = memberFeignService.oauthloginWeibo(weiboUser);
            if(r.getCode() == 0){
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                System.out.println("登录成功， 用户信息为：, " + data.toString());

                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                System.out.println("session中的信息为：" + session.getAttribute(AuthServerConstant.LOGIN_USER));
                //2、登录成功跳回首页
                return "redirect:http://gulimall.com";
            }else {
                log.error("memberFeignService远程调用oauthloginWeibo失败。。。");
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            log.error("用户授权返回的code去换取token失败。。。");
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
