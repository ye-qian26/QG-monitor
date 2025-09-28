package com.qg.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Result;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: MobileErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
public interface MobileErrorService {
    Result selectByCondition(String projectId, String type);

    void receiveErrorFromSDK(String mobileErrorJSON);

    Object[] getMobileErrorStats(String projectId);

    Object[] getMobileErrorStatsPro(String projectId);

    List<MobileError> getMobileErrorByWrapper(LambdaQueryWrapper<MobileError> wrapper);

    List<MobileError> getMobileErrorByErrorId(Long errorId);

    List<MobileError> getMobileErrorByErrorType(List<String> errorTypes, String projectId);
}
