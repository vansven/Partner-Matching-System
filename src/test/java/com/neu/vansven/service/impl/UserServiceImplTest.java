package com.neu.vansven.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neu.vansven.domain.User;
import com.neu.vansven.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;



@SpringBootTest
@Slf4j
class UserServiceImplTest {


    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void userServiceTest() {
        User user = new User();
        user.setUserName("范思文");
        user.setUserAccount("xxxiA");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setPassword("123");
        user.setPhone("456");
        user.setEmail("789");
        boolean result = userService.save(user);
        Assertions.assertTrue(result);
        System.out.println(user.getId());
    }

    @Test
    void userRegisterTest(){
//        String account = "vansven_xxiA";
//        String password = "155422wwwwaa";
//        String checkPassword = "155422wwwwaa";
//        long userId = userService.userRegister(account, password, checkPassword);
//        Assertions.assertEquals(3,userId);
    }

    @Test
    void md5Test(){
        // 98cb0303b6d8546e4b7feeef9489334a --> 1222288888
        // e76e4de78f5d664c9fb051acb743c92f --> 15522244444www
        String string = "52d021dd788d2d41c1835bec168a2374";
        System.out.println(string.length());
        String vansven = UUID.nameUUIDFromBytes("vansven".getBytes()).toString();
        System.out.println(vansven);
        String van = UUID.nameUUIDFromBytes("vansven".getBytes()).toString().replace("-", "");
        String md5 = DigestUtils.md5DigestAsHex((van + "1222288888").getBytes());
        System.out.println(md5);
    }

    @Test
    void makeMD5(){
        // e11c8d14a883cc01080fc3f3120eeeb2 --> 12345678
        // 4173ddb197383d9ef368d729335b3532 --> 12345678xxxiA
        // fadce880592eae0faa9ea1cc63dcd2a6 --> 12345678vasven
        // 5476a96d05431fc7126861cf0e2aa916 --> xxxxxxxxiA
        String salt = UUID.nameUUIDFromBytes("vansven".getBytes()).toString().replaceAll("-", "");
        String md5 = DigestUtils.md5DigestAsHex((salt + "xxxxxxxxiA").getBytes());
        System.out.println(md5);
    }

    @Test
    void searchTest(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("tags","java").or().like("tags","c++");
        List<User> orignalUserList = userMapper.selectList(wrapper);
        System.out.println(orignalUserList);
    }

    @Test
    void  pageTest(){
        Page<User> page = new Page<>(1,10);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("tags","python");
        List<User> userList = userMapper.selectPage(page, null).getRecords();
//        System.out.println(userList.size());
        Page<User> userPage = userMapper.selectPage(new Page<>(1, 20), null);
        System.out.println(userPage);
    }

    @Test
    void jsonTest(){
        List<String> tags = Arrays.asList("java");
//        List<User> userList = userService.searchUserByTags(tags, HttpServletRequest);
//        System.out.println(userList);
    }

    @Test
    void  updateUserById(){
        User user = new User();
        user.setId(Long.valueOf(3));
        user.setTags("['php','python','c++']");
        int i = userMapper.updateById(user);
        Assertions.assertEquals(1,i);
    }

    @Test
    void redisOptions(){
        ValueOperations opsString = redisTemplate.opsForValue();
        User user = new User();
        opsString.set("vansven3", user);
        System.out.println(opsString.get("vansven3"));
    }

}