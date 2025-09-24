package com.qg.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Description: // 类说明
 * @ClassName: FrontendServiceApplication    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/20 21:37   // 时间
 * @Version: 1.0     // 版本
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.qg.feign")
public class AlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
    }
}
