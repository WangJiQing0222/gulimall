package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.service.SocialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SocialServiceImpl implements SocialService {
    @Override
    public HttpResponse getInfoByGiteeToken(String token) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", token);
        //获取gitee用户信息
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), map);

        if(response.getStatusLine().getStatusCode() == 200){
            return response;
        }else {
            log.error("gitee获取用户信息失败。。。");
            return null;
        }
    }

    @Override
    public HttpResponse getInfoByWeiboToken(String access_token) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", access_token);
        //获取gitee用户信息
        HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/account/get_uid.json", "get", new HashMap<>(), map);

        if(response.getStatusLine().getStatusCode() == 200){
            return response;
        }else {
            log.error("gitee获取用户信息失败。。。");
            return null;
        }
    }
}
