package com.qg.common.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.qg.common.handler.MapHandler;
import com.qg.common.handler.ListHandler;

/**
 * @Description: 前端错误Vo  // 类说明
 * @ClassName: FrontendResponsibilityVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:20   // 时间
 * @Version: 1.0     // 版本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrontendResponsibilityVO {

    private Long id;
    private String projectId;
    private LocalDateTime timestamp;
    private String sessionId;
    private String captureType;
    private Long duration;

    private String errorType;
    private String message;
    private String stack;
    @TableField(typeHandler =MapHandler.class)
    private Map<String, Object> request;
    @TableField(typeHandler = MapHandler.class)
    private Map<String, Object> response;
    @TableField(typeHandler = MapHandler.class)
    private Map<String, Object> resource;
    @TableField(typeHandler = ListHandler.class)
    private List<Map<String, Object>> breadcrumbs;
    @TableField(typeHandler = MapHandler.class)
    private Map<String, Object> tags;
    @TableField(typeHandler = MapHandler.class)
    private Map<String, Object> elementInfo;

    private String userAgent;
    private String name;
    private Long responsibleId;
    private Long delegatorId;
    private String avatarUrl;
}
