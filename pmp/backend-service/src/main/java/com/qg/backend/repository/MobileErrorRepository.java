package com.qg.backend.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.backend.mapper.MobileErrorMapper;
import com.qg.common.domain.po.*;
import com.qg.feign.repository.ErrorRepository;
import com.qg.common.repository.RepositoryConstants;
import com.qg.feign.clients.AlertClient;
import com.qg.feign.clients.ProjectClient;
import com.qg.feign.clients.UserClient;
import com.qg.feign.domain.dto.UsersDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.qg.common.repository.RepositoryConstants.*;
import static com.qg.common.utils.Constants.ALERT_CONTENT_NEW;
import static com.qg.common.utils.Constants.USER_ROLE_ADMIN;


@Slf4j
@Repository
public class MobileErrorRepository extends ErrorRepository<MobileError> {

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    public MobileErrorRepository(StringRedisTemplate stringRedisTemplate, ProjectClient projectClient, RestTemplateBuilder restTemplateBuilder, UserClient userClient, AlertClient alertClient) {
        super(stringRedisTemplate, projectClient, restTemplateBuilder, userClient, alertClient);
    }

    @Override
    protected void saveToDatabase(MobileError error) {
        try {
            System.err.println("存储错误数据: " + error + this.getClass().getSimpleName());
            mobileErrorMapper.insert(error);
        } catch (Exception e) {
            log.error("移动端错误统计失败,项目ID: {}: {}", error.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(MobileError error) {
        return String.format("%s:%s:%s:%s",
                MOBILE_ERROR_PREFIX.getAsString(),
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName()
        );
    }

    /**
     * 发送消息模板
     *
     * @param error 移动端错误
     * @return 结果
     */
    @Override
    protected String generateAlertMessage(MobileError error) {
        return String.format("【移动端错误告警】\n" +
                             "项目ID：%s\n" +
                             "错误类型：%s\n" +
                             "类名：%s\n" +
                             "发生次数：%d\n" +
                             "触发时间：%s\n" +
                             ALERT_CONTENT_NEW,
                error.getProjectId(),
                error.getErrorType(),
                error.getClassName(),
                error.getEvent(),
                LocalDateTime.now()
                        .format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    protected void incrementEvent(MobileError error) {
        error.incrementEvent();
    }

    /**
     * 判断是否需要启用企业机器人发送告警
     *
     * @param redisKey 缓存前缀
     * @param error    移动端错误
     * @return 结果
     */
    @Override
    protected boolean shouldAlert(String redisKey, MobileError error) {
        String[] data = redisKey.split(":");
        HashMap<String, Integer> alertRuleMap = alertClient
                .selectByBackendRedisKeyToMap(data[2], data[3], data[4]);

        int currentCount = error.getEvent();
        int threshold = alertRuleMap.getOrDefault(redisKey, DEFAULT_THRESHOLD.getAsInt());

        // 如果达到阈值，先检查40分钟内是否已经有相同的告警
        if (currentCount >= threshold) {
            if (checkNotificationExist(error)) {
                log.info("消息已存在或仍未被解决！");
                return false;
            } else {
                log.info("消息不存在，可以发送！");
                return true;
            }
        }
        return false;
    }

    /**
     * 检测通知是否已存在,同时要检测他是否被解决
     *
     * @param error 移动端错误
     * @return 结果
     */
    protected boolean checkNotificationExist(MobileError error) {
        // 检测通知是否已存在
        log.info("错误信息：{}", error);
//        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Notification::getErrorType, error.getErrorType())
//                .eq(Notification::getProjectId, error.getProjectId())
//                .eq(Notification::getErrorId, error.getId())
//                .eq(Notification::getPlatform, "mobile")
//                .eq(Notification::getContent, ALERT_CONTENT_NEW)
//                .orderByDesc(Notification::getTimestamp)  // 按时间倒序排序
//                .last("LIMIT 1");  // 限制只取第一条记录
        Notification notification = alertClient.getNotification(error.getProjectId(),
                error.getErrorType(), "mobile", error.getId(), ALERT_CONTENT_NEW);

        log.info("notification:{}", notification);

        if (notification == null) {
            log.info("该错误没有通知记录");
            return false;
        } else {
            // 获取notification的时间戳
            LocalDateTime notificationTime = notification.getTimestamp();
            // 计算与当前时间的差值
            Duration duration = Duration.between(notificationTime, LocalDateTime.now());
            // 判断是否超过40分钟(2400秒)
            if (duration.getSeconds() < 300) {
                log.info("该错误40分钟内有通知记录");
                // 检测该错误是否未被解决 （未解决在该时间段内无需重发）
//                LambdaQueryWrapper<Responsibility> queryWrapper1 = new LambdaQueryWrapper<>();
//                queryWrapper1.eq(Responsibility::getErrorType, error.getErrorType())
//                        .eq(Responsibility::getProjectId, error.getProjectId());
//                Responsibility responsibility1 = alertClient.getResponsibilityByQueryWrapper(queryWrapper1);
                Responsibility responsibility1 = alertClient.getResponsibility(error.getProjectId(), error.getErrorType());
                // 若该错误未被指派、则发送警告
                if (responsibility1 == null) {
                    log.info("该错误未被指派");
                    return true;
                } else {
                    //已解决就要发
                    if (responsibility1.getIsHandle() == RepositoryConstants.HANDLED) {
                        log.info("该错误40分钟内又报错，但其显示已解决");
                        return false;
                    } else return true;
                }
            }
            log.info("该错误40分钟内无消息记录");
            return false;

        }

    }

    /**
     * 发送微信企业机器人告警
     *
     * @param error 移动错误
     */
    @Override
    public void checkIfAlert(MobileError error) {
        log.info("判断是否达到阈值");

        int currentCount = error.getEvent();
//        Integer threshold = alertRuleMapper.selectThresholdByProjectAndErrorType(error.getProjectId(), error.getErrorType(), "mobile");

        Integer threshold = alertClient.selectThresholdByProjectAndErrorType(
                error.getProjectId(),
                error.getErrorType(),
                "mobile");

        if (threshold == null) {
            log.info("没有设置阈值");
            threshold = DEFAULT_THRESHOLD.getAsInt();
        }

        // 如果没达到阈值，直接返回
        if (currentCount < threshold) return;

        log.info("发送移动端告警");

        //查询同类错误的最新记录
        LambdaQueryWrapper<MobileError> queryWrapper4 = new LambdaQueryWrapper<>();
        queryWrapper4.eq(MobileError::getProjectId, error.getProjectId())
                .eq(MobileError::getMessage, error.getMessage())
                .eq(MobileError::getStack, error.getStack())
                .eq(MobileError::getErrorType, error.getErrorType())
                .eq(MobileError::getClassName, error.getClassName())
                .orderByDesc(MobileError::getTimestamp)
                .last("LIMIT 1");
        MobileError latestError = mobileErrorMapper.selectOne(queryWrapper4);

        // 如果存在同类错误记录，检查时间间隔
        if (latestError != null) {
            log.info("最新错误：{}", latestError);
            long timeDiff = Timestamp.valueOf(error.getTimestamp()).getTime()
                            - Timestamp.valueOf(latestError.getTimestamp()).getTime();
            log.info("当前错误时间: {}, 最新错误时间: {}",
                    error.getTimestamp(),
                    latestError.getTimestamp());
            long minutesDiff = timeDiff / (1000 * 60);
            log.info("计算出的时间差(ms): {}, 分钟差: {}",
                    timeDiff,
                    minutesDiff);// 转换为分钟

            // 如果时间间隔小于40分钟，只更新event次数
            if (minutesDiff < 40) {
                log.info("小于40分钟");
                latestError.setEvent(latestError.getEvent() + error.getEvent());
                // latestError.setTimestamp(error.getTimestamp()); // 更新时间戳为最新时间
                mobileErrorMapper.updateById(latestError);
                log.info("时间间隔小于40分钟，只更新错误次数，errorId:{}", latestError.getId());
            } else {
                log.info("大于40分钟");
                //插入新的错误信息
                log.info("存储错误数据: {}", error);
                mobileErrorMapper.insert(error);
            }
        } else {
            log.info("没有找到错误信息，存储错误数据: {}", error);
            mobileErrorMapper.insert(error);
        }

        // 删除缓存数据
        removeError(error);

        // 查询错误id
        LambdaQueryWrapper<MobileError> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(MobileError::getProjectId, error.getProjectId())
                .eq(MobileError::getErrorType, error.getErrorType())
                .eq(MobileError::getMessage, error.getMessage())
                .eq(MobileError::getStack, error.getStack())
                .eq(MobileError::getClassName, error.getClassName())
                .orderByDesc(MobileError::getTimestamp)
                .last("LIMIT 1");  // 只取第一条记录
        error = mobileErrorMapper.selectOne(queryWrapper2);

        log.info("errorId:{}", error.getId());

        // 如果应该告警
        if (shouldAlert(generateUniqueKey(error), error)) {

            String message = generateAlertMessage(error);

            // 查看该错误类型是否被委派
//            LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(Responsibility::getErrorType, error.getErrorType())
//                    .eq(Responsibility::getProjectId, error.getProjectId());
//            Responsibility responsibility = responsibilityMapper.selectOne(queryWrapper);
//            Responsibility responsibility = alertClient.getResponsibilityByQueryWrapper(queryWrapper);

            Responsibility responsibility = alertClient.getResponsibility(error.getProjectId(), error.getErrorType());

            // 如果已经被委派
            if (responsibility != null) {
                log.info("该错误已经被委派");

                // 更新responsibility中的errorId
//                LambdaQueryWrapper<Responsibility> queryWrapper5 = new LambdaQueryWrapper<>();
//                queryWrapper5.eq(Responsibility::getProjectId, error.getProjectId())
//                        .eq(Responsibility::getPlatform, "mobile")
//                        .eq(Responsibility::getErrorType, error.getErrorType());
//                Responsibility responsibility1 = alertClient.getResponsibilityByQueryWrapper(queryWrapper5);
//
//                Responsibility responsibility1 = alertClient.getResponsibilityFromPlatform(
//                        error.getProjectId(), error.getErrorType(), "mobile");
//
//                responsibility1.setErrorId(error.getId());
//                alertClient.updateResponsibilityByWrapper(responsibility1, queryWrapper5);

                alertClient.updateResponsibility(error.getProjectId(), error.getErrorType(), "mobile", error.getId());
//
//                // 标记该错误为未解决
//                responsibility.setIsHandle(UN_HANDLED);
//                responsibility.setUpdateTime(LocalDateTime.now());
//                alertClient.signResponsibilityNoHandle(responsibility, queryWrapper);
                alertClient.signResponsibilityNoHandle(error.getProjectId(), error.getErrorType(), "mobile");

                // 存储进通知表
                List<Long> alertReceiverID = Arrays.asList(responsibility.getResponsibleId());
                boolean success = saveNotification(alertReceiverID, error);
                if (!success) {
                    log.error("保存通知进数据库失败！");
                }

                String webhookUrl = getWebhookUrl(error.getProjectId());
                if (StrUtil.isBlank(webhookUrl)) {
                    log.warn("未找到对应的企业微信群机器人Webhook地址, 告警失败");
                    return;
                }

                // 获取负责人手机号码
//                LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
//                queryWrapper1.eq(Users::getId, responsibility.getResponsibleId());
//
//                Users responsibleUser = userClient.selectList(queryWrapper1).getFirst();
                UsersDto responsibleUser = userClient.findUserById(responsibility.getResponsibleId());
                String responsiblePhone = responsibleUser.getPhone();

                log.info("发送告警给: {}", responsiblePhone);

                List<String> alertReceiver = Arrays.asList(responsiblePhone);
                sendAlert(webhookUrl, message, alertReceiver);
            } else {
                log.info("该错误未被委派！");
                // 未指派的错误找到管理员
//                LambdaQueryWrapper<Role> queryWrapper3 = new LambdaQueryWrapper<>();
//                queryWrapper3.eq(Role::getProjectId, error.getProjectId())
//                        .eq(Role::getUserRole, USER_ROLE_ADMIN);
//                List<Role> roles = roleMapper.selectList(queryWrapper3);
//                List<Role> roles = projectClient.getRoleListByQueryWrapper(queryWrapper3);

                List<Role> roles = userClient.getRoleListByProjectId(error.getProjectId());

                // 提取角色中的用户ID集合
                List<Long> userIds = roles.stream()
                        .map(Role::getUserId)  // 假设Role中有getUserId()
                        .collect(Collectors.toList());

                //3、保存通知进数据库
                boolean success = saveNotification(userIds, error);
                if (!success) {
                    log.error("保存通知进数据库失败！");
                }

                String webhookUrl = getWebhookUrl(error.getProjectId());
                if (StrUtil.isBlank(webhookUrl)) {
                    log.warn("未找到对应的企业微信群机器人Webhook地址, 告警失败");
                    return;
                }

//                    //4、获取电话号码 发送警告
//                    LambdaQueryWrapper<Users> queryWrapper1 = new LambdaQueryWrapper<>();
//                    queryWrapper1.in(Users::getId, userIds);
//                    List<Users> users = usersMapper.selectList(queryWrapper1);

                List<UsersDto> users = userClient.findUserByIds(userIds);

                List<String> alertReceivers = users.stream()
                        .map(UsersDto::getPhone)
                        .collect(Collectors.toList());

                log.info("发送告警给: {}", alertReceivers);

                sendAlert(webhookUrl, message, alertReceivers);

            }


        } else {
            // 查看该错误类型是否被委派
//            LambdaQueryWrapper<Responsibility> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(Responsibility::getErrorType, error.getErrorType())
//                    .eq(Responsibility::getProjectId, error.getProjectId());
//            Responsibility responsibility = responsibilityMapper.selectOne(queryWrapper);
//            Responsibility responsibility = alertClient.getResponsibilityByQueryWrapper(queryWrapper);
            Responsibility responsibility = alertClient.getResponsibility(error.getProjectId(), error.getErrorType());

            if (responsibility != null) {
                log.info("该错误已经被委派");
                alertClient.updateResponsibility(error.getProjectId(), error.getErrorType(), "mobile", error.getId());
                // 更新responsibility中的errorId
//                LambdaQueryWrapper<Responsibility> queryWrapper6 = new LambdaQueryWrapper<>();
//                queryWrapper6.eq(Responsibility::getProjectId, error.getProjectId())
//                        .eq(Responsibility::getPlatform, "mobile")
//                        .eq(Responsibility::getErrorType, error.getErrorType());
//                Responsibility responsibility2 = alertClient.getResponsibilityByQueryWrapper(queryWrapper6);
//
//                Responsibility responsibility2 = alertClient.getResponsibilityFromPlatform(
//                        error.getProjectId(), error.getErrorType(), "mobile");
//                responsibility2.setErrorId(error.getId());
//
//                alertClient.updateResponsibilityByWrapper(error.getProjectId(), error.getErrorType(), "mobile", responsibility2);
            }
        }
    }

    /**
     * 构建通知信息
     *
     * @param alertReceiverID 接收者
     * @param error           移动端错误
     * @return 结果
     */
    @Override
    protected boolean saveNotification(List<Long> alertReceiverID, MobileError error) {
        log.info("存进通知表！");
        List<Notification> notifications = new ArrayList<>();
        int count = 0;
        for (Long receiverID : alertReceiverID) {
            Notification notification = new Notification();
            notification.setProjectId(error.getProjectId());
            notification.setErrorType(error.getErrorType());
            notification.setErrorId(error.getId());
            notification.setPlatform("mobile");
            notification.setTimestamp(LocalDateTime.now());
            notification.setReceiverId(receiverID);
            notification.setContent(ALERT_CONTENT_NEW);
            count++;
            notifications.add(notification);
        }
        if (count == alertReceiverID.size()) {
            alertClient.addNotification(notifications);
            log.info("已全部通知发送！");
            return true;
        }
        log.info("已通知{}个用户！", count);
        alertClient.addNotification(notifications);
        return false;

    }

}