package com.moyun.reading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 墨韵读书平台主启动类
 * 
 * @author Moyun Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class MoyunReadingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoyunReadingApplication.class, args);
    }
}