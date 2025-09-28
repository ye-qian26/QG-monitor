package com.qg.feign.repository;


import com.qg.common.repository.StatisticsDataRepository;
import com.qg.feign.clients.AlertClient;
import com.qg.feign.clients.ProjectClient;
import com.qg.feign.clients.UserClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qg.common.repository.RepositoryConstants.*;


@Repository
@Slf4j
public abstract class ErrorRepository<T> extends StatisticsDataRepository<T> {

    protected final RestTemplate restTemplate;
    protected final ProjectClient projectClient;
    protected final UserClient userClient;
    protected final AlertClient alertClient;

    protected abstract boolean saveNotification(List<Long> alertReceiverID, T entity);

    protected abstract boolean shouldAlert(String redisKey, T entity);

    protected abstract String generateAlertMessage(T entity);

    @Autowired
    public ErrorRepository(StringRedisTemplate stringRedisTemplate,
                           ProjectClient projectClient,
                           RestTemplateBuilder restTemplateBuilder, UserClient userClient, AlertClient alertClient) {
        // 初始化final字段
        this.projectClient = projectClient;
        this.restTemplate = restTemplateBuilder.build();
        this.userClient = userClient;
        this.alertClient = alertClient;

        // 设置父类的依赖
        setStringRedisTemplate(stringRedisTemplate);
    }

    /**
     * 获取企业机器人webhook
     *
     * @param projectId 项目id
     * @return 结果
     */
    protected String getWebhookUrl(String projectId) {
        // 从数据库查询webhook
//        return projectClient.selectWebhookByProjectId(projectId);
        return userClient.selectWebhookByProjectId(projectId);
    }

    /**
     * 发送企业微信机器人告警
     *
     * @param webhookUrl          机器人url
     * @param message             发送的消息
     * @param mentionedMobileList 告警接收人集合
     */
    public void sendAlert(String webhookUrl, String message, List<String> mentionedMobileList) {
        try {
            // 设置请求头为JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建消息体
            Map<String, Object> requestBody = new HashMap<>();
            // 固定为text类型
            requestBody.put(MESSAGE_TYPE.getAsString(), TEXT.getAsString());

            // 设置消息内容
            Map<String, Object> textContent = new HashMap<>();
            // 消息正文
            textContent.put(CONTENT.getAsString(), message);

            // 添加告警接收人
            if (mentionedMobileList != null && !mentionedMobileList.isEmpty()) {
                textContent.put(MENTIONED_MOBILE_LIST.getAsString(), mentionedMobileList);
            }

            // 组合最终请求体
            requestBody.put(TEXT.getAsString(), textContent);

            // 发送请求
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(requestBody, headers), String.class);
        } catch (Exception e) {
            log.error("发送告警失败:{}", e.getMessage());
        }
    }

    /**
     * 从内存缓存中删除该错误
     *
     * @param entity 具体类型
     */
    protected void removeError(T entity) {
        // 1. 生成唯一键(与statistics方法一致)
        String key = generateUniqueKey(entity);

        // 2. 从Redis删除
        stringRedisTemplate.delete(key);

        // 3. 从内存缓存删除
        cacheMap.remove(key);
        log.info("缓存已被删除！");
    }

    @Override
    protected void incrementEvent(T entity) {
    }

    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES.getAsInt();
    }

    @Override
    protected void saveToDatabase(T entity) {
    }

    @Override
    protected String generateUniqueKey(T entity) {
        return "";
    }

    @Override
    public void checkIfAlert(T entity) {
    }

}