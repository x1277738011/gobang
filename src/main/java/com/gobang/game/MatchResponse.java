package com.gobang.game;

import lombok.Data;

/**
 * 这是表示一个 websocket 的匹配响应
 */
@Data
public class MatchResponse {
    private boolean ok;
    private String reason;
    private String message;
}
