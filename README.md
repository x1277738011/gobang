#五子棋
    【用户模块】
        注册
            MyBatis 来实现数据库的增删改查
            注册页面，存入MySQL时将密码加密
            引入security中的BCryptPasswordEncoder()类，
            加密：encode(password)
            验证：matches(password,user.getPassword())
            注册成功后跳转登录页面login.html
        登录
            登录页面，登录成功后进入游戏大厅game_hall.html
            
    【匹配模块】
        在游戏大厅会显示自己名字, 分数, 比赛场数和获胜场数以及一个匹配按钮
        进入游戏大厅后，建立与客户端的websocket连接
        连接成功afterConnectionEstablished：
            检查是否多开（重复登录，同时进行多场比赛）
            多开跳回登录界面
            无多开设置该用户为在线用户：存入ConcurrentHashMap中
        开始匹配
            向服务器发送'startMatch'（开始匹配的信息）
            接收信息到handleTextMessage：
            将用户添加到匹配队列
            进入匹配对列
        停止匹配
            向服务器发送'stopMatch'（停止匹配的信息）
            接收信息到handleTextMessag（该方法会判断接收到的信息停止还是开始）：
            将用户移出匹配队列
            退出匹配队列
        匹配成功
            则进入比赛房间，此时客户端与服务器开启一个新的websocket连接
            进入游戏房间
    【对战模块】
        准备就绪: 
            两个玩家均连上游戏房间的 websocket 时, 则认为双方准备就绪
        落子：
            有一方玩家落子时, 会通过 websocket 给服务器发送落子的用户信息和落子位置,
            同时服务器再将这样的信息返回给房间内的双方客户端. 
            然后客户端根据服务器的响应来绘制棋子位置
        判定胜负：
            服务器判定这一局游戏的胜负关系. 
            如果某一方玩家落子,产生了五子连珠,
            则判定胜负并返回胜负信息.或者如果某一方玩家掉线(比如关闭页面), 
            也会判定对方获胜.
