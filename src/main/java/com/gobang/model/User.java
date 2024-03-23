package com.gobang.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class User {
    @TableId(value = "userId", type = IdType.INPUT)
    private Integer userId;
    private String username;
    private String password;
    private Integer score;
    private Integer totalcount;
    private Integer wincount;
}
