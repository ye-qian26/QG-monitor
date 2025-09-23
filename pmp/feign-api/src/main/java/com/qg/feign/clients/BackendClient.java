package com.qg.feign.clients;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.BackendError;
import com.qg.common.domain.po.BackendPerformance;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.EarthVO;
import com.qg.common.domain.vo.IllegalAttackVO;
import com.qg.common.domain.vo.MethodInvocationVO;
import kotlin.jvm.internal.Lambda;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient("backend-service")
@RequestMapping("/backend")
public interface BackendClient {

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @GetMapping("/queryIpInterceptionCount")
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
    @GetMapping("/queryForeignIpInterceptions")
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
    @GetMapping("/getBackendErrorStats")
    Object[] getBackendErrorStats(@RequestParam String projectId);

    /**
     * app端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/getBackendErrorStatsPro")
    Object[] getBackendErrorStatsPro(@RequestParam String projectId);

    /**
     * 获取后端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @GetMapping("/getAverageTime")
    Result getAverageTime(@RequestParam String projectId, @RequestParam String timeType);


    @GetMapping("/getBackendErrorByWrapper")
    List<BackendError> getBackendErrorByWrapper(@RequestParam LambdaQueryWrapper<BackendError> queryWrapper);

    @GetMapping("/getBackendPerformanceByWrapper")
    List<BackendPerformance> getBackendPerformanceByWrapper(@RequestParam LambdaQueryWrapper<BackendPerformance> queryWrapper);

    /**
     * 获取后端方法调用统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @GetMapping("/queryMethodInvocationStats")
    List<MethodInvocationVO> queryMethodInvocationStats(
            @RequestParam String projectId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime);
}
