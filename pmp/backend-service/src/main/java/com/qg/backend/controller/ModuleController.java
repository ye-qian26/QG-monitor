package com.qg.backend.controller;


import com.qg.backend.domain.po.Module;
import com.qg.backend.service.ModuleService;
import com.qg.common.domain.po.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "模块管理")
@RestController
@RequestMapping("/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    /**
     * 添加模块
     * @param module
     * @return
     */
    @PostMapping("/addModule")
    public Result addModule(@RequestBody Module module) {
        return moduleService.addModule(module);
    }

    /**
     * 根据项目id查询模块
     * @param projectId
     * @return
     */
    @GetMapping("/selectByProjectId")
    public Result selectByProjectId(@RequestParam String projectId) {
        return moduleService.selectByProjectId(projectId);
    }

    /**
     * 删除模块
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById")
    public Result deleteById(@RequestParam Long id) {
        return moduleService.deleteById(id);
    }
}
