package com.qg.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.vo.ErrorTrendVO;
import com.qg.common.domain.vo.ManualTrackingVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: FrontendErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:31   // 时间
 * @Version: 1.0     // 版本
 */
public interface FrontendErrorService {
    Result selectByCondition(String projectId, String type);

    Integer saveFrontendError(List<FrontendError> frontendErrors);

    Result addFrontendError(String errorData);


    Object[] getErrorStats(String projectId);

    Result getAverageTime(String projectId, String timeType);

    List<ErrorTrendVO> getErrorTrend
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<ManualTrackingVO> queryManualTrackingStats(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<FrontendError> getFrontendErrorByWrapper(LambdaQueryWrapper<FrontendError> queryWrapper);

    List<FrontendError> getFrontendErrorByErrorId(Long errorId);

    List<FrontendError> getFrontendErrorByErrorType(List<String> errorTypes, String projectId);
}
