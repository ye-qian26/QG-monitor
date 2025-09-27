package com.qg.backend.mapper;

import com.qg.backend.domain.po.Error;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorMapper extends BaseMapper<Error> {
}
