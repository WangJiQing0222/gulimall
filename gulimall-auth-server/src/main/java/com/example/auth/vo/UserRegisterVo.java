package com.example.auth.vo;

import com.example.auth.feign.MemberFeignService;
import com.example.auth.feign.ThirdPartFeignService;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVo {
    @NotEmpty
    @Length(min = 6, max = 18, message = "用户名长度再6-18位字符")
    private String userName;

    @NotEmpty
    @Length(min = 6, max = 18, message = "密码长度再6-18位字符")
    private String passWord;

    @NotEmpty
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
