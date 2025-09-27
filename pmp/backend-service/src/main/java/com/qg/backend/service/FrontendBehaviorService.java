package com.qg.backend.service;


import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.FrontendBehaviorVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: FrontendBehaviorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:31   // 时间
 * @Version: 1.0     // 版本
 */
public interface FrontendBehaviorService {
    Result saveFrontendBehavior(String data);


    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute
            (String projectId, String route, LocalDateTime startTime, LocalDateTime endTime);


    Result getFrontendButton(String projectId);
}
