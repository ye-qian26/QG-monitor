package com.qg.feign.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MobileErrorHandleVO {
    private Long id;
    private String projectId;

    private LocalDateTime timestamp;
    private String errorType;
    private String message;
    private String stack;
    private String className;

    private Integer event;

    private Integer isHandle;
}
