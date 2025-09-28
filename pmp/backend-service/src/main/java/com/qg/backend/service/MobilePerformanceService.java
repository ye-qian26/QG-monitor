package com.qg.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.po.MobilePerformance;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: MobilePerformanceService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
public interface MobilePerformanceService {
    Integer saveMobilePerformance(MobilePerformance mobilePerformance);


    Result selectByCondition(String projectId, String osVersion);

    Result getAverageTime(String projectId, String timeType);

    Result getMobileOperation(String projectId, String timeType);

    List<MobilePerformance> getMobilePerformanceByWrapper(LambdaQueryWrapper<MobilePerformance> wrapper);
}
