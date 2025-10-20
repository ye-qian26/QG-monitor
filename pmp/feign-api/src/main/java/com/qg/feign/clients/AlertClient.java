package com.qg.feign.clients;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Notification;
import com.qg.common.domain.po.Responsibility;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: ResponsibilityClient    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/22 11:54   // 时间
 * @Version: 1.0     // 版本
 */
@FeignClient("alert-service")
public interface AlertClient {
    @DeleteMapping("/responsibilities/deleteUserId")
    int deleteByUserId(@RequestParam Long userId);


    @GetMapping("/alertRules/selectThresholdByProjectAndErrorType")
    Integer selectThresholdByProjectAndErrorType(@RequestParam String projectId,
                                                 @RequestParam String errorType, @RequestParam String platform);

    //    @GetMapping("/responsibilities/getResponsibilityByQueryWrapper")
//    Responsibility getResponsibilityByQueryWrapper(@RequestParam LambdaQueryWrapper<Responsibility> queryWrapper);

    /**
     * 获取责任
     *
     * @param projectId 项目id
     * @param errorType 错误类型
     * @return 结果
     */
    @GetMapping("/responsibilities/getResponsibility")
    Responsibility getResponsibility(@RequestParam String projectId, @RequestParam String errorType);

    /**
     * 获取某端责任
     *
     * @param projectId 项目id
     * @param errorType 错误类型
     * @return 结果
     */
    @GetMapping("/responsibilities/getResponsibilityFromPlatform")
    Responsibility getResponsibilityFromPlatform(@RequestParam String projectId, @RequestParam String errorType, @RequestParam String platform);

    /**
     * 更新责任错误id
     *
     * @param projectId 项目id
     * @param errorType 错误类型
     * @param platform  来源
     * @param errorId   新的id
     * @return 结果
     */
    @PutMapping("/responsibilities/updateResponsibility")
    boolean updateResponsibility(@RequestParam String projectId, @RequestParam String errorType, @RequestParam String platform, @RequestParam Long errorId);
//
//    @PutMapping("/responsibilities/updateResponsibilityByWrapper")
//    Integer updateResponsibilityByWrapper(@RequestParam Responsibility responsibility, @RequestParam LambdaQueryWrapper<Responsibility> queryWrapper);
//
//    @GetMapping("/responsibilities/getResponsibilityListByWrapper")
//    List<Responsibility> getResponsibilityListByWrapper(@RequestParam LambdaQueryWrapper<Responsibility> queryWrapper);

    @GetMapping("/responsibilities/getResponsibilityListByProjectId")
    List<Responsibility> getResponsibilityListByProjectId(@RequestParam String projectId, @RequestParam(required = false) String platform);
//
//    @GetMapping("/notifications/getNotificationByWrapper")
//    Notification getNotificationByWrapper(@RequestParam LambdaQueryWrapper<Notification> queryWrapper);

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
    @GetMapping("/notifications/getNotification")
    Notification getNotification(@RequestParam String projectId, @RequestParam String errorType,
                                 @RequestParam String platform, @RequestParam Long errorId, @RequestParam String content);


    @GetMapping("/alertRules/selectByBackendRedisKeyToMap")
    HashMap<String, Integer> selectByBackendRedisKeyToMap(@RequestParam String projectId,
                                                          @RequestParam String errorType, @RequestParam String environment);

    @PostMapping("/notifications/addNotifications")
    void addNotification(@RequestBody List<Notification> notifications);

    /**
     * 标记为未解决
     *
     * @param projectId 项目id
     * @param errorType 错误类型
     * @param platform  来源
     * @return 结果
     */
    @PutMapping("/responsibilities/signResponsibilityNoHandle")
    boolean signResponsibilityNoHandle(@RequestParam String projectId,
                                       @RequestParam String errorType,
                                       @RequestParam String platform);
}
