package com.gobang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gobang.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名,来查询用户的详细信息,用于登录功能
     * @param username
     * @return
     */
    @Select("select * from user where username=#{username}")
    User selectByName(String username);

    /**
     * 总比赛场数 + 1, 获胜场数 + 1, 天梯分数 + 30
     * @param userId
     * @return
     */
    @Update("update user set totalcount=totalcount+1,wincount=wincount+1,score=score+30 where userId=#{userId}")
    Integer userWin(Integer userId);

    /**
     * 总比赛场数 + 1, 获胜场数 不变, 天梯分数 - 30
     * @param userId
     * @return
     */
    @Update("update user set totalcount=totalcount+1,score=score-30 where userId=#{userId}")
    Integer userLose(Integer userId);
}
