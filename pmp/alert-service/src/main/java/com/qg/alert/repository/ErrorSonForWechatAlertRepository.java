package com.qg.alert.repository;

import com.qg.feign.clients.AlertClient;
import com.qg.feign.clients.ProjectClient;
import com.qg.feign.clients.UserClient;
import com.qg.feign.repository.ErrorRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ErrorSonForWechatAlertRepository<T> extends ErrorRepository<T> {

    public ErrorSonForWechatAlertRepository(StringRedisTemplate stringRedisTemplate, ProjectClient projectClient, RestTemplateBuilder restTemplateBuilder, UserClient userClient, AlertClient alertClient) {
        super(stringRedisTemplate, projectClient, restTemplateBuilder, userClient, alertClient);
    }

    @Override
    protected boolean saveNotification(List<Long> alertReceiverID, T entity) {
        return false;
    }

    @Override
    protected boolean shouldAlert(String redisKey, T entity) {
        return false;
    }

    @Override
    protected String generateAlertMessage(T entity) {
        return "";
    }
}
