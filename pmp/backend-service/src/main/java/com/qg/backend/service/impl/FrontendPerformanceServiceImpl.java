package com.qg.backend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.qg.backend.mapper.FrontendErrorMapper;
import com.qg.backend.mapper.FrontendPerformanceMapper;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.FrontendPerformanceAverageVO;
import com.qg.common.domain.po.FrontendPerformance;

import com.qg.backend.service.FrontendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.common.domain.po.Code.*;


/**
 * @Description: 前端性能应用  // 类说明
 * @ClassName: FrontendPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendPerformanceServiceImpl implements FrontendPerformanceService {

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;
    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Override
    public Result saveFrontendPerformance(String data) {
        // 参数校验
        if (data == null || data.trim().isEmpty()) {
            log.warn("前端性能数据为空");
            return new Result(BAD_REQUEST, "前端性能数据为空");
        }

        try {
            List<FrontendPerformance> frontendPerformanceList = JSONUtil.toList(data, FrontendPerformance.class);

            if (frontendPerformanceList == null || frontendPerformanceList.isEmpty()) {
                log.warn("解析后的前端性能数据为空");
                return new Result(BAD_REQUEST, "解析前端性能数据为空");
            }

            // 计数成功插入的记录数
            int successCount = 0;
            for (FrontendPerformance performance : frontendPerformanceList) {
                if (performance != null) { // 额外的空值检查
                    int result = frontendPerformanceMapper.insert(performance);
                    successCount += result;
                }
            }

            log.info("保存前端性能数据完成，总共{}条，成功{}条", frontendPerformanceList.size(), successCount);
            return new Result(SUCCESS, "保存前端性能数据成功，共处理" + frontendPerformanceList.size() + "条数据");

        } catch (cn.hutool.json.JSONException e) {
            log.error("前端性能数据JSON解析失败: ", e);
            return new Result(BAD_REQUEST, "数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存前端性能数据时发生异常: ", e);
            return new Result(INTERNAL_ERROR, "保存前端性能数据失败: " + e.getMessage());
        }
    }


    @Override
    public Result selectByCondition(String projectId, String capture) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }
        LambdaQueryWrapper<FrontendPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FrontendPerformance::getProjectId, projectId);

        if (capture != null && !capture.isEmpty()) {
            queryWrapper.like(FrontendPerformance::getCaptureType, capture);
        }

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(queryWrapper);

        return new Result(SUCCESS, List.of(new ArrayList<>(), frontendPerformances, new ArrayList<>()), "查询成功");
    }

    /**
     * 获取某个项目的访问量
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
    @Override
    public Result getVisits(String projectId, String timeType) {
        // 参数校验
        if (projectId == null || projectId.trim().isEmpty()) {
            log.warn("项目ID不能为空");
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }
        if (timeType == null || timeType.trim().isEmpty()) {
            log.warn("时间类型不能为空");
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }

        Result count = new Result();
        switch (timeType) {
            case "day":
            case "week":
            case "month":
            case "year":
                count = getVisitCount(projectId, timeType);
                break;
            default:
                log.warn("不支持的时间类型: {}", timeType);
                return new Result(BAD_REQUEST, "不支持的时间类型: " + timeType);
        }


        if (count != null) return count;


        return new Result(BAD_GATEWAY, "查询失败");
    }

    private Result getVisitCount(String projectId, String timeType) {

        List<Integer> timeCount = new ArrayList<>();

        switch (timeType) {
            case "day":
                for (int i = 0; i < 24; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "week":
                for (int i = 0; i < 7; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "month":
                for (int i = 0; i < 30; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "year":
                for (int i = 0; i < 12; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            default:
                return new Result(BAD_REQUEST, "不支持的时间类型");
        }
        return new Result(SUCCESS, timeCount, "查询成功");
    }

    private void getCount(String projectId, int i, List<Integer> timeCount, String timeType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start, end;
        switch (timeType) {
            case "day":
                start = now.minusHours(i + 1);
                end = now.minusHours(i);
                break;
            case "week":
                start = now.minusDays(i + 1);
                end = now.minusDays(i);
                break;
            case "month":
                start = now.minusWeeks(i + 1);
                end = now.minusWeeks(i);
                break;
            case "year":
                start = now.minusMonths(i + 1);
                end = now.minusMonths(i);
                break;
            default:
                return;
        }
        LambdaQueryWrapper<FrontendPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FrontendPerformance::getProjectId, projectId);
        wrapper.between(FrontendPerformance::getTimestamp, start, end);
        List<FrontendPerformance> list = frontendPerformanceMapper.selectList(wrapper);
        int count = list.stream()
                .filter(p -> p.getEvent() != null)
                .mapToInt(FrontendPerformance::getEvent)
                .sum();
        timeCount.add(count);
    }

    @Override
    public Result getAverageTime(String projectId, String timeType) {
        if (projectId == null || timeType == null) {
            return new Result(BAD_REQUEST, "参数不能为空");
        }
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FrontendError::getProjectId, projectId);

        LocalDateTime passTime;

        switch (timeType) {
            case "day":
                passTime = LocalDateTime.now().minusDays(1);
                break;
            case "week":
                passTime = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                passTime = LocalDateTime.now().minusMonths(1);
                break;
            default:
                return new Result(BAD_REQUEST, "不支持的时间类型");
        }
        queryWrapper.ge(FrontendError::getTimestamp, passTime);

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        // 计算加权平均响应时间
        Map<String, Double> averageTimeMap = frontendErrors.stream()
                .filter(bp -> bp.getRequest() != null && bp.getDuration() != null && bp.getEvent() != null)
                .collect(Collectors.groupingBy(bp -> {
                            // 解析 request 字段，提取 url
                            try {
                                JSONObject json = JSONUtil.parseObj(bp.getRequest());
                                return json.getStr("url");
                            } catch (Exception e) {
                                return "unknown";
                            }
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double totalTime = list.stream()
                                            .mapToDouble(bp -> bp.getDuration() * bp.getEvent())
                                            .sum();
                                    int totalEvents = list.stream()
                                            .mapToInt(FrontendError::getEvent)
                                            .sum();
                                    return totalEvents > 0 ? totalTime / totalEvents : 0.0;
                                }
                        )));

        return new Result(SUCCESS, averageTimeMap, "查询成功");
    }

    /**
     * 获取前端性能，加载时间平均数据
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @Override
    public FrontendPerformanceAverageVO queryAverageFrontendPerformanceTime(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime) {
        return frontendPerformanceMapper.queryAverageFrontendPerformanceTime(projectId, startTime, endTime);
    }

    @Override
    public List<FrontendPerformance> getFrontendPerformanceByWrapper(LambdaQueryWrapper<FrontendPerformance> queryWrapper) {
        return frontendPerformanceMapper.selectList(queryWrapper);
    }
}
