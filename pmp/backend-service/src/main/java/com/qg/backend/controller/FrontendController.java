package com.qg.backend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.backend.service.*;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.po.FrontendPerformance;
import com.qg.common.domain.po.SourcemapFiles;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.ErrorTrendVO;
import com.qg.common.domain.vo.FrontendBehaviorVO;
import com.qg.common.domain.vo.FrontendPerformanceAverageVO;
import com.qg.common.domain.vo.ManualTrackingVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.qg.common.domain.po.Code.INTERNAL_ERROR;


/**
 * @Description: 前端业务类  // 类说明
 * @ClassName: FrontendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:59   // 时间
 * @Version: 1.0     // 版本
 */
@Slf4j
@RequestMapping("/frontend")
@RestController
@Tag(name = "前端信息")
public class FrontendController {

    @Autowired
    private FrontendPerformanceService frontendPerformanceService;
    @Autowired
    private FrontendResponsibilityService frontendResponsibilityService;
    @Autowired
    private FrontendErrorService frontendErrorService;

    @Autowired
    private FrontendBehaviorService frontendBehaviorService;
    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SourcemapFilesService sourcemapFilesService;


//    @PostMapping("/performance")
//    public void getPerformanceData(@RequestBody String performanceData) {
//        log.info("***********接收到了前端性能数据***********");
//        log.info(performanceData);
//        List<FrontendPerformance> frontendPerformance = JSONUtil.toList(performanceData, FrontendPerformance.class);
//        if (frontendPerformanceService.saveFrontendPerformance(frontendPerformance) > 0) {
//            log.info("已接收的前端性能数据: " + frontendPerformance);
//        } else {
//            log.error("接收前端性能数据失败");
//        }
//    }
//
//    @PostMapping("/error")
//    public Result getErrorData(@RequestBody String errorData) {
//        log.info("***********接收到了前端错误数据***********");
//        log.info(errorData);
//        return frontendErrorService.addFrontendError(errorData);
//    }
//
//    @PostMapping("/behavior")
//    public void getBehaviorData(@RequestBody String behaviorData) {
//        log.info("***********接收到了前端行为数据***********");
//        log.info(behaviorData);
//        List<FrontendBehavior> behaviorList = JSONUtil.toList(behaviorData, FrontendBehavior.class);
//        if (frontendBehaviorService.saveFrontendBehavior(behaviorList) > 0) {
//            log.info("已接收的前端行为数据: " + behaviorList);
//        } else {
//            log.error("接收前端行为数据失败");
//        }
//    }

    /**
     * 接收前端数据
     *
     * @param data
     * @param type
     * @return
     */
    @PostMapping("/{type}")
    public Result getData(@RequestBody String data, @PathVariable String type) {

        switch (type) {
            case "performance":
                log.info("************接收到前端性能数据*************");
                log.info("前端性能数据: " + data);
                return frontendPerformanceService.saveFrontendPerformance(data);
            case "error":
                log.info("************接收到前端错误数据*************");
                log.info("前端错误数据: " + data);
                return frontendErrorService.addFrontendError(data);
            case "behavior":
                log.info("************接收到前端用户行为数据*************");
                log.info("前端用户行为数据: " + data);
                return frontendBehaviorService.saveFrontendBehavior(data);
            default:
                log.error("未知的数据类型: " + type);
                return new Result(INTERNAL_ERROR, "未知的数据类型");
        }

    }

    /**
     * 接收前端sourcemap文件
     *
     * @param projectId
     * @param timestamp
     * @param version
     * @param buildVersion
     * @param files
     * @param jsFilenames
     * @param fileHashes
     * @return
     */
    @PostMapping("/formData")
    public Result uploadMapFile(@RequestParam String projectId, @RequestParam String timestamp, @RequestParam String version,
                                @RequestParam(required = false) String buildVersion, @RequestParam("files") MultipartFile[] files,
                                @RequestParam String[] jsFilenames, @RequestParam String fileHashes) {

        log.info("\n项目ID: {}\n时间戳: {}\n版本: {}\n构建版本: {}\n文件数量: {}\nJS文件名: {}\n文件哈希: {}", projectId, timestamp, version, buildVersion, files.length, String.join(", ", jsFilenames), fileHashes);

        return sourcemapFilesService.uploadFile(projectId, timestamp
                , version, buildVersion, files, jsFilenames, fileHashes);
    }

    /**
     * 已弃用，以下方法均弃用
     * graph微服务，查询指定时间段内某项目中，用户页面停留《所有路由下》时间数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryTimeDataByProjectIdAndTimeRange")
    public List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return frontendBehaviorService.queryTimeDataByProjectIdAndTimeRange(projectId, startTime, endTime);
    }


    /**
     * 已弃用
     *
     * @param projectId 项目id
     * @param route     查询的路由
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryTimeDataByProjectIdAndTimeRangeAndRoute")
    public List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute(
            @RequestParam String projectId,
            @RequestParam String route,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return frontendBehaviorService.queryTimeDataByProjectIdAndTimeRangeAndRoute(projectId, route, startTime, endTime);
    }


    /**
     * 按时间（允许按照时间筛选）以及错误类别（前端/后端/移动）展示错误量
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/getErrorTrend")
    public List<ErrorTrendVO> getErrorTrend(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return frontendErrorService.getErrorTrend(projectId, startTime, endTime);
    }

    /**
     * 获取埋点错误统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryManualTrackingStats")
    public List<ManualTrackingVO> queryManualTrackingStats(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime) {
        return frontendErrorService.queryManualTrackingStats(projectId, startTime, endTime);
    }

    /**
     * 获取两种前端错误信息
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getErrorStats")
    public Object[] getErrorStats(@Param("projectId") String projectId) {
        return frontendErrorService.getErrorStats(projectId);
    }

    /**
     * 获取前端性能，加载时间平均数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryAverageFrontendPerformanceTime")
    public FrontendPerformanceAverageVO queryAverageFrontendPerformanceTime(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime) {
        return frontendPerformanceService.queryAverageFrontendPerformanceTime(projectId, startTime, endTime);
    }

    /**
     * 获取某个项目的访问量
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getVisits")
    public Result getVisits(@Param("projectId") String projectId, @Param("timeType") String timeType) {
        return frontendPerformanceService.getVisits(projectId, timeType);
    }

    /**
     * 获取前端按钮数据
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getFrontendButton")
    public Result getFrontendButton(@Param("projectId") String projectId) {
        return frontendBehaviorService.getFrontendButton(projectId);
    }

    /**
     * 获取前端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getAverageTime")
    public Result getAverageTime(@Param("projectId") String projectId, @Param("timeType") String timeType) {
        return frontendPerformanceService.getAverageTime(projectId, timeType);
    }

    @GetMapping("/getFrontendErrorByWrapper")
    public List<FrontendError> getFrontendErrorByWrapper(@RequestParam LambdaQueryWrapper<FrontendError> queryWrapper) {
        return frontendErrorService.getFrontendErrorByWrapper(queryWrapper);
    }

    @GetMapping("/getFrontendPerformanceByWrapper")
    public List<FrontendPerformance> getFrontendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<FrontendPerformance> queryWrapper) {
        return frontendPerformanceService.getFrontendPerformanceByWrapper(queryWrapper);
    }

    /**
     * 根据条件查询前端错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectFrontResponsibilityByCondition")
    public Result selectFrontResponsibilityByCondition(@RequestParam String projectId, @RequestParam(required = false) String type) {
        return frontendResponsibilityService.selectByCondition(projectId, type);
    }

    /**
     * 根据条件查询前端性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectFrontPerformanceByCondition")
    public Result selectFrontPerformanceByCondition(@RequestParam String projectId, @RequestParam(required = false) String type) {
        return frontendPerformanceService.selectByCondition(projectId, type);
    }

    @GetMapping("/selectOneSourcemapFileByQueryWrapper")
    public SourcemapFiles selectOneSourcemapFileByQueryWrapper(@RequestParam LambdaQueryWrapper<SourcemapFiles> QueryWrapper) {
        return sourcemapFilesService.selectOneSourcemapFileByQueryWrapper(QueryWrapper);
    }

//    /**
//     * 通过 source map 将构建后 JS 文件的行列号解析为原始源码（支持上下文）
//     *
//     * @param sourceMapPath   source map 文件路径
//     * @param generatedLine   构建后 JS 文件中的行号（从1开始）
//     * @param generatedColumn 构建后 JS 文件中的列号（从0开始）
//     * @return 原始源码位置信息（包含上下文）
//     */
//    @GetMapping("/resolveSourcePosition")
//    public SourceMapService.OriginalSourcePosition resolveSourcePosition(@RequestParam String sourceMapPath,
//                                                                         @RequestParam int generatedLine,
//                                                                         @RequestParam int generatedColumn) {
//        return new SourceMapService().resolveSourcePosition(sourceMapPath, generatedLine, generatedColumn);
//    }

    /**
     * 通过错误id查询前端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @GetMapping("/getFrontendErrorByErrorId")
    public List<FrontendError> getFrontendErrorByErrorId(@RequestParam Long errorId) {
        return frontendErrorService.getFrontendErrorByErrorId(errorId);
    }

    /**
     * 通过错误类型查询前端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @GetMapping("/getFrontendErrorByErrorType")
    public List<FrontendError> getFrontendErrorByErrorType(@RequestParam List<String> errorTypes, @RequestParam String projectId) {
        return frontendErrorService.getFrontendErrorByErrorType(errorTypes, projectId);
    }
}
