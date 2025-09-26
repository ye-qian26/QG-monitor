package com.qg.feign.clients;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.MobilePerformance;
import com.qg.common.domain.po.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient("mobile-service")
public interface MobileClient {

    /**
     * 获取移动端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/mobile/getAverageTime")
    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType);

    /**
     * 获取移动端操作性能
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/mobile/getMobileOperation")
    Result getMobileOperation(@RequestParam String projectId, @RequestParam String timeType);

    /**
     * 网页端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/mobile/getMobileErrorStats")
    Object[] getMobileErrorStats(@RequestParam String projectId);

    /**
     * app端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/mobile/getMobileErrorStatsPro")
    Object[] getMobileErrorStatsPro(@RequestParam String projectId);

    @GetMapping("/mobile/getMobileErrorByWrapper")
    List<MobileError> getMobileErrorByWrapper(@RequestParam LambdaQueryWrapper<MobileError> wrapper);

    @GetMapping("/mobile/getMobilePerformanceByWrapper")
    List<MobilePerformance> getMobilePerformanceByWrapper(@RequestParam LambdaQueryWrapper<MobilePerformance> wrapper);

    /**
     * 根据条件查询移动错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectMobileResponsibilityByCondition")
    Result selectMobileResponsibilityByCondition(@RequestParam String projectId, @RequestParam String type);

    /**
     * 根据条件查询移动性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectMobilePerformanceByCondition")
    Result selectMobilePerformanceByCondition(@RequestParam String projectId, @RequestParam String type);
}
