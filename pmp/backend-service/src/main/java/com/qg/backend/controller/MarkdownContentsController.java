package com.qg.backend.controller;


import com.qg.backend.service.MarkdownContentsService;
import com.qg.common.domain.po.Result;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SDK说明文档")
@RequestMapping("/markdownContents")
public class MarkdownContentsController {

    @Autowired
    private MarkdownContentsService markdownContentsService;


    @GetMapping("/select/{platform}")
    public Result select(@PathVariable String platform) {
        return markdownContentsService.select(platform);
    }
}
