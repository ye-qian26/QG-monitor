package com.qg.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.BackendError;
import com.qg.common.domain.po.Result;

import java.util.List;


/**
 * @Description: // 类说明
 * @ClassName: BackendErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:30   // 时间
 * @Version: 1.0     // 版本
 */
public interface BackendErrorService {
    Result selectByCondition(String projectId, Long moduleId, String type);

    Integer saveBackendError(BackendError backendError);

    Result addBackendError(String errorData);

    Object[] getBackendErrorStats(String projectId);

    Object[] getBackendErrorStatsPro(String projectId);

    List<BackendError> getBackendErrorByWrapper(LambdaQueryWrapper<BackendError> queryWrapper);

    List<BackendError> getBackendErrorByErrorId(Long errorId);

    List<BackendError> getBackendErrorByErrorType(List<String> errorTypes, String projectId);
}
