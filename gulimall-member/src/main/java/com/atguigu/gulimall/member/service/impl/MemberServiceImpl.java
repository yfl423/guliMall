package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PasswordNotMatchUsernameException;
import com.atguigu.gulimall.member.exception.UsernameNotExistsException;
import com.atguigu.gulimall.member.exception.UsernameNotOnlyException;
import com.atguigu.gulimall.member.exception.MobileNotOnlyException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserEnrollTo;
import com.atguigu.gulimall.member.vo.UserLoginTo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void enroll(UserEnrollTo userEnrollTo) {
        MemberEntity memberEntity = new MemberEntity();
        // 正式注册前要保证会员名和手机号唯一
        checkUsername(userEnrollTo.getUserName());
        checkMobile(userEnrollTo.getPhone());
        // 注册默认设置为初级会员
        MemberLevelEntity defaultLevel = memberLevelService.selectDefaultLevel();
        memberEntity.setLevelId(defaultLevel.getId());
        memberEntity.setUsername(userEnrollTo.getUserName());
        memberEntity.setMobile(userEnrollTo.getPhone());
        // 密码在数据库中不能明文存储，要进行加密处理
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(userEnrollTo.getPassword());
        memberEntity.setPassword(encode);
        this.baseMapper.insert(memberEntity);

    }

    @Override
    public void checkUsername(String username) throws UsernameNotOnlyException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count != 0) {
            throw new UsernameNotOnlyException();
        }
    }

    @Override
    public void checkMobile(String mobile) throws MobileNotOnlyException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        if (count != 0) {
            throw new MobileNotOnlyException();
        }

    }

    @Override
    public MemberEntity login (UserLoginTo userLoginTo) throws UsernameNotExistsException, PasswordNotMatchUsernameException {
        // 1.校验用户名是否存在
        String userName = userLoginTo.getUserName();
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (entity == null) {
            // 用户名不存在
            throw new UsernameNotExistsException();
        } else {
            String password = userLoginTo.getPassword();
            String encodedPassword = entity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (!bCryptPasswordEncoder.matches(password, encodedPassword)) {
                // 用户名或密码输入错误
                throw new PasswordNotMatchUsernameException();
            }
            else {
                // 验证登陆成功
                return entity;
            }
        }
    }

    @Override
    public MemberEntity socialLogin(SocialUser socialUser) {
        // 注册和登录的合并逻辑
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));
        // 查询是否登录过
        if (entity != null) {
            // 当前使用社交登录的账号已注册
            MemberEntity update = new MemberEntity();
            update.setId(entity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            baseMapper.updateById(update);

            entity.setAccessToken(socialUser.getAccess_token());
            entity.setExpiresIn(socialUser.getExpires_in());
            return entity;
        } else {
            // 当前使用社交登录账号在系统数据库没有注册（第一次登录）
            MemberEntity enroll = new MemberEntity();
            try {
                // 携带access token访问微博资源服务器的接口，获取个人账号微博的所有开放信息
                String host = "https://api.weibo.com";
                String path = "/2/users/show.json";
                Map<String, String> queries = new HashMap<>();
                queries.put("access_token", socialUser.getAccess_token());
                queries.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet(host, path, "get", new HashMap<>(), queries);

                if (response.getStatusLine().getStatusCode() == 200) {
                    // 查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    // 可以注入一切微博开放的信息
                    enroll.setNickname(name);
                    enroll.setGender("m".equalsIgnoreCase(gender)?1:0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            enroll.setSocialUid(socialUser.getUid());
            enroll.setAccessToken(socialUser.getAccess_token());
            enroll.setExpiresIn(socialUser.getExpires_in());
            baseMapper.insert(enroll);
            return enroll;
        }
    }
}