package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.service.SocialService;
import com.atguigu.gulimall.member.vo.*;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Autowired
    SocialService socialService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity entity = new MemberEntity();
        MemberDao memberDao = this.baseMapper;

        //设置其它的默认信息
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一。感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        entity.setUsername(vo.getUserName());//设置用户名
        entity.setMobile(vo.getPhone());//设置手机号

        entity.setNickname(vo.getUserName());

        //密码需要进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        log.debug("password:" + vo.getPassWord());
        String encode = encoder.encode(vo.getPassWord());
        entity.setPassword(encode);//设置密码

        memberDao.insert(entity);
    }


    @Override
    public void checkUserNameUnique(String userName) throws  UserNameExistException{
        Integer usernameCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(usernameCount > 0){
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer phoneCount  = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(phoneCount > 0){
            throw new PhoneExistException();
        }
    }

    public static void main(String[] args) {
        String pass = "123456";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = null;
        for (int i = 0; i < 10; i++) {
            encode = encoder.encode(pass);
            System.out.println(encoder.matches(pass, encode));
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1、去数据库查询 SELECT * FROM ums_member WHERE username = ? OR mobile = ?
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct).or().eq("mobile", loginacct));

        if (entity == null) {
            //登录失败
            return null;
        }else {
            //获取到数据库里的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = encoder.matches(password, passwordDb);
            if(matches) {
                //登录成功
                return entity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity giteeLogin(SocialGiteeUser giteeUser) throws Exception {
        //具有登录和注册逻辑
        HttpResponse response = socialService.getInfoByGiteeToken(giteeUser.getAccess_token());
        String json = EntityUtils.toString(response.getEntity());
        SocialGiteeUserInfoVo giteeUserInfoVo = JSON.parseObject(json, SocialGiteeUserInfoVo.class);
        //查询socialUid
        String uid = String.valueOf(giteeUserInfoVo.getId());

        //1、判断当前社交用户是否已经登录过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //这个用户已经注册过
            //更新用户的访问令牌的时间和access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(giteeUser.getAccess_token());
            update.setExpiresIn(giteeUser.getExpires_in());
            updateById(update);

            //数据库查询出来的信息更全面，所以返回memberEntity而不是update
            memberEntity.setAccessToken(giteeUser.getAccess_token());
            memberEntity.setExpiresIn(giteeUser.getExpires_in());
            return memberEntity;
        }else {
            MemberEntity register = new MemberEntity();
            try {
                if(response.getStatusLine().getStatusCode() == 200) {
                    //2、没有查到当前社交用户对应的记录我们就需要注册一个
                    //名称
                    register.setNickname(giteeUserInfoVo.getName());
                    //性别
                    register.setGender(0);//TODO gitee not have gender
                    //头像
                    register.setHeader(giteeUserInfoVo.getAvatar_url());
                    //注册时间
                    register.setCreateTime(new Date());
                }
            }catch (Exception e){}

            register.setSocialUid(uid);
            register.setAccessToken(giteeUser.getAccess_token());
            register.setExpiresIn(giteeUser.getExpires_in());

            //把用户信息插入到数据库中 注册
            this.baseMapper.insert(register);

            return register;
        }
    }

    @Override
    public MemberEntity weiboLogin(SocialWeiboUser weiboUser) throws Exception {
        //具有登录和注册逻辑
        HttpResponse response = socialService.getInfoByWeiboToken(weiboUser.getAccess_token());
        String json = EntityUtils.toString(response.getEntity());
        SocialWeiboUserInfoVo weiboUserInfoVo = JSON.parseObject(json, SocialWeiboUserInfoVo.class);
        //查询socialUid
        String uid = String.valueOf(weiboUserInfoVo.getUid());

        //1、判断当前社交用户是否已经登录过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //这个用户已经注册过
            //更新用户的访问令牌的时间和access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(weiboUser.getAccess_token());
            update.setExpiresIn(weiboUser.getExpires_in());
            updateById(update);

            //数据库查询出来的信息更全面，所以返回memberEntity而不是update
            memberEntity.setAccessToken(weiboUser.getAccess_token());
            memberEntity.setExpiresIn(weiboUser.getExpires_in());
            return memberEntity;
        }else {
            MemberEntity register = new MemberEntity();
            try {
                if(response.getStatusLine().getStatusCode() == 200) {
                    //2、没有查到当前社交用户对应的记录我们就需要注册一个
                    //名称
//                    register.setNickname(weiboUserInfoVo.getName());
                    //性别
//                    register.setGender(0);//TODO gitee not have gender
                    //头像
//                    register.setHeader(weiboUserInfoVo.getAvatar_url());
                    //注册时间
                    register.setCreateTime(new Date());
                }
            }catch (Exception e){}

            register.setSocialUid(uid);
            register.setAccessToken(weiboUser.getAccess_token());
            register.setExpiresIn(weiboUser.getExpires_in());

            //把用户信息插入到数据库中 注册
            this.baseMapper.insert(register);

            return register;
        }
    }

}