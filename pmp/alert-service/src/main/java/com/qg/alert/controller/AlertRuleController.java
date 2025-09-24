package com.qg.alert.controller;


import com.qg.alert.domain.po.AlertRule;
import com.qg.alert.service.AlertRuleService;
import com.qg.common.domain.po.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@Tag(name ="告警规则")
@RestController
@RequestMapping("/alertRules")
public class AlertRuleController {

    @Autowired
    private AlertRuleService alertRuleService;

    /**
     * 根据错误类型查询报警规则
     * @param errorType
     * @return
     */
    @GetMapping("/selectByTypeEnvProjectId")
    public Result selectByType(@RequestParam String errorType, @RequestParam(required = false) String env,
                               @RequestParam String projectId, @RequestParam String platform) {
        return alertRuleService.selectByType(errorType, env, projectId, platform);
    }

    /**
     * 修改报警阈值（添加与修改）
     * @param alertRule
     * @return
     */
    @PutMapping("/updateThreshold")
    public Result updateThreshold(@RequestBody AlertRule alertRule) {
        System.out.println("Received AlertRule: " + alertRule);
        return alertRuleService.updateThreshold(alertRule);
    }

    @GetMapping("/selectThresholdByProjectAndErrorType")
    public Integer selectThresholdByProjectAndErrorType(@RequestParam String projectId,
                                                        @RequestParam String errorType,
                                                        @RequestParam String platform) {
        return alertRuleService.selectThresholdByProjectAndErrorType(projectId, errorType, platform);
    }

    @GetMapping("/selectByBackendRedisKeyToMap")
    public HashMap<String, Integer> selectByBackendRedisKeyToMap(@RequestParam String projectId,
                                                        @RequestParam String errorType,
                                                        @RequestParam String environment) {
        return alertRuleService.selectByBackendRedisKeyToMap(projectId, errorType, environment);
    }

}
