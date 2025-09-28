package com.qg.backend.domain.vo;


import com.qg.common.domain.po.FrontendError;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FrontendErrorVO {
    private String projectId;
    private LocalDateTime timestamp;
    private List<FrontendError> data;
}
