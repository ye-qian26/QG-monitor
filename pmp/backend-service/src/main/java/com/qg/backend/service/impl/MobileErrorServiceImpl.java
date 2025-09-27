package com.qg.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.qg.backend.mapper.MobileErrorMapper;
import com.qg.backend.repository.MobileErrorRepository;
import com.qg.common.domain.po.Code;
import com.qg.common.domain.po.FrontendError;
import com.qg.common.domain.po.MobileError;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.vo.TransformDataVO;
import com.qg.common.domain.vo.UvBillDataVO;
import com.qg.common.utils.MathUtil;
import com.qg.backend.service.MobileErrorService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description: 移动错误应用  // 类说明
 * @ClassName: MobileErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:36   // 时间
 * @Version: 1.0     // 版本
 */

@Slf4j
@Service
public class MobileErrorServiceImpl implements MobileErrorService {

    @Autowired
    private MobileErrorMapper mobileErrorMapper;
    @Autowired
    private MobileErrorRepository mobileErrorRepository;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(Code.BAD_REQUEST, "参数错误");
        }
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(MobileError::getErrorType, type);
        }

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);


        return new Result(200, mobileErrors, "查询成功");
    }

    @Override
    public void receiveErrorFromSDK(String mobileErrorJSON) {
        try {
            mobileErrorRepository.statistics(JSONUtil.toBean(mobileErrorJSON, MobileError.class));
            log.info("mobile-error存入缓存成功");
        } catch (Exception e) {
            log.warn("mobile-error存入缓存失败,发生异常:{}", e.getMessage());
        }
    }

    /**
     * 网页端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @Override
    public Object[] getMobileErrorStats(String projectId) {
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        queryWrapper.ge(MobileError::getTimestamp, oneWeekAgo);

        Map<String, Double> transformDataVOList = new HashMap<>();
        Map<String, Double> uvBillDataVOList = new HashMap<>();

        Integer count = 0;

        for (MobileError mobileError : mobileErrors) {
            if (mobileError.getEvent() > 0 && mobileError.getErrorType() != null) {
                addToMap(mobileError, transformDataVOList);
                addToMap(mobileError, uvBillDataVOList);
                count += mobileError.getEvent();
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
     * app端，获取移动端错误统计
     *
     * @param projectId 项目id
     * @return 结果
     */
    @Override
    public Object[] getMobileErrorStatsPro(String projectId) {
        LambdaQueryWrapper<MobileError> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobileError::getProjectId, projectId);

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(queryWrapper);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        queryWrapper.ge(MobileError::getTimestamp, oneWeekAgo);

        Map<String, Double> transformDataVOList = new HashMap<>();
        Map<String, Double> uvBillDataVOList = new HashMap<>();

        Integer count = 0;

        for (MobileError mobileError : mobileErrors) {
            if (mobileError.getEvent() > 0 && mobileError.getErrorType() != null) {
                addToMap(mobileError, transformDataVOList);
                addToMap(mobileError, uvBillDataVOList);
                count += mobileError.getEvent();
            }

        }

        if (count == 0) {
            return new Object[0]; // 如果没有数据，直接返回空数组
        }

        Integer finalCount = count;

        uvBillDataVOList.entrySet().removeIf(entry -> entry.getValue() == 0);


        uvBillDataVOList.replaceAll((k, v) -> MathUtil.truncate(v / finalCount, 3));


        List<TransformDataVO> uvBillDataVOListPro = uvBillDataVOList.entrySet().stream()
                .map(entry -> new TransformDataVO(entry.getKey(), entry.getValue()))
                .toList();

        List<UvBillDataVO> transformDataVOListPro = transformDataVOList.entrySet().stream()
                .map(entry -> new UvBillDataVO(entry.getKey(), entry.getValue().intValue()))
                .toList();


        return new Object[]{transformDataVOListPro, uvBillDataVOListPro};
    }

    @Override
    public List<MobileError> getMobileErrorByWrapper(LambdaQueryWrapper<MobileError> wrapper) {
        return mobileErrorMapper.selectList(wrapper);
    }

    private static void addToMap(MobileError mobileError, Map<String, Double> transformDataVOList) {
        if (mobileError.getErrorType() == null || mobileError.getEvent() == 0) {
            return; // 如果错误类型或事件为空，则不处理
        }
        if (transformDataVOList.containsKey(mobileError.getErrorType())) {
            transformDataVOList.put(mobileError.getErrorType(), transformDataVOList.get(mobileError.getErrorType()) + mobileError.getEvent());
        } else {
            transformDataVOList.put(mobileError.getErrorType(), Double.valueOf(mobileError.getEvent()));
        }
    }

    /**
     * 通过错误id查询移动端错误
     *
     * @param errorId 错误id
     * @return 结果
     */
    @Override
    public List<MobileError> getMobileErrorByErrorId(Long errorId) {
        return mobileErrorMapper.selectList(new LambdaQueryWrapper<MobileError>().eq(MobileError::getId, errorId));
    }

    /**
     * 通过错误类型查询移动端错误
     *
     * @param errorTypes 错误id
     * @return 结果
     */
    @Override
    public List<MobileError> getMobileErrorByErrorType(List<String> errorTypes, String projectId) {
        if (projectId == null) {
            return mobileErrorMapper.selectList(new LambdaQueryWrapper<MobileError>().in(MobileError::getErrorType, errorTypes));

        }

        return mobileErrorMapper.selectList(
                new LambdaQueryWrapper<MobileError>()
                        .in(MobileError::getErrorType, errorTypes)
                        .eq(MobileError::getProjectId, projectId)
                        .orderByDesc(MobileError::getTimestamp));
    }
}
