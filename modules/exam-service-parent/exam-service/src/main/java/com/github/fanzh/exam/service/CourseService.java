package com.github.fanzh.exam.service;

import com.github.fanzh.exam.mapper.CourseMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.exam.api.module.Course;
import com.github.fanzh.user.api.constant.AttachmentConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 课程service
 *
 * @author fanzh
 * @date 2018/11/8 21:18
 */
@Slf4j
@Service
@AllArgsConstructor
public class CourseService extends BaseService<CourseMapper, Course> {

    private final SysProperties sysProperties;


    /**
     * 初始化logo
     *
     * @param courseList courseList
     * @author fanzh
     * @date 2020/03/18 20:38
     */
    public void initLogoUrl(List<Course> courseList) {
        try {
            if (sysProperties.getLogoUrl() != null && !sysProperties.getLogoUrl().endsWith("/")) {
                sysProperties.setLogoUrl(sysProperties.getLogoUrl() + "/");
            }
            courseList.forEach(course -> {
                // 获取配置默认头像地址
                if (course.getLogoId() != null && course.getLogoId() != 0L) {
                    course.setLogoUrl(AttachmentConstant.ATTACHMENT_PREVIEW_URL + course.getLogoId());
                } else {
                    Long index = new Random().nextInt(sysProperties.getLogoCount()) + 1L;
                    course.setLogoUrl(sysProperties.getLogoUrl() + index + sysProperties.getLogoSuffix());
                    course.setLogoId(index);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
