package com.qg.alert.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Responsibility;
import com.qg.common.domain.po.Result;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ResponsibilityService {
    Result addResponsibility(Responsibility responsibility);

    Result getResponsibilityList(String projectId);

    Result selectByRespId(Long responsibleId);

    Result updateResponsibility(Responsibility responsibility);

    Result deleteResponsibility(Long id);

    Result selectResponsibleError(String projectId, Long responsibleId, String errorType, String platform);

    Result updateHandleStatus(Responsibility responsibility);

    Result selectHandleStatus(String projectId, String errorType, String platform);

    int deleteUserId(Long userId);

    Responsibility getResponsibilityByWrapper(LambdaQueryWrapper<Responsibility> queryWrapper);

    Integer updateResponsibilityByWrapper(Responsibility responsibility, LambdaQueryWrapper<Responsibility> queryWrapper);

    List<Responsibility> getResponsibilityListByWrapper(LambdaQueryWrapper<Responsibility> queryWrapper);

    List<Responsibility> getResponsibilityListByProjectId(String projectId, String platform);

    Responsibility getResponsibility(String projectId, String errorType);

    Responsibility getResponsibilityFromPlatform(@RequestParam String projectId, @RequestParam String errorType, @RequestParam String platform);

    boolean updateResponsibility(String projectId, String errorType, String platform, Long errorId);

    boolean signResponsibilityNoHandle(String projectId, String errorType, String platform);
}
