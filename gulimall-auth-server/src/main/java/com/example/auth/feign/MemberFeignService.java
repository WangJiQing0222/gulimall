package com.example.auth.feign;

import com.atguigu.common.utils.R;
import com.example.auth.vo.SocialGiteeUser;
import com.example.auth.vo.UserLoginVo;
import com.example.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 注册
     * @param vo
     * @return
     */
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegisterVo vo);

    /**
     * 自己网站登录
     * @param vo
     * @return
     */
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    /**
     * OAuth2.0_Gitee登录
     * @param vo
     * @return
     * @throws Exception
     */
    @PostMapping("/member/member/oauth2/gitee/login")
    R oauthloginGitee(@RequestBody SocialGiteeUser vo) throws Exception;
}
