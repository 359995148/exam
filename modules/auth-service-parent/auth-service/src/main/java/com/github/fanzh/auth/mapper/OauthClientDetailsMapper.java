package com.github.fanzh.auth.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.auth.api.module.OauthClientDetails;
import org.apache.ibatis.annotations.Mapper;

/**
 * Oauth2客户端mapper
 *
 * @author fanzh
 * @date 2019/3/30 16:39
 */
@Mapper
public interface OauthClientDetailsMapper extends BaseMapper<OauthClientDetails> {
}
