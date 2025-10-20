package com.qg.feign.clients;


import com.qg.common.domain.po.BackendError;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient("backend-service")
public interface BackendClient {

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @GetMapping("/backend/queryIpInterceptionCount")
    List<IllegalAttackVO> queryIpInterceptionCount(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);

    /**
     * 查询指定时间段内所有境外访问的IP的拦截次数统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/backend/queryForeignIpInterceptions")
    List<EarthVO> queryForeignIpInterceptions(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);

    /**
     * web端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/backend/getBackendErrorStats")
    Object[] getBackendErrorStats(@RequestParam String projectId);

    /**
     * app端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/backend/getBackendErrorStatsPro")
    Object[] getBackendErrorStatsPro(@RequestParam String projectId);

    /**
     * 获取某端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/backend/getAverageTime")
    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType, @RequestParam String platform);

//    @GetMapping("/backend/getBackendErrorByWrapper")
//    List<BackendError> getBackendErrorByWrapper(@RequestParam LambdaQueryWrapper<BackendError> queryWrapper);

    /**
     * 通过错误id查询后端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @GetMapping("/backend/getBackendErrorByErrorId")
    List<BackendError> getBackendErrorByErrorId(@RequestParam Long errorId);

    /**
     * 通过错误类型查询后端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @GetMapping("/backend/getBackendErrorByErrorType")
    List<BackendError> getBackendErrorByErrorType(@RequestParam List<String> errorTypes, @RequestParam(required = false) String projectId);


//    @PostMapping("/backend/getBackendErrorByWrapper")
//    List<BackendError> getBackendErrorByWrapper(@RequestBody WrapperDTO<BackendError> dto);
//
//    @GetMapping("/backend/getBackendPerformanceByWrapper")
//    List<BackendPerformance> getBackendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<BackendPerformance> queryWrapper);

    /**
     * 获取后端方法调用统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/backend/queryMethodInvocationStats")
    List<MethodInvocationVO> queryMethodInvocationStats(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);

    /**
     * 根据条件查询后端错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/backend/selectBackendResponsibilityByCondition")
    Result selectBackendResponsibilityByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

    /**
     * 根据条件查询后端性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/backend/selectBackendPerformanceByCondition")
    Result selectBackendPerformanceByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

    /**
     * =================================================================================================================
     */

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

//    /**
//     * 获取前端api平均响应时间
//     *
//     * @param projectId 项目id
//     * @param timeType  时间类型
//     * @return 结果
//     */
//    @GetMapping("/frontend/getAverageTime")
//    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType);

//    @GetMapping("/frontend/getFrontendErrorByWrapper")
//    List<FrontendError> getFrontendErrorByWrapper(@RequestParam LambdaQueryWrapper<FrontendError> queryWrapper);
//
//    @GetMapping("/frontend/getFrontendPerformanceByWrapper")
//    List<FrontendPerformance> getFrontendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<FrontendPerformance> queryWrapper);

    /**
     * 根据条件查询前端错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/frontend/selectFrontResponsibilityByCondition")
    Result selectFrontResponsibilityByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

    /**
     * 根据条件查询前端性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/frontend/selectFrontPerformanceByCondition")
    Result selectFrontPerformanceByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

//    @GetMapping("/frontend/selectOneSourcemapFileByQueryWrapper")
//    SourcemapFiles selectOneSourcemapFileByQueryWrapper(@RequestParam LambdaQueryWrapper<SourcemapFiles> QueryWrapper);
//    /**
//     * 通过 source map 将构建后 JS 文件的行列号解析为原始源码（支持上下文）
//     *
//     * @param sourceMapPath   source map 文件路径
//     * @param generatedLine   构建后 JS 文件中的行号（从1开始）
//     * @param generatedColumn 构建后 JS 文件中的列号（从0开始）
//     * @return 原始源码位置信息（包含上下文）
//     */
//    @GetMapping("/resolveSourcePosition")
//    SourceMapService.OriginalSourcePosition resolveSourcePosition(@RequestParam String sourceMapPath,
//                                                                         @RequestParam int generatedLine,
//                                                                         @RequestParam int generatedColumn);

    /**
     * 通过错误id查询前端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @GetMapping("/frontend/getFrontendErrorByErrorId")
    List<FrontendError> getFrontendErrorByErrorId(@RequestParam Long errorId);

    /**
     * 通过错误类型查询前端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @GetMapping("/frontend/getFrontendErrorByErrorType")
    List<FrontendError> getFrontendErrorByErrorType(@RequestParam List<String> errorTypes, @RequestParam String projectId);

    /**
     * =================================================================================================================
     */


//    /**
//     * 获取移动端api平均响应时间
//     *
//     * @param projectId 项目id
//     * @param timeType  时间类型
//     * @return 结果
//     */
//    @GetMapping("/mobile/getAverageTime")
//    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType);

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

//    @GetMapping("/mobile/getMobileErrorByWrapper")
//    List<MobileError> getMobileErrorByWrapper(@RequestParam LambdaQueryWrapper<MobileError> wrapper);
//
//    @GetMapping("/mobile/getMobilePerformanceByWrapper")
//    List<MobilePerformance> getMobilePerformanceByWrapper(@RequestParam LambdaQueryWrapper<MobilePerformance> wrapper);

    /**
     * 根据条件查询移动错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/mobile/selectMobileResponsibilityByCondition")
    Result selectMobileResponsibilityByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

    /**
     * 根据条件查询移动性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/mobile/selectMobilePerformanceByCondition")
    Result selectMobilePerformanceByCondition(@RequestParam String projectId, @RequestParam(required = false) String type);

    /**
     * 通过错误id查询移动端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @GetMapping("/mobile/getMobileErrorByErrorId")
    List<MobileError> getMobileErrorByErrorId(@RequestParam Long errorId);

    /**
     * 通过错误类型查询移动端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @GetMapping("/mobile/getMobileErrorByErrorType")
    List<MobileError> getMobileErrorByErrorType(@RequestParam List<String> errorTypes, @RequestParam String projectId);
}
