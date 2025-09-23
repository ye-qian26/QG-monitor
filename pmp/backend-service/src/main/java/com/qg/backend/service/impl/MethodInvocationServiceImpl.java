package com.qg.backend.service.impl;


import com.qg.backend.mapper.MethodInvocationMapper;
import com.qg.backend.repository.MethodInvocationRepository;
import com.qg.backend.service.MethodInvocationService;
import com.qg.common.domain.vo.MethodInvocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MethodInvocationServiceImpl implements MethodInvocationService {

    @Autowired
    private MethodInvocationMapper methodInvocationMapper;

    private final MethodInvocationRepository methodInvocationRepository;

    @Autowired
    public MethodInvocationServiceImpl(MethodInvocationRepository methodInvocationRepository) {
        this.methodInvocationRepository = methodInvocationRepository;
    }

    @Override
    public void statisticsMethod(Map<String, Integer> methodMap, String projectId) {
        try {
            Map<String, Integer> processedMap = methodMap.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toMap(
                            entry -> projectId + ":" + entry.getKey(),
                            Map.Entry::getValue
                    ));

            methodInvocationRepository.statisticsMethod(processedMap);
            log.info("成功统计{}个方法的调用情况,项目ID: {}", processedMap.size(), projectId);
        } catch (Exception e) {
            log.error("方法调用统计失败,项目ID: {}: {}", projectId, e.getMessage(), e);
        }
    }

    /**
     * 获取后端方法调用统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @Override
    public List<MethodInvocationVO> queryMethodInvocationStats
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return methodInvocationMapper.queryMethodInvocationStats(projectId, startTime, endTime);
    }

}