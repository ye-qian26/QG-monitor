package com.qg.feign.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: // 类说明
 * @ClassName: UsersDto    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/22 11:40   // 时间
 * @Version: 1.0     // 版本
 */
@Data
public class UsersDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private LocalDateTime createdTime;
    private String phone;
}
