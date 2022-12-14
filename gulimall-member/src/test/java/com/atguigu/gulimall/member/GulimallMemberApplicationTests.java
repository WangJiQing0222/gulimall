package com.atguigu.gulimall.member;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallMemberApplicationTests {

    @Autowired
    private MemberService memberService;

    @Test
    public void contextLoads() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("小王i");

        memberService.save(memberEntity);
    }

}
