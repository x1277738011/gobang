package com.gobang.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobang.game.*;
import com.gobang.model.User;
import com.gobang.service.UserService;
import com.gobang.tools.AppVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
@Component
public class GameAPI extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Resource
    private UserService userService;

    /**
     * 玩家进入房间
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        GameReadyResponse resp = new GameReadyResponse();

        // 1. 先获取到用户的身份信息. (从 HttpSession 里拿到当前用户的对象)
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            resp.setOk(false);
            resp.setReason("用户未登录");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }
        // 2. 判定当前用户是否已经进入房间. (拿着房间管理器进行查询)
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            // 如果为 null, 当前没有找到对应的房间. 该玩家还没有匹配到.
            resp.setOk(false);
            resp.setReason("用户尚未匹配到!");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }
        // 3. 判定当前是不是多开 (该用户是不是已经在其他地方进入游戏了)
        //    前面准备了一个 OnlineUserManager
        if (onlineUserManager.getSessionFromGameHall(user.getUserId()) != null
                || onlineUserManager.getSessionFromGameRoom(user.getUserId()) !=null) {
            resp.setOk(false);
            resp.setReason("禁止多开");
            resp.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }
        // 4. 设置当前玩家上线!
        onlineUserManager.enterGameRoom(user.getUserId(), session);
        System.out.println("玩家 " + user.getUsername() + " 进入游戏房间!");

        // 5. 把两个玩家加入到游戏房间中.
        //    前面的创建房间/匹配过程, 是在 game_hall.html 页面中完成的.
        //    因此前面匹配到对手之后, 需要经过页面跳转, 来到 game_room.html 才算正式进入游戏房间(才算玩家准备就绪)
        //    当前这个逻辑是在 game_room.html 页面加载的时候进行的.
        //    执行到当前逻辑, 说明玩家已经页面跳转成功了!!
        //    页面跳转, 其实是个大活~~ (很有可能出现 "失败" 的情况的)
        synchronized (room) {
            if (room.getUser1() == null){
                // 第一个玩家还尚未加入房间.
                // 就把当前连上 websocket 的玩家作为 user1, 加入到房间中.
                room.setUser1(user);
                // 把先连入房间的玩家作为先手方.
                room.setWhitUser(user.getUserId());
                System.out.println("玩家 " + user.getUsername() + " 已经准备就绪! 作为玩家1");
                return;
            }
            if (room.getUser2() == null){
                // 如果进入到这个逻辑, 说明玩家1 已经加入房间, 现在要给当前玩家作为玩家2 了
                room.setUser2(user);
                System.out.println("玩家 " + user.getUsername() + " 已经准备就绪! 作为玩家2");
                // 当两个玩家都加入成功之后, 就要让服务器, 给这两个玩家都返回 websocket 的响应数据.
                // 通知这两个玩家说, 游戏双方都已经准备好了.
                // 通知玩家1,2
                noticeGameReady(room, room.getUser1(), room.getUser2());
                noticeGameReady(room, room.getUser2(), room.getUser1());
                return;

            }
            // 6. 此处如果又有玩家尝试连接同一个房间, 就提示报错.
            //    这种情况理论上是不存在的, 为了让程序更加的健壮, 还是做一个判定和提示.
            resp.setOk(false);
            resp.setReason("当前房间已满, 您不能加入房间");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        }


    }

    /**
     * 把当前的响应数据传回给玩家.
     * @param room
     * @param thisUser
     * @param thatUser
     * @throws IOException
     */
    private void noticeGameReady(Room room,User thisUser,User thatUser) throws IOException {
        GameReadyResponse resp = new GameReadyResponse();
        resp.setMessage("gameReady");
        resp.setOk(true);
        resp.setReason("");
        resp.setRoomId(room.getRoomId());
        resp.setThisUserId(thisUser.getUserId());
        resp.setThatUserId(thatUser.getUserId());
        resp.setWhiteUser(room.getWhitUser());
        WebSocketSession webSocketSession = onlineUserManager.getSessionFromGameRoom(thisUser.getUserId());
        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    /**
     * 在对应房间里执行落子
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 1. 先从 session 里拿到当前用户的身份信息
        User user = (User) session.getAttributes().get(AppVariable.USER_SESSION_KEY);
        if (user == null) {
            System.out.println("[handleTextMessage] 当前玩家尚未登录! ");
            return;
        }
        // 2. 根据玩家 id 获取到房间对象
        Room room = roomManager.getRoomByUserId(user.getUserId());
        // 3. 通过 room 对象来处理这次具体的请求
        room.putChess(message.getPayload());
    }

    /**
     * 处理一方的连接异常
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get(AppVariable.USER_SESSION_KEY);
        if (user == null) {
            // 此处就简单处理, 在断开连接的时候就不给客户端返回响应了.
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getSessionFromGameRoom(user.getUserId());
        if (session != exitSession) {
            // 加上这个判定, 目的是为了避免在多开的情况下, 第二个用户退出连接动作, 导致第一个用户的会话被删除.
            System.out.println("当前会话不是游戏中玩家的会话");
            return;
        }
        System.out.println("当前用户 " + user.getUsername() + " 游戏房间连接异常!");
        onlineUserManager.exitGameRoom(user.getUserId());
        // 通知对手获胜了
        noticeThatUserWin(user);
    }

    /**
     * 当用户突然离开游戏房间
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            // 此处就简单处理, 在断开连接的时候就不给客户端返回响应了.
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getSessionFromGameRoom(user.getUserId());
        if (session != exitSession) {
            // 加上这个判定, 目的是为了避免在多开的情况下, 第二个用户退出连接动作, 导致第一个用户的会话被删除.
            System.out.println("当前会话不是游戏中玩家的会话");
            return;
        }
        System.out.println("当前用户 " + user.getUsername() + " 离开游戏房间!");
        onlineUserManager.exitGameRoom(user.getUserId());
        // 通知对手获胜了
        noticeThatUserWin(user);
    }

    private void noticeThatUserWin(User user) throws IOException {
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            // 这个情况意味着房间已经被释放了, 也就没有 "对手" 了
            System.out.println("当前房间已经释放, 无需通知对手!");
            return;
        }
        // 2. 根据房间找到对手
        User thatUser = (user == room.getUser1()) ? room.getUser2() : room.getUser1();
        // 3. 找到对手的在线状态
        WebSocketSession webSocketSession = onlineUserManager.getSessionFromGameRoom(thatUser.getUserId());
        if (webSocketSession == null) {
            // 这就意味着对手也掉线了!
            System.out.println("对手也已经掉线了, 无需通知!");
            return;
        }
        // 4. 构造一个响应, 来通知对手, 你是获胜方
        GameResponse response = new GameResponse();
        response.setMessage("pustChess");
        response.setUserId(thatUser.getUserId());
        response.setWinner(thatUser.getUserId());
        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        // 5. 更新玩家的分数信息
        int winUserId = thatUser.getUserId();
        int loseUserId = user.getUserId();
        userService.userWin(winUserId);
        userService.userLose(loseUserId);
        // 6. 释放房间对象
        roomManager.remove(room.getRoomId(),room.getUser1().getUserId(),room.getUser2().getUserId());
    }
}
