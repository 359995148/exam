package com.github.fanzh.user.uploader;

import com.github.fanzh.user.api.module.Attachment;

import java.io.InputStream;

/**
 * @author fanzh
 * @date 2020/04/05 13:36
 */
public interface IUploader {

    /**
     * 上传附件
     * @param attachment attachment
     * @param bytes bytes
     * @return Attachment
     */
    Attachment upload(Attachment attachment, byte[] bytes);

    /**
     * 保存附件信息
     * @param attachment attachment
     * @return int
     */
    void save(Attachment attachment);

    /**
     * 下载附件
     * @param attachment attachment
     * @return InputStream
     */
    InputStream download(Attachment attachment);

    /**
     * 删除附件
     * @param attachment attachment
     * @return boolean
     */
    void delete(Attachment attachment);

}
