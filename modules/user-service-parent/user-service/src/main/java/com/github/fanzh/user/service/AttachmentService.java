package com.github.fanzh.user.service;

import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.user.api.constant.AttachmentConstant;
import com.github.fanzh.user.mapper.AttachmentMapper;
import com.github.fanzh.user.uploader.UploadInvoker;
import com.github.fanzh.user.api.module.Attachment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * @author fanzh
 * @date 2018/10/30 20:55
 */
@Slf4j
@AllArgsConstructor
@Service
public class AttachmentService extends BaseService<AttachmentMapper, Attachment> {


    /**
     * 下载
     *
     * @param attachment attachment
     * @return InputStream
     */
    public InputStream download(Attachment attachment) throws Exception {
        // 下载附件
        return UploadInvoker.getInstance().download(attachment);
    }


    /**
     * 获取附件的预览地址
     *
     * @param id attachmentId
     * @return String
     * @author fanzh
     * @date 2019/06/21 17:45
     */
    public String getPreviewUrl(Long id) {
        if (ParamsUtil.isEmpty(id)) {
            return "";
        }
        Attachment attachment = this.selectById(id);
        if (ParamsUtil.isEmpty(attachment)) {
            return "";
        }
        String preview = attachment.getPreviewUrl();
        if (StringUtils.isNotBlank(preview) && !preview.startsWith("http")) {
            preview = "http://" + preview;
        } else {
            preview = AttachmentConstant.ATTACHMENT_PREVIEW_URL + attachment.getId();
        }
        log.debug("GetPreviewUrl id: {}, preview url: {}", attachment.getId(), preview);
        return preview;
    }

}
