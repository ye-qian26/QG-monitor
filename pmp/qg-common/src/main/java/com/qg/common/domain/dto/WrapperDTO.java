package com.qg.common.domain.dto;

import lombok.Data;

@Data
public class WrapperDTO<T> {
    /**
     * 存储 LambdaQueryWrapper 的 JSON 字符串
     */
    private String wrapperJson;
}