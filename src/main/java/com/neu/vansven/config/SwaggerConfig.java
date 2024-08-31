package com.neu.vansven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: SwaggerConfiguration
 * Created by kamier on 2022/12/28 22:06
 */

@Configuration
@EnableSwagger2
@Profile("test")
public class SwaggerConfig {

    /**
     * 文档定义
     * @return
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)  // 文档类型
                .apiInfo(apiInfo()) // api信息，包括标题、描述、联系人等
                .select()
                // done 这里一定要标注控制器的位置，说明需要生成控制器代码的相关接口文档
                .apis(RequestHandlerSelectors.basePackage("com.neu.vansven.controller"))
                .paths(PathSelectors.any()) // 匹配所有路径
                .build()
                .globalOperationParameters(setHeaderToken());   // 添加全局参数

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("vansven伙伴匹配系统") // 设置接口文档的标题为 "vansven伙伴匹配系统"，即 API 的名称或主题
                .description("伙伴匹配系统中心接口文档入口") //设置接口文档的描述
                .termsOfServiceUrl("http://github.com/vansven") // 设置服务条款的 URL，该 URL vansven伙伴匹配系统 GitHub 页面
                .version("1.0") // 设置接口文档的版本号，指明当前接口的版本
                .build(); // 构建并返回一个 ApiInfo 对象，该对象包含了上述设置的接口文档信息
    }

    /**
     * 设置swagger文档中全局参数
     */
    private List<Parameter> setHeaderToken() {
        List<Parameter> params = new ArrayList<>(); // 创建一个参数列表
        ParameterBuilder parameterBuilder = new ParameterBuilder(); // 创建一个参数构建器
        // 使用参数构建器设置参数的各个属性
        Parameter parameter = parameterBuilder.name("token")// 参数名为 "token"
                .description("用户token")// 参数描述为 "用户token"
                .modelRef(new ModelRef("string")) // 参数类型为字符串
                .parameterType("header")// 参数位置为请求头
                .required(true)// 参数为必需的
                .build();// 构建参数对象
        params.add(parameter);   // 将参数添加到参数列表中
        return params;   // 返回参数列表
    }
}