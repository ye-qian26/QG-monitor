package com.qg.backend.controller;

import cn.hutool.core.util.StrUtil;

import com.qg.backend.domain.po.BackendLog;
import com.qg.backend.service.*;
import com.qg.common.domain.vo.*;
import com.qg.common.domain.po.Code;
import com.qg.common.domain.po.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.qg.common.domain.po.Code.NOT_FOUND;
import static com.qg.common.domain.po.Code.SUCCESS;

@Slf4j
@RequestMapping("/graph")
@RestController
@Tag(name = "前端图数据来源")
public class GraphController {

    @Autowired
    private GraphService graphService;
    @Autowired
    private FrontendPerformanceService frontendPerformanceService;
    @Autowired
    private BackendPerformanceService backendPerformanceService;
    @Autowired
    private MobilePerformanceService mobilePerformanceService;
    @Autowired
    private AllPerformanceService allPerformanceService;
    @Autowired
    private BackendLogService backendLogService;

    /**
     * 页面停留时间，页面进入次数
     *
     * @param projectId 项目id
     * @param route     指定路由
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */

    @GetMapping("/pageStateAndEnterCount")
    public Result pageStateAndEnterCount(
            @RequestParam String projectId,
            @RequestParam(required = false) String route,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            // 定义结果集合
            List<FrontendBehaviorVO> list;

            // 执行不同查询
            if (StrUtil.isBlank(route)) {
                list = graphService
                        .queryTimeDataByProjectIdAndTimeRange(projectId, startTime, endTime);
                log.info("""
                        
                        查询项目id:{}
                        起始时间:{}
                        终止时间:{}\
                        
                        内的页面停留时间数据成功""", projectId, startTime, endTime);
            } else {
                list = graphService
                        .queryTimeDataByProjectIdAndTimeRangeAndRoute(projectId, route, startTime, endTime);
                log.info("""
                        
                        查询项目id:{}
                        路由:{}
                        起始时间:{}
                        终止时间:{}\
                        
                        内的页面停留时间数据成功""", projectId, route, startTime, endTime);
            }

            // 至少返回空集合
            return new Result(SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "查询成功");

        } catch (Exception e) {
            log.error("查询页面停留时间，页面进入次数失败，项目id: {}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询页面停留时间，页面进入次数失败");
        }

    }

    /**
     * 获取某个项目的访问量
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getVisits")
    public Result getVisits(@RequestParam String projectId, @RequestParam String timeType) {
        return graphService.getVisits(projectId, timeType);
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
    public Result getErrorTrend(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        try {
            List<ErrorTrendVO> list = graphService.getErrorTrend(projectId, startTime, endTime);
            System.err.println("查询错误趋势成功");
            // 至少返回空集合
            return new Result(SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "查询成功");
        } catch (Exception e) {
            log.error("查询错误趋势时发生异常: projectId={}, startTime={}, endTime={}:{}", projectId, startTime, endTime, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询错误趋势失败 ");
        }
    }

    /**
     * 获取前端错误周报
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getFrontendErrorStats")
    public Result getFrontendErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            System.err.println("查询前端错误前10");
            return new Result(SUCCESS,
                    graphService.getErrorStats(projectId), "查询近一周前端错误统计成功");
        } catch (Exception e) {
            log.error("查询错误统计时发生异常: projectId={}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询近一周前端错误统计失败");
        }
    }

    /**
     * 获取埋点错误统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/getManualTrackingStats")
    public Result getManualTrackingStats(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            List<ManualTrackingVO> list = graphService.getManualTrackingStats(projectId, startTime, endTime);
            return new Result(
                    SUCCESS,
                    list.isEmpty() ? Collections.emptyList() : list,
                    "查询前端埋点统计错误成功");
        } catch (Exception e) {
            log.error("查询前端埋点统计错误时发生异常: projectId={}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询近一周前端错误统计失败");
        }
    }

    /**
     * web端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getBackendErrorStats")
    public Result getBackendErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(SUCCESS,
                    graphService.getBackendErrorStats(projectId), "查询近一周后端错误统计成功");
        } catch (Exception e) {
            log.error("查询后端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周后端错误统计失败");
        }
    }

    /**
     * app端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getBackendErrorStatsPro")
    public Result getBackendErrorStatsPro(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(SUCCESS,
                    graphService.getBackendErrorStatsPro(projectId), "查询近一周后端错误统计成功");
        } catch (Exception e) {
            log.error("查询后端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周后端错误统计失败");
        }
    }

    /**
     * @param projectId
     * @return com.qg.domain.Result
     * @Author lrt
     * @Description //TODO 近一周移动端错
     * @Date 17:25 2025/8/12
     * @Param
     **/
    @GetMapping("/getMobileErrorStats")
    public Result getMobileErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(SUCCESS,
                    graphService.getMobileErrorStats(projectId), "查询近一周移动端错误统计成功");
        } catch (Exception e) {
            log.error("查询移动端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周移动端错误统计失败");
        }
    }

    @GetMapping("/getMobileErrorStatsPro")
    public Result getMobileErrorStatsPro(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(SUCCESS,
                    graphService.getMobileErrorStatsPro(projectId), "查询近一周移动端错误统计成功");
        } catch (Exception e) {
            log.error("查询移动端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周移动端错误统计失败");
        }
    }

    /**
     * @param projectId
     * @param platform
     * @param timeType
     * @return com.qg.domain.Result
     * @Author lrt
     * @Description //TODO api平均响应时间
     * @Date 17:26 2025/8/12
     * @Param
     **/
    @GetMapping("/getAverageTime")
    public Result getAverageTime(@RequestParam String projectId, @RequestParam String platform,
                                 @RequestParam String timeType) {
        switch (platform) {
            case "frontend":
                return graphService.getFrontApiAverageTime(projectId, timeType);
            case "backend":
                return graphService.getBackendApiAverageTime(projectId, timeType);
            case "mobile":
                return graphService.getMobileApiAverageTime(projectId, timeType);
            default:
                return new Result(Code.BAD_REQUEST, "不支持的平台类型");
        }
    }

    /**
     * @param projectId 项目id
     * @return com.qg.domain.Result
     * @Author lrt
     * @Description //TODO 获取前端按钮数据
     * @Date 20:43 2025/8/12
     * @Param
     **/
    @GetMapping("/getFrontendButton")
    public Result getFrontendButton(@RequestParam String projectId) {
        return graphService.getFrontendButton(projectId);
    }

    /**
     * 获取前端性能，加载时间平均数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/getAverageFrontendPerformanceTime")
    public Result getAverageFrontendPerformanceTime(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            return new Result(SUCCESS
                    , graphService.getAverageFrontendPerformanceTime(projectId, startTime, endTime)
                    , "查询前端性能数据-平均时间,成功");
        } catch (Exception e) {
            log.error("查询查询前端性能数据-平均时间,失败:{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询查询前端性能数据-平均时间,失败");
        }

    }


    /**
     * 获取移动端操作性能
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/MobileOperationalPerformance")
    public Result getMobileOperation(@RequestParam String projectId, @RequestParam String timeType) {
        return graphService.getMobileOperation(projectId, timeType);
    }

    /**
     * 获取后端方法调用统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/getMethodInvocationStats")
    public Result getMethodInvocationStats(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        try {
            List<MethodInvocationVO> list = graphService.getMethodInvocationStats(projectId, startTime, endTime);
            // 至少返回空集合
            return new Result(SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "获取方法调用统计成功");
        } catch (Exception e) {
            log.error("获取方法调用统计时发生异常: projectId={}, startTime={}, endTime={}:{}", projectId, startTime, endTime, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "获取方法调用统计失败 ");
        }
    }

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @GetMapping("/getIpInterceptionCount")
    public Result getIpInterceptionCount(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        try {
            List<IllegalAttackVO> list = graphService.getIpInterceptionCount(projectId, startTime, endTime);
            // 至少返回空集合
            return new Result(SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "获取非法攻击统计成功");
        } catch (Exception e) {
            log.error("获取非法攻击统计时发生异常: projectId={}, startTime={}, endTime={}:{}", projectId, startTime, endTime, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "获取非法攻击统计失败 ");
        }
    }

    /**
     * 查询指定时间段内所有境外访问的IP的拦截次数统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/getForeignIpInterception")
    public Result getForeignIpInterception(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        try {
            List<EarthVO> list = graphService.getForeignIpInterception(projectId, startTime, endTime);
            // 至少返回空集合
            return new Result(SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "获取外网非法攻击统计成功");
        } catch (Exception e) {
            log.error("获取外网非法攻击统计时发生异常: projectId={}, startTime={}, endTime={}:{}", projectId, startTime, endTime, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "获取外网非法攻击统计失败 ");
        }
    }

    /**
     * 判断项目id、时间是否为空
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    private boolean isProjectIdAndTimeNull(String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        if (StrUtil.isBlank(projectId) || startTime == null || endTime == null) {
            log.warn("参数为空" +
                     ",projectId:" + projectId +
                     ",startTime:" + startTime +
                     ",endTime:" + endTime);
            return true;
        }
        return false;
    }

    @GetMapping("/selectPerformanceByCondition")
    public Result selectPerformanceByCondition(@RequestParam String platform, @RequestParam String projectId,
                                               @RequestParam(required = false) String api,
                                               @RequestParam(required = false) String captureType,
                                               @RequestParam(required = false) String osVersion) {
        switch (platform) {
            case "backend":
                return backendPerformanceService.selectByCondition(projectId, api);
            case "frontend":
                return frontendPerformanceService.selectByCondition(projectId, captureType);
            case "mobile":
                return mobilePerformanceService.selectByCondition(projectId, osVersion);
            case "all":
                return allPerformanceService.selectByCondition(projectId, api, captureType, osVersion);
            default:
                return new Result(400, "平台参数错误");
        }

    }

/*
    @GetMapping("/getAllLogs/{projectId}")
    public Result getAllLogs(@PathVariable String projectId) {
        List<BackendLog> logs = backendLogService.getAllLogs(projectId);
        if (logs != null && !logs.isEmpty()) {
            return new Result(SUCCESS,logs,"查询成功");
        } else {
            return new Result(500, "没有日志数据");
        }
    }
*/

    @GetMapping("/selectLogsByCondition")
    public Result getLogsByCondition(@RequestParam(required = false) String evn,
                                     @RequestParam(required = false) String logLevel, @RequestParam String projectId) {
        List<BackendLog> logs = backendLogService.getLogsByCondition(evn, logLevel, projectId);
        if (logs != null && !logs.isEmpty()) {
            return new Result(SUCCESS, logs, "查询成功");
        } else {
            return new Result(SUCCESS, Collections.emptyList(), "没有符合条件的日志数据");
        }
    }
}
