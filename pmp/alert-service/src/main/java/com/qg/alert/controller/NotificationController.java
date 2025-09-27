package com.qg.alert.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Notification;
import com.qg.alert.service.NotificationService;
import com.qg.common.domain.po.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 根据接收者id查询通知
     *
     * @param receiverId
     * @return
     */
    @GetMapping("/selectByReceiverId")
    public Result selectByReceiverId(@RequestParam Long receiverId, @RequestParam Integer isSenderExist) {
        return notificationService.selectByReceiverId(receiverId, isSenderExist);
    }

    /**
     * 根据接收者id更新通知为已读
     *
     * @param receiverId
     * @return
     */
    @PutMapping("/updateIsRead/{receiverId}")
    public Result updateIsRead(@PathVariable Long receiverId) {
        return notificationService.updateIsRead(receiverId);
    }

    /**
     * 根据通知id更新通知为已读
     *
     * @param id
     * @return
     */
    @PutMapping("/updateIsReadById/{id}")
    public Result updateIsReadById(@PathVariable Long id) {
        return notificationService.updateIsReadById(id);
    }


    /**
     * 添加通知
     *
     * @param notificationList
     * @return
     */
    @PostMapping("/add")
    public Result addNotification(@RequestBody List<Notification> notificationList) {
        return notificationService.add(notificationList);
    }

    /**
     * 根据 id 删除 通知
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable Long id) {
        return notificationService.deleteById(id);
    }

    /**
     * 根据 接收者 id 删除 通知
     *
     * @param receiverId
     * @return
     */
    @DeleteMapping("/deleteByReceiverId")
    public Result deleteByReceiverId(@RequestParam Long receiverId, @RequestParam Integer isSenderExist) {
        return notificationService.deleteByReceiverId(receiverId, isSenderExist);
    }

    @GetMapping("/getNotificationByWrapper")
    public Notification getNotificationByWrapper(@RequestParam LambdaQueryWrapper<Notification> queryWrapper) {
        return notificationService.getNotificationByWrapper(queryWrapper);
    }

    @PostMapping("/addNotifications")
    public void addNotifications(@RequestBody List<Notification> notifications) {
        notificationService.add(notifications);
    }

    /**
     * 获取通知内容
     *
     * @param projectId 项目id
     * @param errorType 错误类型
     * @param platform  来源
     * @param errorId   错误id
     * @param content   内容
     * @return 结果
     */
    @GetMapping("/getNotification")
    public Notification getNotification(@RequestParam String projectId, @RequestParam String errorType,
                                        @RequestParam String platform, @RequestParam Long errorId, @RequestParam String content) {
        return notificationService.getNotification(projectId, errorType, platform, errorId, content);
    }


}
