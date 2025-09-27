package com.qg.backend.domain.vo;

import lombok.Data;

@Data
public class PerformanceVO {
    private String page;
    private String metricType; // fcp/lcp/dom/load
    private Double metricValue;
}