package com.qg.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.qg.backend.mapper.BackendPerformanceMapper;
import com.qg.backend.mapper.FrontendPerformanceMapper;
import com.qg.backend.mapper.MobilePerformanceMapper;
import com.qg.common.domain.po.BackendPerformance;
import com.qg.common.domain.po.FrontendPerformance;
import com.qg.common.domain.po.MobilePerformance;
import com.qg.common.domain.po.Result;
import com.qg.feign.clients.BackendClient;
import com.qg.feign.clients.FrontendClient;
import com.qg.feign.clients.MobileClient;
import com.qg.backend.service.AllPerformanceService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: AllPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 11:28   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@AllArgsConstructor
public class AllPerformanceServiceImpl implements AllPerformanceService {

    //    private final BackendClient backendClient;
    //    private final FrontendClient frontendClient;
    //    private final MobileClient mobileClient;
    @Autowired
    private BackendPerformanceMapper backendPerformanceMapper;
    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;
    @Autowired
    private MobilePerformanceMapper mobilePerformanceMapper;

    @Override
    public Result selectByCondition(String projectId, String api, String capture, String osVersion) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }

        LambdaQueryWrapper<BackendPerformance> BackendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendPerformance> FrontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobilePerformance> MobileQueryWrapper = new LambdaQueryWrapper<>();

        BackendQueryWrapper.eq(BackendPerformance::getProjectId, projectId);
        FrontendQueryWrapper.eq(FrontendPerformance::getProjectId, projectId);
        MobileQueryWrapper.eq(MobilePerformance::getProjectId, projectId);

        if (api != null && !api.isEmpty()) {
            BackendQueryWrapper.like(BackendPerformance::getApi, api);
        }

//        List<BackendPerformance> backendPerformances = backendClient.getBackendPerformanceByWrapper(BackendQueryWrapper);
        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(BackendQueryWrapper);

        if (capture != null && !capture.isEmpty()) {
            FrontendQueryWrapper.eq(FrontendPerformance::getCaptureType, capture);
        }

//        List<FrontendPerformance> frontendPerformances = frontendClient.getFrontendPerformanceByWrapper(FrontendQueryWrapper);
        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(FrontendQueryWrapper);

        if (osVersion != null && !osVersion.isEmpty()) {
            MobileQueryWrapper.like(MobilePerformance::getOsVersion, osVersion);
        }

//        List<MobilePerformance> mobilePerformances = mobileClient.getMobilePerformanceByWrapper(MobileQueryWrapper);
        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(MobileQueryWrapper);

        return new Result(200, List.of(backendPerformances, frontendPerformances, mobilePerformances), "查询成功");
    }
}
