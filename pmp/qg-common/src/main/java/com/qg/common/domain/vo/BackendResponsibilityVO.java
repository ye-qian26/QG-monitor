package com.qg.common.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.Map;
import com.qg.common.handler.MapHandler;

/**
 * @Description: 后端错误Vo  // 类说明
 * @ClassName: BackendResponsibilityVO    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 15:19   // 时间
 * @Version: 1.0     // 版本
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackendResponsibilityVO {

    private Long id;
    private LocalDateTime timestamp;
    private String module;
    private String projectId;
    private String environment;
    private String errorType;
    private String stack;
    @TableField(value = "environment_snapshot", typeHandler = MapHandler.class)
    private Map<String, Object> environmentSnapshot;
    private String name;
    private Long delegatorId;
    private Long responsibleId;
    private String avatarUrl;


}
