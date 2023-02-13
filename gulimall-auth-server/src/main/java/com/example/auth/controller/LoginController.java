package com.example.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.example.auth.feign.MemberFeignService;
import com.example.auth.feign.ThirdPartFeignService;
import com.example.auth.vo.UserLoginVo;
import com.example.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个页面
     * 这样太麻烦，使用springmvc viewcontroller提供的WebMvcConfigurer
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        //1、TODO 接口防刷

        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - l > 60000);{
                //60秒内不能再发
                R.error(BizCodeEnume.SMS_CODE_EXCETPION.getCode(), BizCodeEnume.SMS_CODE_EXCETPION.getMsg());
            }
        }

        //2、验证码校验 redis  key -> sms:code:18527860662, value ->
//        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        int code = (int) ((Math.random() * 9 + 1) * 100000);//阿里云短信测试，只能4~6纯数字
        String cacheCode = code + "_" + System.currentTimeMillis();

        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, cacheCode, 12, TimeUnit.MINUTES);

        System.out.println("验证码========================>" + code);
        thirdPartFeignService.sendCode(phone, code  + "");
        return R.ok();
    }

    /**
     * TODO: 重定向携带数据：利用session原理，将数据放在session中。
     * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO：分布下session问题
     * RedirectAttributes：重定向也可以保留数据，不会丢失
     * @param vo
     * @param result
     * @param attributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result,
                           RedirectAttributes attributes){
        //如果有错误回到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);

            //出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //1、校验验证码
        String code = vo.getCode();

        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if(code.equals(s.split("_")[0])){
                //删除验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                // 验证码通过，真正注册，调用远程服务进行注册
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0){
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }else {
                //校验出错回到注册页面
                HashMap<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }

        }else {
            //校验出错回到注册页面
            HashMap<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


    /**
     * 自己网站登录页
     * @param vo
     * @param attributes
     * @param session
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session){
        //远程登陆
        R login = memberFeignService.login(vo);
        if(login.getCode() == 0){
            //登录成功
            System.out.println("自己网站登录成功...");
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulilmall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    /**
     *
     */
//    @GetMapping("/login.html")
//    public String loginPage(HttpSession session){
//        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
//        if (attribute == null) {
//            //没登陆
//            System.out.println("No login。。。");
//            return "login";
//        }else {
//            //登录成功，跳转首页
//            System.out.println("login successfully, attribute:" + attribute);
//            return "redirect:http://gulimall.com";
//        }
//    }




//    @GetMapping("/login.html")
//    public String login(){
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String reg(){
//        return "reg";
//    }

}
