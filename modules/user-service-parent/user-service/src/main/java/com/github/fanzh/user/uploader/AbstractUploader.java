package com.github.fanzh.user.uploader;

import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.core.utils.SpringContextHolder;
import com.github.fanzh.user.api.module.Attachment;
import com.github.fanzh.user.service.AttachmentService;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;

/**
 * @author fanzh
 * @date 2020/04/05 13:37
 */
public abstract class AbstractUploader implements IUploader {

    @Override
    public void save(Attachment attachment) {
        SpringContextHolder.getApplicationContext().getBean(AttachmentService.class).baseSave(attachment);
    }

    @Override
    public void delete(Attachment attachment) {
        SpringContextHolder.getApplicationContext().getBean(AttachmentService.class).baseLogicDelete(attachment.getId());
    }

    @Override
    public abstract Attachment upload(Attachment attachment, byte[] bytes);

    @Override
    public abstract InputStream download(Attachment attachment);

    /**
     * 获取附件存储目录
     *
     * @param attachment attachment
     * @param id         id
     * @return String
     */
    public String getFileRealDirectory(Attachment attachment, String id) {
        String applicationCode = attachment.getApplicationCode();
        String busiId = attachment.getBusiId();
        String fileName = attachment.getAttachName();
        String fileRealDirectory = SpringContextHolder.getApplicationContext().getBean(SysProperties.class).getAttachPath() + File.separator
                + applicationCode + File.separator;
        // 有分类就加上
        if (StringUtils.isNotBlank(attachment.getBusiModule())) {
            String busiModule = attachment.getBusiModule();
            fileRealDirectory = fileRealDirectory + busiModule + File.separator;
        }
        if (StringUtils.isNotBlank(attachment.getBusiType())) {
            String busiType = attachment.getBusiType();
            fileRealDirectory = fileRealDirectory + busiType + File.separator;
        }
        fileRealDirectory = fileRealDirectory + busiId;
        return fileRealDirectory;
    }
}
