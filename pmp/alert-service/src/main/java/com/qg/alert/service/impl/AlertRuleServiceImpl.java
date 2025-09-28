package com.qg.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.qg.alert.domain.po.AlertRule;
import com.qg.alert.mapper.AlertRuleMapper;
import com.qg.alert.service.AlertRuleService;
import com.qg.common.domain.po.Code;
import com.qg.common.domain.po.Project;
import com.qg.common.domain.po.Result;
import com.qg.feign.clients.ProjectClient;
import com.qg.feign.clients.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    @Autowired
    private AlertRuleMapper alertRuleMapper;
    /*
    @Autowired
    private ProjectMapper projectMapper;
    */
    private final UserClient userClient;


    @Override
    public Result selectByType(String errorType, String env, String projectId, String platform) {
        // 参数校验
        if (isNullOrEmpty(errorType) || isNullOrEmpty(projectId) || isNullOrEmpty(platform)) {
            return new Result(Code.BAD_REQUEST, "所需参数不能为空");
        }

        try {
            LambdaQueryWrapper<AlertRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AlertRule::getErrorType, errorType.trim())
                    .eq(AlertRule::getProjectId, projectId.trim())
                    .eq(AlertRule::getPlatform, platform.trim());

            // 对于后端平台，需要处理环境参数
            if ("backend".equals(platform.trim())) {
                String envValue = isNullOrEmpty(env) ? "dev" : env.trim();
                queryWrapper.eq(AlertRule::getEnv, envValue);
                if (isNullOrEmpty(env)) {
                    log.info("未指定环境，使用默认环境: dev, projectId: {}", projectId);
                }
            }

            AlertRule alertRule = alertRuleMapper.selectOne(queryWrapper);
            if (alertRule == null) {
                log.info("未找到错误类型对应的规则: {}, projectId: {}, platform: {}", errorType, projectId, platform);
                return new Result(Code.NOT_FOUND, "未找到该错误类型");
            }

            log.debug("成功查询错误类型规则: {}, projectId: {}, platform: {}", errorType, projectId, platform);
            return new Result(Code.SUCCESS, alertRule, "查询成功");
        } catch (Exception e) {
            log.error("查询错误类型规则失败，errorType: {}, projectId: {}, platform: {}", errorType, projectId, platform, e);
            return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public Result updateThreshold(AlertRule alertRule) {

        // 参数校验
        if (alertRule == null) {
            System.out.println("AlertRule对象不能为空");
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }

        String errorType = alertRule.getErrorType();
        String projectId = alertRule.getProjectId();
        String platform = alertRule.getPlatform();

        if (isNullOrEmpty(errorType) || isNullOrEmpty(projectId) || isNullOrEmpty(platform)) {
            System.out.println("错误类型、项目ID和平台不能为空");
            return new Result(Code.BAD_REQUEST, "错误类型、项目ID和平台不能为空");
        }

        // 对于后端平台，确保环境不为空
        if ("backend".equals(platform) && isNullOrEmpty(alertRule.getEnv())) {
            alertRule.setEnv("dev");
            log.info("未指定环境，使用默认环境: dev");
            System.out.println("未指定环境，使用默认环境: dev");
        }

        try {
            // 验证项目是否存在
            /*
            Project project = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                    .eq(Project::getUuid, projectId));
            */
            Project project = userClient.getProjectById(projectId);
            if (project == null) {
                log.error("操作告警阈值失败，项目id不存在 id: {}", projectId);
                return new Result(Code.NOT_FOUND, "操作告警阈值失败，项目id不存在");
            }

            // 构建查询条件
            LambdaQueryWrapper<AlertRule> queryWrapper = buildQueryWrapper(errorType, projectId, platform, alertRule.getEnv());
            boolean exists = alertRuleMapper.selectCount(queryWrapper) > 0;

            if (exists) {
                // 更新现有记录
                LambdaUpdateWrapper<AlertRule> updateWrapper = buildUpdateWrapper(errorType, projectId, platform, alertRule.getEnv(), alertRule.getThreshold());
                alertRuleMapper.update(null, updateWrapper);
                log.debug("更新告警阈值成功: errorType={}, projectId={}, platform={}", errorType, projectId, platform);
                return new Result(Code.SUCCESS, "更新成功");
            } else {
                // 插入新记录
                alertRuleMapper.insert(alertRule);
                log.debug("创建告警阈值成功: errorType={}, projectId={}, platform={}", errorType, projectId, platform);
                return new Result(Code.SUCCESS, "创建成功");
            }
        } catch (Exception e) {
            log.error("操作阈值失败，errorType: {}, projectId: {}, platform: {}", errorType, projectId, platform, e);
            return new Result(Code.INTERNAL_ERROR, "操作失败: " + e.getMessage());
        }
    }

    @Override
    public Integer selectThresholdByProjectAndErrorType(String projectId, String errorType, String platform) {
        return alertRuleMapper.selectThresholdByProjectAndErrorType(projectId, errorType, platform);
    }

    @Override
    public HashMap<String, Integer> selectByBackendRedisKeyToMap(String projectId, String errorType, String environment) {
        return alertRuleMapper.selectByBackendRedisKeyToMap(projectId, errorType, environment);
    }

    /**
     * 检查字符串是否为空或空字符串
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<AlertRule> buildQueryWrapper(String errorType, String projectId, String platform, String env) {
        LambdaQueryWrapper<AlertRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertRule::getErrorType, errorType)
                .eq(AlertRule::getProjectId, projectId)
                .eq(AlertRule::getPlatform, platform);

        // 后端平台需要考虑环境因素
        if ("backend".equals(platform)) {
            queryWrapper.eq(AlertRule::getEnv, env);
        }

        return queryWrapper;
    }

    /**
     * 构建更新条件
     */
    private LambdaUpdateWrapper<AlertRule> buildUpdateWrapper(String errorType, String projectId, String platform, String env, Integer threshold) {
        LambdaUpdateWrapper<AlertRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AlertRule::getErrorType, errorType)
                .eq(AlertRule::getProjectId, projectId)
                .eq(AlertRule::getPlatform, platform)
                .set(AlertRule::getThreshold, threshold);

        // 后端平台需要考虑环境因素
        if ("backend".equals(platform)) {
            updateWrapper.eq(AlertRule::getEnv, env);
        }

        return updateWrapper;
    }

}
