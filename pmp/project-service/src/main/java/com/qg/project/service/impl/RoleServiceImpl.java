package com.qg.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Code;

import com.qg.common.domain.po.Responsibility;
import com.qg.common.domain.po.Result;

import com.qg.feign.clients.AlertClient;
import com.qg.feign.clients.UserClient;
import com.qg.feign.dto.UsersDto;
import com.qg.common.domain.po.Project;
import com.qg.common.domain.po.Role;
import com.qg.project.domain.vo.ProjectMemberVO;

import com.qg.project.domain.vo.RoleVO;
import com.qg.project.mapper.ProjectMapper;
import com.qg.project.mapper.RoleMapper;


import com.qg.project.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.qg.common.utils.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    /*@Autowired
    private UsersMapper usersMapper;*/

    private final UserClient usersClient;

    @Autowired
    private ProjectMapper projectMapper;



    /*
    @Autowired
    private ResponsibilityMapper responsibilityMapper;
    */

    private final AlertClient alertClient;

    @Override
    public Result addRole(Role role) {
        //判断该用户是否在项目中
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getUserId, role.getUserId()).eq(Role::getProjectId, role.getProjectId());
        if (roleMapper.selectOne(lqw) != null) {
            return new Result(Code.CONFLICT, "该用户已加入该项目");
        }
        //加入项目
        return roleMapper.insert(role) == 1 ? new Result(Code.CREATED, "添加成功") : new Result(Code.INTERNAL_ERROR, "添加失败");
    }

    @Override
    public Result updateRole(RoleVO roleVO) {
        // 参数校验
        if (roleVO == null) {
            return new Result(Code.BAD_REQUEST, "参数错误");
        }

        if (roleVO.getUserId() == null || roleVO.getProjectId() == null
                || roleVO.getProjectId().isEmpty() || roleVO.getCurrentUserId() == null) {
            return new Result(Code.BAD_REQUEST, "用户ID和项目ID不能为空");
        }

        try {
            Long currentUserId = roleVO.getCurrentUserId();
            Long userId = roleVO.getUserId();
            String projectId = roleVO.getProjectId();
            // 检查处于登录状态下的用户在项目中的身份
            Integer currentUserRole = getUserRole(currentUserId, projectId);
            Integer userRole = getUserRole(userId, projectId);
            // 检查用户是否在
            if (currentUserRole == null || userRole == null) {
                return new Result(Code.FORBIDDEN, "您没有权限修改用户身份");
            }
            // 判断所登录用户是否有资格修改对应用户角色
            if (userRole < currentUserRole) {
                // 所登录用户身份低于被修改用户身份
                return new Result(Code.FORBIDDEN, "您没有权限修改用户身份");
            }
            // 构建更新条件
            LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Role::getUserId, roleVO.getUserId())
                    .eq(Role::getProjectId, roleVO.getProjectId());

            Role role = BeanUtil.copyProperties(roleVO, Role.class);
            // 直接执行更新操作
            int updateResult = roleMapper.update(role, lqw);

            if (updateResult > 0) {
                log.info("更新用户角色成功: userId={}, projectId={}",
                        roleVO.getUserId(), roleVO.getProjectId());
                return new Result(Code.SUCCESS, "更新成功");
            } else {
                log.info("更新用户角色失败，用户未加入该项目: userId={}, projectId={}",
                        roleVO.getUserId(), roleVO.getProjectId());
                return new Result(Code.NOT_FOUND, "该用户未加入该项目");
            }
        } catch (Exception e) {
            log.error("更新用户角色失败: userId={}, projectId={}",
                    roleVO.getUserId(), roleVO.getProjectId(), e);
            return new Result(Code.INTERNAL_ERROR, "更新失败: " + e.getMessage());
        }
    }

    private Integer getUserRole(Long userId, String projectId) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getUserId, userId)
                .eq(Role::getProjectId, projectId);

        Role role = roleMapper.selectOne(queryWrapper);
        return role != null ? role.getUserRole() : null;
    }


    @Override
    public Result deleteRole(String projectId, Long userId) {
        // 参数校验
        if (projectId == null || projectId.isEmpty() || userId == null) {
            return new Result(Code.BAD_REQUEST, "用户ID和项目ID不能为空");
        }

        try {
            // 检查用户是否在项目中
            LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(Role::getUserId, userId)
                    .eq(Role::getProjectId, projectId);

            Role existingRole = roleMapper.selectOne(roleQueryWrapper);
            if (existingRole == null) {
                log.info("删除用户角色失败，用户未加入该项目: userId={}, projectId={}", userId, projectId);
                return new Result(Code.NOT_FOUND, "该用户未加入该项目");
            }

            // 删除role记录
            int deleteRoleResult = roleMapper.delete(roleQueryWrapper);

            // 删除responsibility中相关用户数据
            LambdaQueryWrapper<Responsibility> responsibilityQueryWrapper = new LambdaQueryWrapper<>();
            responsibilityQueryWrapper
                    .eq(Responsibility::getProjectId, projectId)
                    .and(wrapper -> wrapper
                            .eq(Responsibility::getDelegatorId, userId)
                            .or()
                            .eq(Responsibility::getResponsibleId, userId));

            //int deleteResponsibilityResult = responsibilityMapper.delete(responsibilityQueryWrapper);
            int deleteResponsibilityResult = alertClient.deleteByUserId(userId);

            if (deleteRoleResult > 0) {
                log.info("删除用户角色成功: userId={}, projectId={}, 删除责任链记录数: {}",
                        userId, projectId, deleteResponsibilityResult);
                return new Result(Code.SUCCESS, "删除成功");
            } else {
                log.warn("删除用户角色失败: userId={}, projectId={}", userId, projectId);
                return new Result(Code.INTERNAL_ERROR, "删除失败");
            }

        } catch (Exception e) {
            log.error("删除用户角色失败: userId={}, projectId={}", userId, projectId, e);
            return new Result(Code.INTERNAL_ERROR, "删除失败: " + e.getMessage());
        }
    }


    @Override
    public Result getMemberList(String projectId) {
        //获取项目成员
        LambdaQueryWrapper<Role> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Role::getProjectId, projectId);
        List<Role> list = roleMapper.selectList(lqw);
        if(list == null) {
            return new Result(Code.NOT_FOUND, "该项目下无成员或项目不存在");
        }
        List<ProjectMemberVO> projectMemberVOList = new ArrayList<>();
        for(Role role : list)
        {
            ProjectMemberVO vo = new ProjectMemberVO();
            Long id= role.getUserId();
            vo.setId(id);
/*            LambdaQueryWrapper<Users> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(Users::getId, id);
            Users users = usersMapper.selectOne(lqw1);*/

            UsersDto users = usersClient.findUserById(id);

            if(users != null){
                BeanUtils.copyProperties(users,vo);
                BeanUtils.copyProperties(role,vo);
                projectMemberVOList.add(vo);
            }

        }
        if(projectMemberVOList.size() == 0){
            return new Result(Code.NOT_FOUND, "该项目下无成员");
        }
        return new Result(Code.SUCCESS,projectMemberVOList,"查询成功");
    }


    //查询该项目该用户的角色与权限
    @Override
    public Result getRole(Long userId, String projectId) {
        // 参数校验
        if (userId == null || projectId == null || projectId.isEmpty()) {
            log.warn("参数为空: userId={}, projectId={}", userId, projectId);
            return new Result(Code.BAD_REQUEST, "参数不能为空");
        }

        try {
            // 查询项目信息
            LambdaQueryWrapper<Project> projectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            projectLambdaQueryWrapper.eq(Project::getUuid, projectId)
                    .eq(Project::getIsDeleted, false);
            Project project = projectMapper.selectOne(projectLambdaQueryWrapper);





            // 检查项目是否存在
            if (project == null) {
                log.warn("项目不存在或已被删除: projectId={}", projectId);
                return new Result(Code.NOT_FOUND, "项目不存在或已被删除");
            }

            // 查询用户在项目中的角色
            LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(Role::getUserId, userId)
                    .eq(Role::getProjectId, projectId);
            Role role = roleMapper.selectOne(roleQueryWrapper);

            // 如果用户在项目中没有角色，创建默认角色
            if (role == null) {
                Role defaultRole = new Role();
                defaultRole.setUserId(userId);
                defaultRole.setProjectId(projectId);

                // 根据项目是否公开设置默认权限
                if (project.getIsPublic()) {
                    defaultRole.setPower(PERMISSION_READ); // 公开项目默认权限
                } else {
                    defaultRole.setPower(PERMISSION_NOT_VISIBLE); // 私有项目默认权限
                }

                log.info("为用户创建默认角色: userId={}, projectId={}, power={}",
                        userId, projectId, defaultRole.getPower());
                return new Result(Code.SUCCESS, defaultRole, "查询成功");
            }

            log.info("查询到用户角色: userId={}, projectId={}, power={}",
                    userId, projectId, role.getPower());
            return new Result(Code.SUCCESS, role, "查询成功");

        } catch (Exception e) {
            log.error("查询用户角色时发生异常: userId={}, projectId={}", userId, projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询用户角色失败: " + e.getMessage());
        }
    }


    @Override
    public Result updateUserRole(Role role) {
        if(role.getId() == null){
            return new Result(Code.BAD_REQUEST, "参数id为空！");
        }
        if(role.getUserRole() == USER_ROLE_ADMIN){
            role.setPower(PERMISSION_OP);
            return roleMapper.updateById(role) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
        }
        else{
            return roleMapper.updateById(role) == 1 ? new Result(Code.CREATED, "更新成功") : new Result(Code.INTERNAL_ERROR, "更新失败");
        }
    }

    @Override
    public Result getBossCountByProjectId(String projectId) {
        // 参数校验
        if (projectId == null || projectId.isEmpty()) {
            return new Result(Code.BAD_REQUEST, "项目ID不能为空");
        }

        try {
            // 查询项目中的所有成员
            LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(Role::getProjectId, projectId);
            List<Role> roles = roleMapper.selectList(roleQueryWrapper);

            if (roles.isEmpty()) {
                return new Result(Code.NOT_FOUND, "该项目下无成员或项目不存在");
            }

            // 统计Boss数量
            int bossCount = (int) roles.stream()
                    .filter(role -> role.getUserRole() == USER_ROLE_BOSS || role.getUserRole() == USER_ROLE_ADMIN)
                    .count();

            log.info("项目 {} 中的可以管理的数量: {}", projectId, bossCount);
            return new Result(Code.SUCCESS, bossCount, "查询成功");

        } catch (Exception e) {
            log.error("查询Boss数量时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public List<Role> getRoleListByQueryWrapper(LambdaQueryWrapper<Role> queryWrapper) {
        return roleMapper.selectList(queryWrapper);
    }


}
