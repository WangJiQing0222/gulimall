package com.atguigu.gulimall.member;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.service.SocialService;
import com.atguigu.gulimall.member.vo.SocialGiteeUser;
import com.atguigu.gulimall.member.vo.SocialGiteeUserInfoVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallMemberApplicationTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    SocialService socialService;

    @Test
    public void member() throws Exception {
        SocialGiteeUser user = new SocialGiteeUser();
        user.setAccess_token("dbd0baf83b3593a000227563145c9881");
        user.setExpires_in("86400");
        user.setToken_type("bearer");
        MemberEntity login = memberService.giteeLogin(user);
        System.out.println(login);
    }

    @Test
    public void giteeInfo() throws Exception {
        HttpResponse response = socialService.getInfoByGiteeToken("e79f5013af56360149eecd796d177309");
        System.out.println(response);
    }


    @Test
    public void contextLoads() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("小王i");

        memberService.save(memberEntity);
    }

    @Test
    public void socialGitee() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "e79f5013af56360149eecd796d177309");
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), map);

        if(response.getStatusLine().getStatusCode() == 200){
            System.out.println("successful===============>" + response.getEntity());
            String json = EntityUtils.toString(response.getEntity());
            System.out.println("json************************"+json);
            SocialGiteeUserInfoVo userInfoVo = JSON.parseObject(json, SocialGiteeUserInfoVo.class);
            System.out.println("userInfoVo---------------->"+userInfoVo);
        }else {
            System.out.println("fail");
        }
    }
}
