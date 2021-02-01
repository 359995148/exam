package com.github.fanzh.exam.api.feign.factory;

import com.github.fanzh.exam.api.feign.fallback.ExaminationServiceClientFallbackImpl;
import com.github.fanzh.exam.api.feign.ExaminationServiceClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author fanzh
 * @date 2019/3/26 09:49
 */
@Component
public class ExaminationServiceClientFallbackFactory implements FallbackFactory<ExaminationServiceClient> {

    @Override
    public ExaminationServiceClient create(Throwable throwable) {
        ExaminationServiceClientFallbackImpl examinationServiceClientFallback = new ExaminationServiceClientFallbackImpl();
        examinationServiceClientFallback.setThrowable(throwable);
        return examinationServiceClientFallback;
    }
}
