package com.qg.feign.clients;

import com.qg.common.domain.po.Project;
import com.qg.common.domain.po.Role;
import com.qg.common.domain.po.Users;
import com.qg.feign.domain.dto.UsersDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@FeignClient(value = "user-service")
public interface UserClient {

    @GetMapping("/users/findUserById")
    UsersDto findUserById(@RequestParam("id") Long id);

    @GetMapping("/users/findUserByIds")
    List<UsersDto> findUserByIds(@RequestParam("ids") Collection<Long> ids);

    /**
     * 批量查询用户
     *
     * @param userIds 用户id集合
     * @return 结果
     */
    @GetMapping("/users/selectBatchIds")
    List<Users> selectBatchIds(@RequestParam Set<Long> userIds);

    /**
     * 批量查询用户
     *
     * @param phone 电话号码
     * @return 结果
     */
    @GetMapping("/users/selectUserByPhone")
    Users selectUserById(String phone);

    /**
     * =================================================================================================================
     */

    @GetMapping("/projects/getProjectByUUId")
    Project getProjectById(@RequestParam String uuid);

    @GetMapping("/projects/checkProjectIdExist")
    boolean checkProjectIdExist(@RequestParam String projectId);

    @GetMapping("/projects/selectWebhookByProjectId")
    String selectWebhookByProjectId(@RequestParam String projectId);

//    @GetMapping("/roles/getRoleListByQueryWrapper")
//    List<Role> getRoleListByQueryWrapper(@RequestParam LambdaQueryWrapper<Role> queryWrapper);

    @GetMapping("/projects/getProjectByUUIds")
    List<Project> getProjectByUUIds(@RequestParam List<String> uuids);

    /**
     * 通过项目id查询权限
     *
     * @param projectId 项目id
     * @return 结果
     */
    @GetMapping("/roles/getRoleListByProjectId")
    List<Role> getRoleListByProjectId(@RequestParam String projectId);

    /**
     * 查询该用户在该项目下的权限
     *
     * @param projectId 项目id
     * @param userId    查询的用户
     * @return 结果
     */
    @GetMapping("/roles/getReceiverRoleInProject")
    Role getReceiverRoleInProject(@RequestParam String projectId, @RequestParam Long userId);

    /**
     * 查询该项目下的所有该用户权限的权限
     *
     * @param projectId 项目id
     * @param userRole  查询的用户
     * @return 结果
     */
    @GetMapping("/roles/getTheRoleListInProject")
    List<Role> getTheRoleListInProject(@RequestParam String projectId, @RequestParam Integer userRole);
}
