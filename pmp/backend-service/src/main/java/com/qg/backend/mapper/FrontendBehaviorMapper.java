package com.qg.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.qg.backend.domain.po.FrontendBehavior;
import com.qg.common.domain.vo.ButtonVO;
import com.qg.common.domain.vo.FrontendBehaviorVO;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 前端行为mapper  // 类说明
 * @ClassName: FrontendBehaviorMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:26   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface FrontendBehaviorMapper extends BaseMapper<FrontendBehavior> {

    @Select("""
            SELECT
                route_data->>'route' AS route,
                AVG((route_data->>'totalTime')::BIGINT) AS avg_total_time,
                AVG((route_data->>'visibleTime')::BIGINT) AS avg_visible_time,
                COUNT(*) AS samples
            FROM (
                SELECT 
                    jsonb_array_elements(breadcrumbs) AS route_data
                FROM frontend_behavior
                WHERE project_id = #{projectId}
                    AND timestamp BETWEEN #{startTime} AND #{endTime}
            ) AS expanded_data
            WHERE route_data->>'message' = 'Page stay time recorded'
            GROUP BY route_data->>'route'
            """)
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT
                route_data->>'route' AS route,
                AVG((route_data->>'totalTime')::BIGINT) AS avg_total_time,
                AVG((route_data->>'visibleTime')::BIGINT) AS avg_visible_time,
                COUNT(*) AS samples
            FROM (
                SELECT 
                    jsonb_array_elements(breadcrumbs) AS route_data
                FROM frontend_behavior
                WHERE project_id = #{projectId, jdbcType=VARCHAR}
                    AND timestamp BETWEEN #{startTime, jdbcType=TIMESTAMP} AND #{endTime, jdbcType=TIMESTAMP}
            ) AS expanded_data
            WHERE route_data->>'message' = 'Page stay time recorded'
                AND (CAST(#{route, jdbcType=VARCHAR} AS TEXT) IS NULL
                     OR route_data->>'route' = CAST(#{route, jdbcType=VARCHAR} AS TEXT))
            GROUP BY route_data->>'route'
            """)
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute(
            @Param("projectId") String projectId,
            @Param("route") String route,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT
                button_data->>'id' AS buttonId,
                SUM(event) AS eventCount
            FROM (
                SELECT 
                    event,
                    jsonb_array_elements(breadcrumbs) AS button_data
                FROM pmp.frontend_behavior
                WHERE project_id = #{projectId}
            ) AS expanded_data
            WHERE button_data->>'tagName' = 'BUTTON'
            GROUP BY button_data->>'id'
            """)
    List<ButtonVO> queryFrontendButton(@Param("projectId") String projectId);
}