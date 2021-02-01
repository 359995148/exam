package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.basic.vo.AttachmentVo;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.FileUtil;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.Servlets;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.module.Attachment;
import com.github.fanzh.user.service.AttachmentService;
import com.github.fanzh.user.uploader.UploadInvoker;
import com.google.common.net.HttpHeaders;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 附件信息管理
 *
 * @author fanzh
 * @date 2018/10/30 20:45
 */
@Slf4j
@AllArgsConstructor
@Api("附件信息管理")
@RestController
@RequestMapping("/v1/attachment")
public class AttachmentController {

    private final AttachmentService attachmentService;

    private final SysProperties sysProperties;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/01/01 19:56
     */
    @ApiImplicitParam(name = "id", value = "附件ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取附件信息", notes = "根据附件id获取附件详细信息")
    @GetMapping("/{id}")
    public ExecResult<Attachment> get(@PathVariable Long id) {
        return new ExecResult<>(attachmentService.baseGetById(id));
    }

    @ApiOperation(value = "附件列表或分页查询")
    @GetMapping("attachmentList")
    public ExecResult<Page<Attachment>> userList(
            PageEntity pageEntity
            , Attachment attachment
    ) {
        attachment.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(attachmentService.baseListOrPage(OptionalUtil.build(pageEntity), attachment));
    }

    /**
     * 上传文件
     *
     * @param file       file
     * @param attachment attachment
     * @author fanzh
     * @date 2018/10/30 21:54
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "busiType", value = "业务分类", dataType = "String"),
            @ApiImplicitParam(name = "busiId", value = "业务Id", dataType = "String"),
            @ApiImplicitParam(name = "busiModule", value = "业务模块", dataType = "String"),
    })
    @ApiOperation(value = "上传文件", notes = "上传文件")
    @PostMapping("upload")
    public ExecResult<Attachment> upload(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file
            , Attachment attachment
    ) {
        if (ParamsUtil.isEmpty(file)) {
            return ExecResultUtil.success(attachment);
        }
        attachment.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
        attachment.setAttachType(FileUtil.getFileNameEx(file.getOriginalFilename()));
        attachment.setAttachSize(String.valueOf(file.getSize()));
        attachment.setAttachName(file.getOriginalFilename());
        attachment.setBusiId(attachment.getId().toString());
        try {
            attachment = UploadInvoker.getInstance().upload(attachment, file.getBytes());
        } catch (Exception e) {
            log.error("upload attachment error: {}", e.getMessage());
        }
        return new ExecResult<>(attachment);
    }

    /**
     * 下载文件
     *
     * @param id id
     * @author fanzh
     * @date 2018/10/30 22:26
     */
    @ApiImplicitParam(name = "id", value = "附件ID", required = true, dataType = "Long")
    @ApiOperation(value = "下载附件", notes = "根据ID下载附件")
    @GetMapping("download")
    public void download(HttpServletRequest request, HttpServletResponse response, @NotBlank Long id) {
        try {
            Attachment attachment = attachmentService.baseGetById(id);
            InputStream inputStream = UploadInvoker.getInstance().download(attachment);
            if (inputStream == null) {
                log.info("Attachment does not exists by attachmentId: {}", id);
                return;
            }
            OutputStream outputStream = response.getOutputStream();
            response.setContentType("application/zip");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=10");
            // IE之外的浏览器使用编码输出名称
            String contentDisposition = "";
            String httpUserAgent = request.getHeader("User-Agent");
            if (StringUtils.isNotEmpty(httpUserAgent)) {
                httpUserAgent = httpUserAgent.toLowerCase();
                String fileName = attachment.getAttachName();
                contentDisposition = httpUserAgent.contains("wps") ? "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") : Servlets.getDownName(request, fileName);
            }
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            response.setContentLength(inputStream.available());
            FileCopyUtils.copy(inputStream, outputStream);
            log.info("download {} success", attachment.getAttachName());
        } catch (java.lang.Exception e) {
            log.error("Download attachment failed: {}", e.getMessage());
        }
    }

    /**
     * 删除附件
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/10/30 22:44
     */
    @ApiImplicitParam(name = "id", value = "附件ID", required = true, paramType = "path")
    @ApiOperation(value = "删除附件", notes = "根据ID删除附件")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        Attachment attachment = attachmentService.selectById(id);
        return ExecResultUtil.success(UploadInvoker.getInstance().delete(attachment));
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/4 10:01
     */
    @ApiImplicitParam(name = "ids", value = "附件ID", dataType = "Long")
    @ApiOperation(value = "批量删除附件", notes = "根据附件id批量删除附件")
    @PostMapping("/deleteAll")
    public ExecResult<Boolean> deleteAttachment(@RequestBody List<Long> ids) {
        UploadInvoker.getInstance().deleteAttachment(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 根据附件ID批量查询
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/01/01 22:16
     */
    @ApiImplicitParam(name = "ids", value = "附件ID", dataType = "Long")
    @ApiOperation(value = "批量查询附件信息", notes = "根据附件ID批量查询附件信息")
    @PostMapping(value = "findById")
    public ExecResult<List<AttachmentVo>> findById(@RequestBody List<Long> ids) {
        List<Attachment> attachmentList = attachmentService.selectBatchIds(ids);
        List<AttachmentVo> attachmentVoList = JsonUtil.listToList(attachmentList, AttachmentVo.class);
        return ExecResultUtil.success(attachmentVoList);
    }

    /**
     * 是否支持预览
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/19 15:47
     */
    @GetMapping("/{id}/canPreview")
    @ApiOperation(value = "判断附件是否支持预览", notes = "根据附件ID判断附件是否支持预览")
    @ApiImplicitParam(name = "id", value = "附件id", required = true, dataType = "Long", paramType = "path")
    public ExecResult<Boolean> canPreview(@PathVariable Long id) {
        Attachment attachment = attachmentService.selectById(id);
        if (ParamsUtil.isEmpty(attachment)) {
            return ExecResultUtil.success(false);
        }
        return ExecResultUtil.success(ArrayUtils.contains(sysProperties.getCanPreview().split(","), attachment.getAttachType()));
    }

    /**
     * 预览附件
     *
     * @param response response
     * @param id       id
     * @author fanzh
     * @date 2019/06/19 15:47
     */
    @ApiImplicitParam(name = "id", value = "附件id", required = true, dataType = "Long")
    @ApiOperation(value = "预览附件", notes = "根据附件ID预览附件")
    @GetMapping("/preview")
    public void preview(HttpServletResponse response, @RequestParam Long id) throws java.lang.Exception {
        Attachment attachment = attachmentService.baseGetById(id);
        FileInputStream stream = new FileInputStream(new File(attachment.getFastFileId() + File.separator + attachment.getAttachName()));
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = stream.read(b)) != -1) {
            out.write(b, 0, n);
        }
        response.setHeader("Content-Type", "image/png");
        response.getOutputStream().write(out.toByteArray());
        response.getOutputStream().flush();
        out.close();
        stream.close();
    }
}
