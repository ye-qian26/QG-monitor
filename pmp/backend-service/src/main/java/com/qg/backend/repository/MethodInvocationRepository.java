package com.qg.backend.repository;

import com.qg.backend.mapper.MethodInvocationMapper;
import com.qg.common.domain.dto.MethodInvocationDTO;
import com.qg.backend.domain.po.MethodInvocation;
import com.qg.common.domain.po.BackendError;
import com.qg.common.repository.RepositoryConstants;
import com.qg.common.repository.StatisticsDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class MethodInvocationRepository extends StatisticsDataRepository<MethodInvocation> {

    @Autowired
    private MethodInvocationMapper methodInvocationMapper;

    /**
     * 统计方法调用
     *
     * @param methodMap 要统计的方法
     */
    public void statisticsMethod(Map<String, Integer> methodMap) {
        // 批量更新Redis
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            methodMap.forEach((fullKey, count) -> {
                String redisKey = RepositoryConstants.REDIS_KEY_PREFIX.getAsString() + fullKey;
                connection.stringCommands().incrBy(redisKey.getBytes(), count);
                // 设置过期时间
                connection.keyCommands().expire(redisKey.getBytes(), getTtlMinutes());
            });
            return null;
        });

        // 批量更新内存缓存
        methodMap.forEach((fullKey, count) -> {
            String[] parts = fullKey.split(":", 2);
            String projectId = parts[0];
            String methodName = parts[1];

            cacheMap.compute(fullKey, (k, existing) -> {
                if (existing == null) {
                    MethodInvocation newEntity = new MethodInvocation();
                    newEntity.setMethodName(methodName);
                    newEntity.setProjectId(projectId);
                    newEntity.incrementEvent(count);
                    return newEntity;
                }
                existing.incrementEvent(count);
                return existing;
            });
        });
    }

    @Override
    protected long getTtlMinutes() {
        return RepositoryConstants.TTL_MINUTES.getAsLong();
    }

    @Override
    protected void saveToDatabase(MethodInvocation entity) {
        try {
            MethodInvocationDTO methodInvocationDTO = convertToVO(entity);
            methodInvocationMapper.insert(methodInvocationDTO);
        } catch (Exception e) {
            log.error("方法调用统计失败,项目ID: {}: {}", entity.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(MethodInvocation entity) {
        return RepositoryConstants.REDIS_KEY_PREFIX.getAsString() + entity.getProjectId() + ":" + entity.getMethodName();
    }

    @Override
    protected void incrementEvent(MethodInvocation entity) {
    }

    @Override
    public void checkIfAlert(MethodInvocation entity) {
    }

    /**
     * 重写父类逻辑
     */
    @Override
    @Scheduled(fixedRate = RepositoryConstants.FIXED_RATE_METHOD)
    public void scheduleSaveToDatabase() {
        cacheMap.forEach((fullKey, entity) -> {
            String redisKey = RepositoryConstants.REDIS_KEY_PREFIX.getAsString() + fullKey;
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                // Redis已过期，保存到数据库并清理缓存
                saveToDatabase(entity);
                cacheMap.remove(fullKey);
            }
        });
    }

    @Override
    protected boolean saveNotification(List<Long> alertReceiverID, BackendError error) {
        return false;
    }

    /**
     * 将实体转换为VO
     *
     * @param entity
     * @return
     */
    private MethodInvocationDTO convertToVO(MethodInvocation entity) {
        MethodInvocationDTO vo = new MethodInvocationDTO();
        vo.setProjectId(entity.getProjectId());
        vo.setMethodName(entity.getMethodName());
        vo.setEvent(entity.getEventCount());
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }
}