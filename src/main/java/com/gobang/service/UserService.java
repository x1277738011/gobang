package com.gobang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gobang.model.User;


public interface UserService extends IService<User> {

    //根据用户名,来查询用户的详细信息,用于登录功能
    User selectByName(String username);

    // 总比赛场数 + 1, 获胜场数 + 1, 天梯分数 + 30
    Integer userWin(Integer userId);

    // 总比赛场数 + 1, 获胜场数 不变, 天梯分数 - 30
    Integer userLose(Integer userId);
}
