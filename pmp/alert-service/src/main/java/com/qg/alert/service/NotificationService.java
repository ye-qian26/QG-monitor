package com.qg.alert.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Notification;
import com.qg.common.domain.po.Result;

import java.util.List;

public interface NotificationService {
    Result selectByReceiverId(Long receiverId, Integer isSenderExist);

    Result updateIsRead(Long receiverId);

    Result add(List<Notification> notificationList);

    Result updateIsReadById(Long id);

    Result deleteById(Long id);

    Result deleteByReceiverId(Long receiverId, Integer isSenderExist);

    Notification getNotificationByWrapper(LambdaQueryWrapper<Notification> queryWrapper);

    Notification getNotification(String projectId, String errorType, String platform, Long errorId, String content);
}
