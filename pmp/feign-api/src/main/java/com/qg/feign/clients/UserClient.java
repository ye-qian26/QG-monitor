package com.qg.feign.clients;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    @GetMapping("/selectBatchIds")
    List<Users> selectBatchIds(@RequestParam Set<Long> userIds);

    /**
     * 批量查询用户
     *
     * @param queryWrapper 查询器
     * @return 结果
     */
    @GetMapping("/selectList")
    List<Users> selectList(LambdaQueryWrapper<Users> queryWrapper);
}
