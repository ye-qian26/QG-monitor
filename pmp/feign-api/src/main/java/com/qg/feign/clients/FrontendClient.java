package com.qg.feign.clients;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.po.FrontendPerformance;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.ErrorTrendVO;
import com.qg.common.domain.vo.FrontendBehaviorVO;
import com.qg.common.domain.vo.FrontendPerformanceAverageVO;
import com.qg.common.domain.vo.ManualTrackingVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient("frontend-service")
public interface FrontendClient {

    /**
     * graph微服务，查询指定时间段内某项目中，用户页面停留《所有路由下》时间数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/frontend/queryTimeDataByProjectIdAndTimeRange")
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

    /**
     * 查询指定时间段内某项目中，用户页面停留《某路由下》时间数据
     *
     * @param projectId 项目id
     * @param route     查询的路由
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/frontend/queryTimeDataByProjectIdAndTimeRangeAndRoute")
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute(
            @RequestParam String projectId,
            @RequestParam String route,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

    /**
     * 按时间（允许按照时间筛选）以及错误类别（前端/后端/移动）展示错误量
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/frontend/getErrorTrend")
    List<ErrorTrendVO> getErrorTrend(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

    /**
     * 获取埋点错误统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/frontend/queryManualTrackingStats")
    List<ManualTrackingVO> queryManualTrackingStats(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);

    /**
     * 获取两种前端错误信息
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/frontend/getErrorStats")
    Object[] getErrorStats(@RequestParam String projectId);

    /**
     * 获取前端性能，加载时间平均数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/frontend/queryAverageFrontendPerformanceTime")
    FrontendPerformanceAverageVO queryAverageFrontendPerformanceTime(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);

    /**
     * 获取某个项目的访问量
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/frontend/getVisits")
    Result getVisits(@RequestParam String projectId, @RequestParam String timeType);

    /**
     * 获取前端按钮数据
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/frontend/getFrontendButton")
    Result getFrontendButton(@RequestParam String projectId);

    /**
     * 获取前端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/frontend/getAverageTime")
    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType);

    @GetMapping("/frontend/getFrontendErrorByWrapper")
    List<FrontendError> getFrontendErrorByWrapper(@RequestParam LambdaQueryWrapper<FrontendError> queryWrapper);

    @GetMapping("/frontend/getFrontendPerformanceByWrapper")
    List<FrontendPerformance> getFrontendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<FrontendPerformance> queryWrapper);
}
