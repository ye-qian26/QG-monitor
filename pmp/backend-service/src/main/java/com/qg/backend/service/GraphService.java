package com.qg.backend.service;

import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface GraphService {
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute
    (String projectId, String route, LocalDateTime startTime, LocalDateTime endTime);

    List<ErrorTrendVO> getErrorTrend
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<ManualTrackingVO> getManualTrackingStats
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    Object[] getErrorStats(String projectId);

    FrontendPerformanceAverageVO getAverageFrontendPerformanceTime
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<MethodInvocationVO> getMethodInvocationStats
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<IllegalAttackVO> getIpInterceptionCount
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<EarthVO> getForeignIpInterception
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    Result getVisits(String projectId, String timeType);

    Result getFrontendButton(String projectId);

    Result getFrontApiAverageTime(String projectId, String timeType);

    Object[] getBackendErrorStats(String projectId);

    Object[] getBackendErrorStatsPro(String projectId);

    Result getBackendApiAverageTime(String projectId, String timeType);

    Result getMobileApiAverageTime(String projectId, String timeType);

    Result getMobileOperation(String projectId, String timeType);

    Object[] getMobileErrorStats(String projectId);

    Object[] getMobileErrorStatsPro(String projectId);
}
