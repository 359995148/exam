package com.github.fanzh.common.config;

import com.github.fanzh.common.security.constant.SecurityConstant;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger配置
 *
 * @author fanzh
 * @date 2019/3/26 16:26
 */
@EnableSwagger2
@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public Docket createRestApi() {
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(authorizationParameter());
        parameterList.add(tenantCodeParameter());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameterList);
    }

    /**
     * Authorization 请求头
     *
     * @return Parameter
     */
    private Parameter authorizationParameter() {
        ParameterBuilder tokenBuilder = new ParameterBuilder();
        tokenBuilder.name("Authorization")
                .description("access_token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build();
        return tokenBuilder.build();
    }

    /**
     * Tenant-Code 请求头
     *
     * @return Parameter
     */
    private Parameter tenantCodeParameter() {
        ParameterBuilder tokenBuilder = new ParameterBuilder();
        tokenBuilder.name("Tenant-Code")
                .defaultValue(SecurityConstant.DEFAULT_TENANT_CODE)
                .description("租户标识")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build();
        return tokenBuilder.build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("微考")
                .description("微服务考试")
                .termsOfServiceUrl("微服务条款网址")
                .contact(new Contact("fanzh", "", "359995148@qq.com"))
                .version("3.7.0")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决静态资源无法访问
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        //过滤swagger
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-resources/");
        registry.addResourceHandler("/swagger/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger*");
        registry.addResourceHandler("/v2/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/v2/api-docs/");
    }
}
