package com.srll.gamification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.srll.gamification", "com.srll.common"})
public class GamificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamificationServiceApplication.class, args);
    }
}
