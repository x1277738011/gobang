package com.gobang.config;

import com.gobang.api.GameAPI;
import com.gobang.api.MatchAPI;
import com.gobang.api.TestAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket//这个注释可以让Spring知道这是一个Websocket配置
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private TestAPI testAPI;
    @Autowired
    private MatchAPI matchAPI;
    @Autowired
    private GameAPI gameAPI;
    @Bean
    public BCryptPasswordEncoder getBCryptPasswordEncoder() { return new BCryptPasswordEncoder(); }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //通过 .addInterceptors(new HttpSessionHandshakeInterceptor() 这个操作来把 HttpSession 里的属性放到 WebSocket 的 session 中
        //然后就可以在 WebSocket 代码中 WebSocketSession 里拿到 HttpSession 中的 attribute.
        registry.addHandler(testAPI,"/test");
        registry.addHandler(matchAPI,"/findMatch")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        registry.addHandler(gameAPI,"/game")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
