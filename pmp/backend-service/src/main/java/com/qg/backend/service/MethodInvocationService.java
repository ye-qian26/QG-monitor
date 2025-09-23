package com.qg.backend.service;

import com.qg.common.domain.vo.MethodInvocationVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Transactional
public interface MethodInvocationService {
    void statisticsMethod(Map<String, Integer> methodMap, String projectId);

    List<MethodInvocationVO> queryMethodInvocationStats
    (String projectId, LocalDateTime startTime, LocalDateTime endTime);
}
