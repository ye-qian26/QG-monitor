package com.qg.user.service;


import com.qg.common.domain.po.Result;

import com.qg.common.domain.po.Project;
import com.qg.user.domain.dto.InviteDto;
import com.qg.user.domain.vo.PersonalProjectVO;

import java.util.List;


public interface ProjectService {
     Result addProject(PersonalProjectVO personalProjectVO);

     Result updateProject(Project project);

     Result deleteProject(String uuid);

     Result getProject(String uuid);

     Result getPublicProjectList();

     Result getPersonalProject(Long userId);

//     Result getPersonalUnpublicProject(Long userId);

    Result getInviteDCode(String projectId);

     Result joinProject(InviteDto inviteDto);

    Result selectProjectByName(String name);

    Result getPrivateProjectList();

    boolean checkProjectIdExist(String projectId);

    String selectWebhookByProjectId(String projectId);

    Project getProjectByUUId(String uuid);

    List<Project> getProjectByUUIds(List<String> uuids);
}
