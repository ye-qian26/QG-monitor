package com.qg.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.qg.common.domain.po.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT webhook FROM project WHERE uuid = #{projectId} AND is_deleted = false")
    String selectWebhookByProjectId(@Param("projectId") String projectId);
}
