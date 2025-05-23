package com.fuint.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger接口文档
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

     @Bean
     public Docket createRestApi() {
         return new Docket(DocumentationType.SWAGGER_2)
         .apiInfo(apiInfo())
         .enable(true)
         .select()
         .apis(RequestHandlerSelectors.basePackage("com.fuint.module"))
         .build();
     }

     @Bean
     public ApiInfo apiInfo() {
         return new ApiInfoBuilder()
         .title("E-Commerce网上购物系统系统接口文档")
         .description("E-Commerce网上购物系统接口文档，“/clientApi”目录接口为会员端相关接口，“/backendApi”目录接口为后台管理端相关接口。")
         .version("1.0")
         .build();
     }
}