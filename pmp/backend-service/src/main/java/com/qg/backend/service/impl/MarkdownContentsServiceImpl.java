package com.qg.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.qg.backend.domain.po.MarkdownContents;
import com.qg.backend.mapper.MarkdownContentsMapper;
import com.qg.common.domain.po.Code;
import com.qg.common.domain.po.Result;
import com.qg.backend.service.MarkdownContentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qg.common.domain.po.Code.SUCCESS;


@Service
@Slf4j
public class MarkdownContentsServiceImpl implements MarkdownContentsService {

    @Autowired
    private MarkdownContentsMapper markdownContentsMapper;

    @Override
    public Result select(String platform) {
        if (platform == null || platform.isEmpty()) {
            return new Result(Code.BAD_REQUEST, "参数错误");
        }
        LambdaQueryWrapper<MarkdownContents> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MarkdownContents::getPlatform, platform);
        MarkdownContents markdownContents = markdownContentsMapper.selectOne(queryWrapper);
        log.info("md文件查询成功");

        return new Result(SUCCESS, markdownContents, "md文件查询成功");
    }
}
