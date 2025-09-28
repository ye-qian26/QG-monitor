package com.qg.feign.clients;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Project;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.po.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: ProjectClient    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/9/22 10:53   // 时间
 * @Version: 1.0     // 版本
 */
@FeignClient("project-service")
public interface ProjectClient {
//
//    @GetMapping("/projects/getProjectByUUId")
//    Project getProjectById(@RequestParam String uuid);
//
//    @GetMapping("/projects/checkProjectIdExist")
//    boolean checkProjectIdExist(@RequestParam String projectId);
//
//    @GetMapping("/projects/selectWebhookByProjectId")
//    String selectWebhookByProjectId(@RequestParam String projectId);
//
////    @GetMapping("/roles/getRoleListByQueryWrapper")
////    List<Role> getRoleListByQueryWrapper(@RequestParam LambdaQueryWrapper<Role> queryWrapper);
//
//    @GetMapping("/projects/getProjectByUUIds")
//    List<Project> getProjectByUUIds(@RequestParam List<String> uuids);
//
//    /**
//     * 通过项目id查询权限
//     *
//     * @param projectId 项目id
//     * @return 结果
//     */
//    @GetMapping("/roles/getRoleListByProjectId")
//    List<Role> getRoleListByProjectId(@RequestParam String projectId);
//
//    /**
//     * 查询该用户在该项目下的权限
//     *
//     * @param projectId 项目id
//     * @param userId    查询的用户
//     * @return 结果
//     */
//    @GetMapping("/roles/getReceiverRoleInProject")
//    Role getReceiverRoleInProject(@RequestParam String projectId, @RequestParam Long userId);
//
//    /**
//     * 查询该项目下的所有该用户权限的权限
//     *
//     * @param projectId 项目id
//     * @param userRole    查询的用户
//     * @return 结果
//     */
//    @GetMapping("/roles/getTheRoleListInProject")
//    List<Role> getTheRoleListInProject(@RequestParam String projectId, @RequestParam Integer userRole);
}
