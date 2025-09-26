package com.qg.mobile.controller;

import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.po.MobilePerformance;
import com.qg.mobile.service.MobileErrorService;
import com.qg.mobile.service.MobilePerformanceService;
import com.qg.mobile.service.MobileResponsibilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 移动业务类  // 类说明
 * @ClassName: MobileController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 22:01   // 时间
 * @Version: 1.0     // 版本
 */
@RequestMapping("/mobile")
@RestController
@Slf4j
@Tag(name = "移动信息")
public class MobileController {


    @Autowired
    private MobilePerformanceService mobilePerformanceService;
    @Autowired
    private MobileResponsibilityService mobileResponsibilityService;
    @Autowired
    private MobileErrorService mobileErrorService;


    @PostMapping("/performance")
    public void getPerformanceData(@RequestBody String performanceData) {
        log.info("***********接收到了移动端性能数据***********");
        log.info(performanceData);
        MobilePerformance mobilePerformance = JSONUtil.toBean(performanceData, MobilePerformance.class);// 解析JSON数据
        if (mobilePerformanceService.saveMobilePerformance(mobilePerformance) > 0) {
            log.info("已接收的移动端性能数据: " + mobilePerformance);
        } else {
            log.error("接收移动端性能数据失败");
        }
    }

    @PostMapping("/error")
    public void getErrorData(@RequestBody String mobileErrorJSON) {
        mobileErrorService.receiveErrorFromSDK(mobileErrorJSON);
    }

    /**
     * 获取移动端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getAverageTime")
    public Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType) {
        return mobilePerformanceService.getAverageTime(projectId, timeType);
    }

    /**
     * 获取移动端操作性能
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getMobileOperation")
    public Result getMobileOperation(@RequestParam String projectId, @RequestParam String timeType) {
        return mobilePerformanceService.getMobileOperation(projectId, timeType);
    }

    /**
     * 网页端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getMobileErrorStats")
    public Object[] getMobileErrorStats(@RequestParam String projectId) {
        return mobileErrorService.getMobileErrorStats(projectId);
    }

    /**
     * app端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getMobileErrorStatsPro")
    public Object[] getMobileErrorStatsPro(@RequestParam String projectId) {
        return mobileErrorService.getMobileErrorStatsPro(projectId);
    }

    @GetMapping("/getMobileErrorByWrapper")
    public List<MobileError> getMobileErrorByWrapper(@RequestParam LambdaQueryWrapper<MobileError> wrapper) {
        return mobileErrorService.getMobileErrorByWrapper(wrapper);
    }

    @GetMapping("/getMobilePerformanceByWrapper")
    public List<MobilePerformance> getMobilePerformanceByWrapper(@RequestParam LambdaQueryWrapper<MobilePerformance> wrapper) {
        return mobilePerformanceService.getMobilePerformanceByWrapper(wrapper);
    }

    /**
     * 根据条件查询移动错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectMobileResponsibilityByCondition")
    public Result selectMobileResponsibilityByCondition(@RequestParam String projectId, @RequestParam String type) {
        return mobileResponsibilityService.selectByCondition(projectId, type);
    }

    /**
     * 根据条件查询移动性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectMobilePerformanceByCondition")
    public Result selectMobilePerformanceByCondition(@RequestParam String projectId, @RequestParam String type) {
        return mobilePerformanceService.selectByCondition(projectId, type);
    }
}
