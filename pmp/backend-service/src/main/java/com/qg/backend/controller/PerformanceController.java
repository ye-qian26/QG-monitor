package com.qg.backend.controller;


import com.qg.backend.service.AllPerformanceService;
import com.qg.common.domain.po.Result;
import com.qg.feign.clients.BackendClient;
import com.qg.feign.clients.FrontendClient;
import com.qg.feign.clients.MobileClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "性能检测")
@RestController
@RequestMapping("/performances")
@AllArgsConstructor
public class PerformanceController {

    private final BackendClient backendClient;
    private final FrontendClient frontendClient;
    private final MobileClient mobileClient;

    @Autowired
    private AllPerformanceService allPerformanceService;


    @GetMapping("/selectByCondition")
    public Result selectByCondition(@RequestParam String platform, @RequestParam String projectId,
                                    @RequestParam(required = false) String api,
                                    @RequestParam(required = false) String captureType,
                                    @RequestParam(required = false) String osVersion) {
        switch (platform) {
            case "backend":
                return backendClient.selectBackendPerformanceByCondition(projectId, api);
            case "frontend":
                return backendClient.selectFrontPerformanceByCondition(projectId, captureType);
            case "mobile":
                return backendClient.selectMobilePerformanceByCondition(projectId, osVersion);
            case "all":
                return allPerformanceService.selectByCondition(projectId, api, captureType, osVersion);
            default:
                return new Result(400, "平台参数错误");
        }
    }

}
