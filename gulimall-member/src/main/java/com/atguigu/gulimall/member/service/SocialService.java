package com.atguigu.gulimall.member.service;

import org.apache.http.HttpResponse;

public interface SocialService {
    HttpResponse getInfoByGiteeToken(String token) throws Exception;

    HttpResponse getInfoByWeiboToken(String access_token) throws Exception;
}
