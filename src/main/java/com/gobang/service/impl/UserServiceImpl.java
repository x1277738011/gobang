package com.gobang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gobang.mapper.UserMapper;
import com.gobang.model.User;
import com.gobang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 添加一个用户
     * @param user
     * @return
     */
//    public Integer insert(User user){
//
//        return userMapper.insert(user);
//    }

    /**
     * 按名字查询
     * @param username
     * @return
     */

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 胜利更新
     * @param userId
     * @return
     */
    @Override
    public Integer userWin(Integer userId) {
        return userMapper.userWin(userId);
    }

    /**
     * 失败更新
     * @param userId
     * @return
     */
    @Override
    public Integer userLose(Integer userId) {
        return userMapper.userLose(userId);
    }
}
