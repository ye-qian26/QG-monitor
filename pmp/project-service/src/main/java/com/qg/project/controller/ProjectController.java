package com.qg.project.controller;


import com.qg.common.domain.po.Result;
import com.qg.project.domain.dto.InviteDto;
import com.qg.common.domain.po.Project;
import com.qg.project.domain.vo.PersonalProjectVO;
import com.qg.project.service.ProjectService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
//@Tag(name ="项目")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建项目
     * @param personalProjectVO
     * @return
     */
    @PostMapping
    public Result addProject(@RequestBody PersonalProjectVO personalProjectVO){
        return projectService.addProject(personalProjectVO);
    }

    /**
     * 更新项目信息
     * @param project
     * @return
     */
    @PutMapping("/update")
    public Result updateProject(@RequestBody Project project){
        return projectService.updateProject(project);
    }

    /**
     * 删除项目
     * @param uuid
     * @return
     */
    @DeleteMapping
    public Result deleteProject(@RequestParam String uuid){
        return projectService.deleteProject(uuid);
    }

    /**
     * 获取项目详细信息
     * @param uuid
     * @return
     */
    @GetMapping("/getProject")
    public Result getProjectList(@RequestParam String uuid){
        return projectService.getProject(uuid);
    }

    /**
     * 获取公开项目
     * @return
     */
    @GetMapping("/getPublicProjectList")
    public Result getPublicProjectList(){
        return projectService.getPublicProjectList();
    }

    /**
     * 获取私有项目
     * @return
     */
    @GetMapping("/getPrivateProject")
    public Result getPrivateProjectList() {
        return projectService.getPrivateProjectList();
    }
    /**
     * 获取个人项目列表
     * @param userId
     * @return
     */
    @GetMapping("/getPersonalProject")
    public Result getPersonalProject(@RequestParam Long userId){
        return projectService.getPersonalProject(userId);
    }

//    /**
//     * 获取个人非公开项目列表
//     * @param userId
//     * @return
//     */
//    @GetMapping("/getPersonalUnpublicProject")
//    public Result getPersonalUnpublicProject(@RequestParam Long userId){
//        return ProjectService.getPersonalUnpublicProject(userId);
//    }

    /**
     * 获取项目邀请码
     * @param projectId
     * @return
     */
    @GetMapping("/getInviteCode")
    public Result getInviteCode(@RequestParam String projectId){
        return projectService.getInviteDCode(projectId);
    }

    /**
     * 加入项目
     * @param inviteDto
     * @return
     */
    @PostMapping("/joinProject")
    public Result joinProject(@RequestBody InviteDto inviteDto){
        return projectService.joinProject(inviteDto);
    }

    /**
     * 根据名称查询项目
     * @param name
     * @return
     */
    @GetMapping("/selectProjectByName")
    public Result selectProjectByName(@RequestParam String name){
        return projectService.selectProjectByName(name);
    }


    @GetMapping("/checkProjectIdExist")
    public boolean checkProjectIdExist(@RequestParam String projectId) {
        return projectService.checkProjectIdExist(projectId);
    }

    @GetMapping("/selectWebhookByProjectId")
    public String selectWebhookByProjectId(@RequestParam String projectId) {
        return projectService.selectWebhookByProjectId(projectId);
    }
    @GetMapping("/getProjectByUUId")
    public Project getProjectByUUId(@RequestParam String uuid) {
        return projectService.getProjectByUUId(uuid);
    }

    @GetMapping("/getProjectByUUIds")
    public List<Project> getProjectByUUIds(@RequestParam List<String> uuids) {
        return projectService.getProjectByUUIds(uuids);
    }
}
