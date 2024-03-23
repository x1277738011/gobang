package com.gobang;

import com.gobang.mapper.UserMapper;
import com.gobang.model.User;
import com.gobang.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class GobangApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Test
    void contextLoads() {
        System.out.println(userMapper.selectById(1));
    }
    @Test
    void register(){
        User user = new User();
        user.setUsername("lisi");
        BCryptPasswordEncoder bCryptPasswordEncoder =new BCryptPasswordEncoder();
        user.setPassword("123");
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        System.out.println(user);
        userService.save(user);
    }
    @Test
    void login(){
        BCryptPasswordEncoder bCryptPasswordEncoder =new BCryptPasswordEncoder();
        User user = userService.selectByName("xcc");
        if (bCryptPasswordEncoder.matches("123", user.getPassword())){
            System.out.println("密码正确");
        }else {
            System.out.println("密码错误");
        }


    }

}
