package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.user.api.module.Attachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 附件mapper
 *
 * @author fanzh
 * @date 2018/10/30 20:55
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {
}
