package com.qg.backend.service.impl;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.backend.aggregator.FrontendErrorAggregator;
import com.qg.backend.mapper.FrontendErrorMapper;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.ErrorTrendVO;
import com.qg.common.domain.vo.ManualTrackingVO;
import com.qg.common.domain.vo.TransformDataVO;
import com.qg.common.domain.vo.UvBillDataVO;
import com.qg.common.utils.MathUtil;


import com.qg.common.domain.po.FrontendError;
import com.qg.backend.service.FrontendErrorService;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.common.domain.po.Code.*;


/**
 * @Description: 前端错误应用  // 类说明
 * @ClassName: FrontendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendErrorServiceImpl implements FrontendErrorService {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private FrontendErrorAggregator frontendErrorAggregator;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null) {
            return new Result(400, "参数不能为空");
        }
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(FrontendError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(FrontendError::getErrorType, type);
        }

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        return new Result(200, frontendErrors, "查询成功");
    }

    @Override
    public Integer saveFrontendError(List<FrontendError> frontendErrors) {
        if (frontendErrors == null || frontendErrors.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (FrontendError error : frontendErrors) {
            count += frontendErrorMapper.insert(error);
        }

        return frontendErrors.size() == count ? count : 0; // 返回保存的记录数
    }

    @Override
    public Result addFrontendError(String errorData) {
        if (errorData == null) {
            log.error("参数为空");
            return new Result(BAD_REQUEST, "参数为空");
        }

        try {
            List<FrontendError> frontendErrorList = JSONUtil.toList(errorData, FrontendError.class);
            System.out.println("前端错误信息list: " + frontendErrorList);

            log.debug("前端错误信息list长度： {}", frontendErrorList.size());
            for (FrontendError frontendError : frontendErrorList) {
                if (frontendError.getProjectId() == null ||
                    frontendError.getErrorType() == null ||
                    frontendError.getSessionId() == null
                ) {
                    log.error("参数错误");
                    return new Result(BAD_REQUEST, "参数错误");
                }

                // 设置当前时间戳（如果未设置）
                if (frontendError.getTimestamp() == null) {
                    frontendError.setTimestamp(LocalDateTime.now());
                }

                // 添加到 Redis 聚合器缓存中
                frontendErrorAggregator.addErrorToCache(frontendError);
            }
            return new Result(SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息时出错，错误信息： {}", errorData, e);
            return new Result(INTERNAL_ERROR, "添加错误信息失败");
        }
    }

    /**
     * 获取两种前端错误信息
     *
     * @param projectId 项目id
     * @return 结果
     */
    @Override
    public Object[] getErrorStats(String projectId) {

        List<UvBillDataVO> uvBillDataVOList = new ArrayList<>();
        List<TransformDataVO> transformDataVOList = new ArrayList<>();
        frontendErrorMapper
                .queryFrontendErrorStats(projectId)
                .forEach(errorStat -> {
                    uvBillDataVOList.add(new UvBillDataVO(errorStat.getErrorType(), errorStat.getCount()));
                    transformDataVOList.add(new TransformDataVO(errorStat.getErrorType(), MathUtil.truncate(errorStat.getRatio(), 3)));
                });

        return new Object[]{uvBillDataVOList, transformDataVOList};
    }

    /**
     * 获取前端api平均响应时间
     *
     * @param projectId 项目id
     * @param timeType  时间类型
     * @return 结果
     */
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
     * 按时间（允许按照时间筛选）以及错误类别（前端/后端/移动）展示错误量
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @Override
    public List<ErrorTrendVO> getErrorTrend
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendErrorMapper.queryErrorTrend(projectId, startTime, endTime);
    }

    /**
     * 获取埋点错误统计
     *
     * @param projectId 项目id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    @Override
    public List<ManualTrackingVO> queryManualTrackingStats(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime) {
        return frontendErrorMapper.queryManualTrackingStats(projectId, startTime, endTime);
    }

    @Override
    public List<FrontendError> getFrontendErrorByWrapper(LambdaQueryWrapper<FrontendError> queryWrapper) {
        return frontendErrorMapper.selectList(queryWrapper);
    }

    /**
     * 通过错误id查询前端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @Override
    public List<FrontendError> getFrontendErrorByErrorId(Long errorId) {
        return frontendErrorMapper.selectList(new LambdaQueryWrapper<FrontendError>().eq(FrontendError::getId, errorId));
    }

    /**
     * 通过错误类型查询前端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @Override
    public List<FrontendError> getFrontendErrorByErrorType(List<String> errorTypes, String projectId) {
        if (projectId == null) {
            return frontendErrorMapper.selectList(new LambdaQueryWrapper<FrontendError>().in(FrontendError::getErrorType, errorTypes));

        }

        return frontendErrorMapper.selectList(
                new LambdaQueryWrapper<FrontendError>()
                        .eq(FrontendError::getProjectId, projectId)
                        .in(FrontendError::getErrorType, errorTypes)
                        .orderByDesc(FrontendError::getTimestamp)
        );
    }
}
