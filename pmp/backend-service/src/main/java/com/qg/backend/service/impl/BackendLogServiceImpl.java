package com.qg.backend.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.qg.backend.domain.po.BackendLog;
import com.qg.backend.mapper.BackendLogMapper;
import com.qg.backend.repository.BackendLogRepository;
import com.qg.backend.service.BackendLogService;
import com.qg.backend.service.ModuleService;

import com.qg.common.domain.vo.EarthVO;
import com.qg.common.domain.vo.IllegalAttackVO;
import com.qg.feign.clients.ProjectClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 后端日志应用  // 类说明
 * @ClassName: BackendLogServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:33   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackendLogServiceImpl implements BackendLogService {

    @Autowired
    private BackendLogRepository backendLogRepository;
    @Autowired
    private BackendLogMapper backendLogMapper;
    @Autowired
    private ModuleService moduleService;
    /*    @Autowired
        private ProjectService projectService;*/
    private final ProjectClient projectClient;


    @Override
    public List<BackendLog> getAllLogs(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return List.of(); // 返回空列表表示没有日志数据
        }
        LambdaQueryWrapper<BackendLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendLog::getProjectId, projectId);

        return backendLogMapper.selectList(queryWrapper);

    }

    /**
     * 获取后端SDK发送的日志
     *
     * @param logJSON
     * @return
     */
    @Override
    public void receiveLogFromSDK(String logJSON) {
        // 转换数据，进行缓存交互
        try {
            List<BackendLog> logs = JSONUtil.toList(logJSON, BackendLog.class);

            // 检验项目id是否存在
            String projectId = logs.getFirst().getProjectId();
            /*if (!projectService.checkProjectIdExist(projectId)) {
                log.warn("项目ID不存在:{}", projectId);
                return;
            }*/
            if (!projectClient.checkProjectIdExist(projectId)) {
                log.warn("项目ID不存在:{}", projectId);
                return;
            }
            logs.forEach(log -> {
                moduleService.putModuleIfAbsent(log.getModule(), log.getProjectId());
                backendLogRepository.statistics(log);
            });
            log.info("backend-info-log存入缓存成功");
        } catch (Exception e) {
            log.warn("backend-info-log存入缓存失败:{}", e.getMessage());
        }
    }


    @Override
    public List<BackendLog> getLogsByCondition(String evn, String logLevel, String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return List.of(); // 返回空列表表示没有日志数据
        }
        LambdaQueryWrapper<BackendLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendLog::getProjectId, projectId)
                .orderByDesc(BackendLog::getTimestamp);
        if (evn != null && !evn.isEmpty()) {
            queryWrapper.eq(BackendLog::getEnvironment, evn);
        }
        if (logLevel != null && !logLevel.isEmpty()) {
            queryWrapper.eq(BackendLog::getLevel, logLevel);
        }

        queryWrapper.orderByDesc(BackendLog::getTimestamp); // 按创建时间降序排列

        return backendLogMapper.selectList(queryWrapper);
    }

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @Override
    public List<IllegalAttackVO> queryIpInterceptionCount(
            String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return backendLogMapper.queryIpInterceptionCount(projectId, startTime, endTime);
    }

    /**
     * 查询指定时间段内所有境外访问的IP的拦截次数统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @Override
    public List<EarthVO> queryForeignIpInterceptions(
            String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return backendLogMapper.queryForeignIpInterceptions(projectId, startTime, endTime);
    }
}
