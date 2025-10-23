package com.qg.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.ArrayList;

/**
 * @Description: // 类说明
 * @ClassName: UserServiceApplication    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/20 21:45   // 时间
 * @Version: 1.0     // 版本
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.qg.feign.clients")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }


}
//@SpringBootApplication
//public class UserServiceApplication {
//    public static void main(String[] args) {
//        try {
//            // 先测试连接
//            testNacosConnection();
//            SpringApplication.run(UserServiceApplication.class, args);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void testNacosConnection() {
//        try {
//            Properties properties = new Properties();
//            properties.put("serverAddr", "47.113.224.195:8848");
//            properties.put("username", "nacos");
//            properties.put("password", "nacos");
//
//            ConfigService configService = NacosFactory.createConfigService(properties);
//            String content = configService.getConfig("shared-spring.yaml", "DEFAULT_GROUP", 5000);
//            System.out.println("Nacos连接测试成功，获取配置: " + (content != null ? "成功" : "空配置"));
//        } catch (Exception e) {
//            System.err.println("Nacos连接测试失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}