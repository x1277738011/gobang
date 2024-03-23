package com.gobang.game;

import lombok.Data;

/**
 * 落子响应
 */
@Data
public class GameResponse {
    private String message;
    private int userId;
    private int row;
    private int col;
    private int winner;
}
