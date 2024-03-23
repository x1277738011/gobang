package com.gobang.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gobang.GobangApplication;
import com.gobang.model.User;
import com.gobang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

public class Room {
    // 使用字符串类型来表示，方便生成唯一值
    private String roomId;

    private User user1;
    private User user2;
    //先手玩家id
    private int whitUser;

    private static final int MAX_ROW = 15;
    private static final int MAX_COL = 15;
    // 这个二维数组用来表示棋盘
    // 约定:
    // 1) 使用 0 表示当前位置未落子. 初始化好的 int 二维数组, 就相当于是 全 0
    // 2) 使用 1 表示 user1 的落子位置
    // 3) 使用 2 表示 user2 的落子位置
    private int[][] board = new int[MAX_ROW][MAX_COL];
    // 创建 ObjectMapper 用来转换 JSON
    private ObjectMapper objectMapper = new ObjectMapper();

    // 引入 OnlineUserManager
    private OnlineUserManager onlineUserManager;
    // 引入 RoomManager, 用于房间销毁
    private RoomManager roomManager;

    private UserService userService;

    // 通过这个方法来处理一次落子操作.
    // 要做的事情:
    public void putChess(String reqJson) throws IOException {
        // 1. 记录当前落子的位置.
        GameRequest request = objectMapper.readValue(reqJson,GameRequest.class);
        GameResponse response = new GameResponse();
        // 当前这个子是玩家1 落的还是玩家2 落的. 根据这个玩家1 和 玩家2 来决定往数组中是写 1 还是 2
        int chess = request.getUserId() == user1.getUserId() ? 1 : 2;
        int row = request.getRow();
        int col = request.getCol();
        if (board[row][col] != 0) {
            // 在客户端已经针对重复落子进行过判定了. 此处为了程序更加稳健, 在服务器再判定一次.
            System.out.println("当前位置 (" + row + ", " + col + ") 已经有子了!");
            return;
        }
        board[row][col] = chess;
        // 2. 打印出当前的棋盘信息, 方便来观察局势. 也方便后面验证胜负关系的判定.
        printBoard();
        // 3. 进行胜负判定
        int winner = checkWinner(row, col, chess);
        // 4. 给房间中的所有客户端都返回响应.
        response.setMessage("putChess");
        response.setUserId(request.getUserId());
        response.setRow(row);
        response.setCol(col);
        response.setWinner(winner);
        // 要想给用户发送 websocket 数据, 就需要获取到这个用户的 WebSocketSession
        WebSocketSession session1 = onlineUserManager.getSessionFromGameRoom(user1.getUserId());
        WebSocketSession session2 = onlineUserManager.getSessionFromGameRoom(user2.getUserId());
        // 万一当前查到的会话为空(玩家已经下线了) 特殊处理一下
        if (session1 == null) {
            // 玩家1 已经下线了. 直接认为玩家2 获胜!
            response.setWinner(user2.getUserId());
            System.out.println("玩家1 掉线!");
        }
        if (session2 == null) {
            // 玩家2 已经下线. 直接认为玩家1 获胜!
            response.setWinner(user1.getUserId());
            System.out.println("玩家2 掉线!");
        }
        // 把响应构造成 JSON 字符串, 通过 session 进行传输.
        String respJson = objectMapper.writeValueAsString(response);
        if (session1 != null) {
            session1.sendMessage(new TextMessage(respJson));
        }
        if (session2 != null) {
            session2.sendMessage(new TextMessage(respJson));
        }
        // 5. 如果当前胜负已分, 此时这个房间就失去存在的意义了. 就可以直接销毁房间. (把房间从房间管理器中给移除)
        if (response.getWinner() != 0) {
            // 胜负已分
            System.out.println("游戏结束! 房间即将销毁! roomId=" + roomId + " 获胜方为: " + response.getWinner());
            // 更新获胜方和失败方的信息.
            int winUserId = response.getWinner();
            int loseUserId = response.getWinner() == user1.getUserId() ? user2.getUserId() : user1.getUserId();
            userService.userWin(winUserId);
            userService.userLose(loseUserId);
            // 销毁房间
            roomManager.remove(roomId, user1.getUserId(), user2.getUserId());
        }
    }
    private void printBoard() {
        // 打印出棋盘
        System.out.println("[打印棋盘信息] " + roomId);
        System.out.println("=====================================================================");
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                // 针对一行之内的若干列, 不要打印换行
                System.out.print(board[r][c] + " ");
            }
            // 每次遍历完一行之后, 再打印换行.
            System.out.println();
        }
        System.out.println("=====================================================================");
    }

    /**
     *    使用这个方法来判定当前落子是否分出胜负.
     *    约定如果玩家1 获胜, 就返回玩家1 的 userId
     *    如果玩家2 获胜, 就返回玩家2 的 userId
     *    如果胜负未分, 就返回 0
     * @param row
     * @param col
     * @param chess
     * @return
     */
    private int checkWinner(int row,int col,int chess){
        for (int c = col - 4 ;c <= col ; c++ ){
            try {
                if (board[row][c] == chess
                        && board[row][c + 1] == chess
                        && board[row][c + 2] == chess
                        && board[row][c + 3] == chess
                        && board[row][c + 4] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 2. 检查所有列
        for (int r = row - 4 ;r <= row ; r++ ){
            try {
                if (board[r][col] == chess
                        && board[r+1][col] == chess
                        && board[r+2][col] == chess
                        && board[r+3][col] == chess
                        && board[r+4][col] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 3. 检查左对角线
        for (int r = row - 4, c = col - 4; r <= row && c <= col; r++, c++ ){
            try {
                if (board[r][c] == chess
                        && board[r + 1][c + 1] == chess
                        && board[r + 2][c + 2] == chess
                        && board[r + 3][c + 3] == chess
                        && board[r + 4][c + 4] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 4. 检查右对角线
        for (int r = row - 4, c = col + 4; r <= row && c >= col; r++, c-- ){
            try {
                if (board[r][c] == chess
                        && board[r + 1][c - 1] == chess
                        && board[r + 2][c - 2] == chess
                        && board[r + 3][c - 3] == chess
                        && board[r + 4][c - 4] == chess) {
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 胜负未分, 就直接返回 0 了.
        return 0;
    }


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public int getWhitUser() {
        return whitUser;
    }

    public void setWhitUser(int whitUser) {
        this.whitUser = whitUser;
    }

    public Room() {
        // 构造 Room 的时候生成一个唯一的字符串表示房间 id.
        // 使用 UUID 来作为房间 id
        roomId = UUID.randomUUID().toString();
        // 通过入口类中记录的 context 来手动获取到前面的 RoomManager 和 OnlineUserManager
        onlineUserManager = GobangApplication.context.getBean(OnlineUserManager.class);
        roomManager = GobangApplication.context.getBean(RoomManager.class);
        userService = GobangApplication.context.getBean(UserService.class);
    }

    public static void main(String[] args) {
        Room room = new Room();
        System.out.println(room.roomId);
    }
}
