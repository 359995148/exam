package com.github.fanzh.msc.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.github.fanzh.msc.api.dto.SmsDto;
import com.github.fanzh.msc.api.model.SmsResponse;
import com.github.fanzh.msc.properties.SmsProperties;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.core.utils.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author fanzh
 * @date 2019/6/22 13:23
 */
@Slf4j
@AllArgsConstructor
@Service
public class SmsService {

    private final SmsProperties smsProperties;

    /**
     * 发送短信
     *
     * @param smsDto smsDto
     * @return SmsResponse
     * @author fanzh
     * @date 2019/06/22 13:28
     */
    public SmsResponse sendSms(SmsDto smsDto) {
        DefaultProfile profile = DefaultProfile.getProfile(smsProperties.getRegionId(), smsProperties.getAppKey(), smsProperties.getAppSecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(smsProperties.getDomain());
        request.putQueryParameter("RegionId", smsProperties.getRegionId());
        request.putQueryParameter("PhoneNumbers", smsDto.getReceiver());
        request.putQueryParameter("SignName", smsProperties.getSignName());
        request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
        request.putQueryParameter("TemplateParam", smsDto.getContent());
        request.setVersion(smsProperties.getVersion());
        request.setAction(smsProperties.getAction());
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("response: {}", response.getData());
            if (response.getHttpStatus() != 200)
                throw new CommonException(response.getData());
            SmsResponse smsResponse = JsonMapper.getInstance().fromJson(response.getData(), SmsResponse.class);
            if (smsResponse == null)
                throw new CommonException("Parse response error");
            if (!"OK".equals(smsResponse.getCode()))
                throw new CommonException(smsResponse.getMessage());
            return smsResponse;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CommonException("Send message failed: " + e.getMessage());
        }
    }
}


