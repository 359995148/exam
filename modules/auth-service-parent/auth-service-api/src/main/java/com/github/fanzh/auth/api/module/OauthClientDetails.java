package com.github.fanzh.auth.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OAuth2客户端信息
 *
 * @author fanzh
 * @date 2019/3/30 16:35
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oauth_client_details")
public class OauthClientDetails extends BaseEntity {

    /**
     * clientId
     */
    private String clientId;

    /**
     * resource_ids
     */
    private String resourceIds;

    /**
     * 密钥明文
     */
    private String clientSecretPlainText;

    /**
     * 密钥密文
     */
    private String clientSecret;

    /**
     * 授权范围
     */
    private String scope;

    /**
     * 授权类型
     */
    private String authorizedGrantTypes;

    /**
     * web_server_redirect_uri
     */
    private String webServerRedirectUri;

    /**
     * authorities
     */
    private String authorities;

    /**
     * access_token有效时间
     */
    private String accessTokenValidity;

    /**
     * refresh_token有效时间
     */
    private String refreshTokenValidity;

    /**
     * additional_information
     */
    private String additionalInformation;

    /**
     * autoapprove
     */
    private String autoapprove;

}
