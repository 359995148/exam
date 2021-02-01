package com.github.fanzh.exam.service;

import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.mapper.ExaminationMapper;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.zxing.QRCodeUtils;
import com.github.fanzh.exam.api.dto.ExaminationDto;
import com.github.fanzh.exam.api.module.Examination;
import com.github.fanzh.user.api.constant.AttachmentConstant;
import com.github.fanzh.user.api.module.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试service
 *
 * @author fanzh
 * @date 2018/11/8 21:19
 */
@Slf4j
@Service
public class ExaminationService extends BaseService<ExaminationMapper, Examination> {

    @Autowired
    private  SubjectService subjectService;

    @Autowired
    private  ExaminationSubjectService examinationSubjectService;

    @Autowired
    private  CourseService courseService;

    @Autowired
    private  SysProperties sysProperties;


    /**
     * 获取考试封面
     *
     * @param examinationDto examinationDto
     * @author fanzh
     * @date 2020/03/12 22:32:30
     */
    public void initExaminationLogo(ExaminationDto examinationDto) {
        try {
            if (sysProperties.getLogoUrl() != null && !sysProperties.getLogoUrl().endsWith("/")) {
                sysProperties.setLogoUrl(sysProperties.getLogoUrl() + "/");
            }
            // 获取配置默认头像地址
            if (examinationDto.getAvatarId() != null && examinationDto.getAvatarId() != 0L) {
                Attachment attachment = new Attachment();
                attachment.setId(examinationDto.getAvatarId());
                examinationDto.setLogoUrl(AttachmentConstant.ATTACHMENT_PREVIEW_URL + examinationDto.getAvatarId());
            } else {
                Long index = new Random().nextInt(sysProperties.getLogoCount()) + 1L;
                examinationDto.setLogoUrl(sysProperties.getLogoUrl() + index + sysProperties.getLogoSuffix());
                examinationDto.setAvatarId(index);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 根据考试ID生成二维码
     *
     * @param examinationId examinationId
     * @author fanzh
     * @date 2020/3/15 1:16 下午
     */
    public byte[] produceCode(Long examinationId) {
        Optional<Examination> examination = this.baseFindById(examinationId);
        // 调查问卷
        if (!examination.isPresent()) {
            return new byte[0];
        }
        String url = sysProperties.getQrCodeUrl() + "?id=" + examination.get().getId();
        BufferedImage outputStream = QRCodeUtils.encode(url);
        log.info("Share examinationId: {}, url: {}", examinationId, url);
        return QRCodeUtils.imageToBytes(outputStream);
    }

    /**
     * 根据考试ID生成二维码
     *
     * @param examinationId examinationId
     * @author fanzh
     * @date 2020/3/21 5:38 下午
     */
    public byte[] produceCodeV2(Long examinationId) {
        Optional<Examination> examination = this.baseFindById(examinationId);
        // 调查问卷
        if (!examination.isPresent()) {
            return new byte[0];
        }
        String url = sysProperties.getQrCodeUrl() + "-v2?id=" + examination.get().getId();
        BufferedImage outputStream = QRCodeUtils.encode(url);
        log.info("Share v2 examinationId: {}, url: {}", examinationId, url);
        return QRCodeUtils.imageToBytes(outputStream);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteExamination(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return;
        }
        //逻辑删除考试
        this.baseLogicDelete(id);
        List<ExaminationSubject> examinationSubjectList = examinationSubjectService.findByExaminationId(id);
        //物理删除考试题目关联关系
        examinationSubjectService.baseDelete(examinationSubjectList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
    }


}
