package com.qg.backend.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.common.domain.po.Result;
import com.qg.common.domain.po.SourcemapFiles;
import org.springframework.web.multipart.MultipartFile;

public interface SourcemapFilesService {
    Result uploadFile(String projectId, String timestamp, String version, String buildVersion, MultipartFile[] files, String[] jsFilenames, String fileHashes);

    SourcemapFiles selectOneSourcemapFileByQueryWrapper(LambdaQueryWrapper<SourcemapFiles> QueryWrapper);
}
