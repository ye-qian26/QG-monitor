package com.qg.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description: // 类说明
 * @ClassName: BackendServiceApplication    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/20 21:07   // 时间
 * @Version: 1.0     // 版本
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.qg.feign")
@EnableScheduling
public class BackendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendServiceApplication.class, args);
    }
}
