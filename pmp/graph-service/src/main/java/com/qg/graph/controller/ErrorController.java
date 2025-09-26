package com.qg.graph.controller;

import com.qg.common.domain.po.Result;
import com.qg.feign.clients.BackendClient;
import com.qg.feign.clients.FrontendClient;
import com.qg.feign.clients.MobileClient;
import com.qg.graph.service.AllErrorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "错误信息")
@RestController
@RequestMapping("/errors")
@AllArgsConstructor
public class ErrorController {

//    @Autowired
//    private ErrorService errorService;
    @Autowired
    private AllErrorService allErrorService;
    private final BackendClient backendClient;
    private final FrontendClient frontendClient;
    private final MobileClient mobileClient;

    /**
     * 根据条件查询错误信息
     * @param projectId 项目id
     * @param errorType 错误类型
     * @param platform  来源
     * @return  结果
     */
    @GetMapping("/selectByCondition")
    public Result selectByCondition(@RequestParam String projectId,
                                    @RequestParam(required = false) String errorType, @RequestParam(required = false) String platform) {

        if (platform == null || platform.isEmpty()) {
            return allErrorService.selectByCondition(projectId, errorType);
        }
        switch (platform) {
            case "backend":
                return backendClient.selectBackendResponsibilityByCondition(projectId, errorType);
            case "frontend":
                return frontendClient.selectFrontResponsibilityByCondition(projectId, errorType);
            case "mobile":
                return mobileClient.selectMobileResponsibilityByCondition(projectId, errorType);
            default:
                return new Result(400, "不支持的平台类型");
        }
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        return allErrorService.selectById(id);
    }

    /**
     * 根据id和platform查询错误信息详情
     *
     * @param errorId   错误id
     * @param platform  来源
     * @return  结果
     */
    @GetMapping("/selectErrorDetail")
    public Result selectErrorDetail(@RequestParam Long errorId, @RequestParam String platform) {
        return allErrorService.selectErrorDetail(errorId, platform);
    }
}
