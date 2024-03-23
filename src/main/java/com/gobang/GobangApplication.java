package com.gobang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan(basePackages = "com.gobang.mapper")
public class GobangApplication {
    public static ConfigurableApplicationContext context;
    public static void main(String[] args) {
        context = SpringApplication.run(GobangApplication.class, args);
    }

}
