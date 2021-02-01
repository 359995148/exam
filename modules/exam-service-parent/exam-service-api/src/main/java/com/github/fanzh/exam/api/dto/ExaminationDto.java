package com.github.fanzh.exam.api.dto;

import com.github.fanzh.exam.api.module.Course;
import com.github.fanzh.exam.api.module.Examination;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fanzh
 * @date 2018/11/20 22:02
 */
@Data
@NoArgsConstructor
public class ExaminationDto extends Examination {

    private Course course;

    /**
     * 封面地址
     */
    private String logoUrl;

}
