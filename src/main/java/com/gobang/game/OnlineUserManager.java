package com.gobang.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManager {

    private ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();

    //进入游戏大厅
    public void enterGameHall(int userId,WebSocketSession session){
        gameHall.put(userId,session);
    }
    //只有当前页面退出的时候,能销毁自己的 session
    //避免当一个 userId 打开两次 游戏页面,错误的删掉之前的会话的问题
    //退出游戏大厅
    public void exitGameHall(int userId){
        gameHall.remove(userId);
    }
    //在游戏大厅中，获取当前WebSocketSession中的userid
    public WebSocketSession getSessionFromGameHall(int userId){
        return gameHall.get(userId);
    }
    //进入游戏房间
    public void enterGameRoom(int userId,WebSocketSession session){
        gameRoom.put(userId,session);
    }
    //退出游戏房间
    public void exitGameRoom(int userId){
        gameRoom.remove(userId);
    }
    //在游戏房间中，获取当前WebSocketSession中的userid
    public WebSocketSession getSessionFromGameRoom(int userId){
        return gameRoom.get(userId);
    }

}
