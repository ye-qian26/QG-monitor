package com.qg.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.backend.aggregator.BackendErrorAggregator;
import com.qg.common.domain.po.BackendError;
import com.qg.backend.mapper.BackendErrorMapper;

import com.qg.backend.domain.po.Module;


import com.qg.backend.mapper.ModuleMapper;
import com.qg.backend.service.BackendErrorService;
import com.qg.backend.service.ModuleService;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.TransformDataVO;
import com.qg.common.domain.vo.UvBillDataVO;
import com.qg.common.utils.MathUtil;
import com.qg.feign.clients.ProjectClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qg.common.domain.po.Code.*;


/**
 * @Description: 后端错误应用  // 类说明
 * @ClassName: BackendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BackendErrorServiceImpl implements BackendErrorService {

    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

//    @Autowired
//    private BackendErrorAggregator backendErrorAggregator;
    @Autowired
    private ModuleService moduleService;
    //@Autowired
    //private ProjectService projectService;

    private final ProjectClient projectClient;

    @Override
    public Result selectByCondition(String projectId, Long moduleId, String type) {
        if (projectId == null) {
            return new Result(BAD_REQUEST, "参数不能为空");
        }

        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        Module module = moduleMapper.selectById(moduleId);
        if (module != null) {
            String moduleName = module.getModuleName();
            queryWrapper.eq(BackendError::getModule, moduleName);
        } else {
            return new Result(BAD_REQUEST, "模块不存在");
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(BackendError::getErrorType, type);
        }
        queryWrapper.eq(BackendError::getProjectId, projectId);

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);

        return new Result(SUCCESS, backendErrors, "查询成功");

    }

    @Override
    public Integer saveBackendError(BackendError backendError) {
        String projectId = backendError.getProjectId();
        String moduleName = backendError.getModule();
        if (backendError == null || projectId == null) {
            return 0; // 返回0表示没有数据需要保存
        }
        moduleService.putModuleIfAbsent(moduleName, projectId);

        return backendErrorMapper.insert(backendError);
    }


    @Override
    public Result addBackendError(String errorData) {
        if (errorData == null) {
            log.error("参数为空");
            return new Result(BAD_REQUEST, "参数为空");
        }

        try {
            BackendError backendError = JSONUtil.toBean(errorData, BackendError.class);
            if (backendError.getProjectId() == null ||
                !projectClient.checkProjectIdExist(Long.valueOf(backendError.getProjectId())) ||
                backendError.getErrorType() == null ||
                backendError.getEnvironment() == null) {
                log.error("参数错误");
                return new Result(BAD_REQUEST, "参数错误");
            }

            // 设置当前时间戳（如果未设置）
            if (backendError.getTimestamp() == null) {
                backendError.setTimestamp(LocalDateTime.now());
            }

            // 添加到 Redis 聚合器缓存中
            backendErrorAggregator.addErrorToCache(backendError);
            return new Result(SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息时出错，错误信息： {}", errorData, e);
            return new Result(INTERNAL_ERROR, "添加错误信息失败");
        }
    }

    /**
     * web端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return  结果
     */
    @Override
    public Object[] getBackendErrorStats(String projectId) {
        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendError::getProjectId, projectId);

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);


        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        queryWrapper.ge(BackendError::getTimestamp, oneWeekAgo);

        Map<String, Double> transformDataVOList = new HashMap<>();
        Map<String, Double> uvBillDataVOList = new HashMap<>();

        Integer count = 0;

        for (BackendError backendError : backendErrors) {
            if (backendError.getEvent() != null && backendError.getErrorType() != null) {
                addToMap(backendError, transformDataVOList);
                addToMap(backendError, uvBillDataVOList);
                count += backendError.getEvent();
            }

        }


        if (count == 0) {
            return new Object[0]; // 如果没有数据，直接返回空数组
        }

        Integer finalCount = count;

        uvBillDataVOList.entrySet().removeIf(entry -> entry.getValue() == 0);

        uvBillDataVOList.replaceAll((k, v) -> MathUtil.truncate(v / finalCount, 3));

        return new Object[]{transformDataVOList, uvBillDataVOList};
    }

    /**
     * app端，获取后端错误统计
     *
     * @param projectId 项目id
     * @return  结果
     */
    @Override
    public Object[] getBackendErrorStatsPro(String projectId) {
        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendError::getProjectId, projectId);

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);


        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        queryWrapper.ge(BackendError::getTimestamp, oneWeekAgo);

        Map<String, Double> transformDataVOList = new HashMap<>();
        Map<String, Double> uvBillDataVOList = new HashMap<>();

        Integer count = 0;

        for (BackendError backendError : backendErrors) {
            if (backendError.getEvent() != null && backendError.getErrorType() != null) {
                addToMap(backendError, transformDataVOList);
                addToMap(backendError, uvBillDataVOList);
                count += backendError.getEvent();
            }

        }

        if (count == 0) {
            return new Object[0]; // 如果没有数据，直接返回空数组
        }

        Integer finalCount = count;

        uvBillDataVOList.entrySet().removeIf(entry -> entry.getValue() == 0);


        uvBillDataVOList.replaceAll((k, v) -> MathUtil.truncate(v / finalCount, 3));


        List<UvBillDataVO> uvBillDataVOs = new ArrayList<>();
        List<TransformDataVO> transformDataVOs = new ArrayList<>();

        transformDataVOList.forEach((key, value) -> {
            UvBillDataVO uvBillDataVO = new UvBillDataVO();
            uvBillDataVO.setErrorType(key);
            uvBillDataVO.setCount(value.intValue());
            uvBillDataVOs.add(uvBillDataVO);
        });


        uvBillDataVOList.forEach((key, value) -> {
            TransformDataVO transformDataVO = new TransformDataVO();
            transformDataVO.setErrorType(key);
            transformDataVO.setRatio(value);
            transformDataVOs.add(transformDataVO);
        });


        return new Object[]{uvBillDataVOs, transformDataVOs};
    }

    @Override
    public List<BackendError> getBackendErrorByWrapper(LambdaQueryWrapper<BackendError> queryWrapper) {
        return backendErrorMapper.selectList(queryWrapper);
    }

    private static void addToMap(BackendError backendError, Map<String, Double> transformDataVOList) {
        if (backendError.getErrorType() == null || backendError.getEvent() == null) {
            return; // 如果错误类型或事件为空，则不处理
        }
        if (transformDataVOList.containsKey(backendError.getErrorType())) {
            transformDataVOList.put(backendError.getErrorType(), transformDataVOList.get(backendError.getErrorType()) + backendError.getEvent());

        } else {
            transformDataVOList.put(backendError.getErrorType(), Double.valueOf(backendError.getEvent()));

        }
    }


}
