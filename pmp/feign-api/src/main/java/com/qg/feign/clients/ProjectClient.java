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

    @GetMapping("/projects/getProjectByUUId")
    Project getProjectById(@RequestParam String uuid);

    @GetMapping("/projects/checkProjectIdExist")
    boolean checkProjectIdExist(@RequestParam Long projectId);

    @GetMapping("/projects/selectWebhookByProjectId")
    String selectWebhookByProjectId(@RequestParam String projectId);

    @GetMapping("/roles/getRoleListByQueryWrapper")
    List<Role> getRoleListByQueryWrapper(@RequestParam LambdaQueryWrapper<Role> queryWrapper);

    @GetMapping("/projects/getProjectByUUIds")
    List<Project> getProjectByUUIds(@RequestParam List<String> uuids);

}
