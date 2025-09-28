package com.qg.backend.controller;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.qg.backend.service.*;

import com.qg.common.domain.dto.WrapperDTO;
import com.qg.common.domain.po.BackendError;
import com.qg.common.domain.po.BackendPerformance;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.EarthVO;
import com.qg.common.domain.vo.IllegalAttackVO;
import com.qg.common.domain.vo.MethodInvocationVO;
import com.qg.feign.clients.ProjectClient;

import com.qg.feign.clients.UserClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description: 后端业务类  // 类说明
 * @ClassName: BackendController    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 22:00   // 时间
 * @Version: 1.0     // 版本
 */

@Slf4j
@RequestMapping("/backend")
@RestController
@Tag(name = "后端信息")
@RequiredArgsConstructor
public class BackendController {

    @Autowired
    private BackendPerformanceService backendPerformanceService;

    @Autowired
    private BackendErrorService backendErrorService;

    @Autowired
    private BackendLogService backendLogService;
    @Autowired
    private MethodInvocationService methodInvocationService;

    private final UserClient userClient;
    @Autowired
    private BackendResponsibilityService backendResponsibilityService;

    /**
     * 接收后端方法调用数据
     *
     * @param encodedData
     */
    @PostMapping("/getMethodUseCount")
    public void getMethodUseCount(@RequestBody String encodedData) {
        try {
            // 解码和验证
            String decodedData = URLDecoder.decode(encodedData, StandardCharsets.UTF_8);
            if (!decodedData.contains("@")) {
                log.warn("传入的数据格式有误: {}", encodedData);
                return;
            }

            // 分割数据
            String[] parts = decodedData.split("@", 2);
            String projectId = parts[0].trim();
            String mapJSON = parts[1].trim();

          /*  if (!projectService.checkProjectIdExist(projectId)) {
                return;
            }*/
            if (!userClient.checkProjectIdExist(projectId)) {
                return;
            }

            // 解析和验证
            if (StrUtil.isBlank(projectId) || StrUtil.isBlank(mapJSON)) {
                log.warn("传入的项目id或方法使用情况为空");
                return;
            }

            JSONObject jsonObj = JSONUtil.parseObj(mapJSON);
            Map<String, Integer> methodMap = jsonObj.toBean(new TypeReference<>() {
            });

            if (methodMap.isEmpty()) {
                log.warn("方法使用情况为空");
                return;
            }

            methodInvocationService.statisticsMethod(methodMap, projectId);

        } catch (Exception e) {
            log.error("统计方法过程出现异常: {}", e.getMessage());
        }
    }

    /**
     * 接收后端性能数据
     *
     * @param performanceData
     */
    @PostMapping("/performance")
    public void getPerformanceData(@RequestBody String performanceData) {
        backendPerformanceService.addPerformance(performanceData);
    }

    /**
     * 接收后端错误数据
     *
     * @param errorData
     */
    @PostMapping("/error")
    public void getErrorData(@RequestBody String errorData) {
        log.info(errorData);
        backendErrorService.addBackendError(errorData);
    }

    /**
     * 接收后端SDK日志
     *
     * @param logJSON
     */
    @PostMapping("/log")
    public void receiveLogFromSDK(@RequestBody String logJSON) {
        backendLogService.receiveLogFromSDK(logJSON);
    }

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @GetMapping("/queryIpInterceptionCount")
    public List<IllegalAttackVO> queryIpInterceptionCount(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return backendLogService.queryIpInterceptionCount(projectId, startTime, endTime);
    }

    /**
     * 查询指定时间段内所有境外访问的IP的拦截次数统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryForeignIpInterceptions")
    public List<EarthVO> queryForeignIpInterceptions(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return backendLogService.queryForeignIpInterceptions(projectId, startTime, endTime);
    }

    /**
     * web端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getBackendErrorStats")
    public Object[] getBackendErrorStats(@RequestParam String projectId) {
        return backendErrorService.getBackendErrorStats(projectId);
    }

    /**
     * app端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getBackendErrorStatsPro")
    public Object[] getBackendErrorStatsPro(@RequestParam String projectId) {
        return backendErrorService.getBackendErrorStatsPro(projectId);
    }

    /**
     * 获取后端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getAverageTime")
    public Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType) {
        return backendPerformanceService.getAverageTime(projectId, timeType);
    }

//    @GetMapping("/getBackendErrorByWrapper")
//    public List<BackendError> getBackendErrorByWrapper(@RequestParam LambdaQueryWrapper<BackendError> queryWrapper) {
//        return backendErrorService.getBackendErrorByWrapper(queryWrapper);
//    }

    @PostMapping("/getBackendErrorByWrapper")
    public List<BackendError> getBackendErrorByWrapper(@RequestBody WrapperDTO<BackendError> dto) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        LambdaQueryWrapper<BackendError> wrapper = mapper.readValue(
                dto.getWrapperJson(),
                TypeFactory.defaultInstance().constructParametricType(
                        LambdaQueryWrapper.class,
                        BackendError.class
                )
        );
        return backendErrorService.getBackendErrorByWrapper(wrapper);
    }

    @GetMapping("/getBackendPerformanceByWrapper")
    public List<BackendPerformance> getBackendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<BackendPerformance> queryWrapper) {
        return backendPerformanceService.getBackendPerformanceByWrapper(queryWrapper);
    }

    /**
     * 获取后端方法调用统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryMethodInvocationStats")
    public List<MethodInvocationVO> queryMethodInvocationStats(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return methodInvocationService.queryMethodInvocationStats(projectId, startTime, endTime);
    }

    /**
     * 根据条件查询后端错误信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectBackendResponsibilityByCondition")
    public Result selectBackendResponsibilityByCondition(@RequestParam String projectId, @RequestParam(required = false) String type) {
        return backendResponsibilityService.selectByCondition(projectId, type);
    }

    /**
     * 根据条件查询后端性能信息
     *
     * @param projectId 项目id
     * @param type      错误类型
     * @return 结果
     */
    @GetMapping("/selectBackendPerformanceByCondition")
    public Result selectBackendPerformanceByCondition(@RequestParam String projectId, @RequestParam(required = false) String type) {
        return backendPerformanceService.selectByCondition(projectId, type);
    }

    /**
     * 通过错误id查询后端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @GetMapping("/getBackendErrorByErrorId")
    public List<BackendError> getBackendErrorByErrorId(@RequestParam Long errorId) {
        return backendErrorService.getBackendErrorByErrorId(errorId);
    }

    /**
     * 通过错误类型查询后端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @GetMapping("/backend/getBackendErrorByErrorType")
    public List<BackendError> getBackendErrorByErrorType(@RequestParam List<String> errorTypes, @RequestParam String projectId) {
        return backendErrorService.getBackendErrorByErrorType(errorTypes, projectId);
    }

}

