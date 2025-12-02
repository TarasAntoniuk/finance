package com.tarasantoniuk.finance.common.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer customizePageableParameter() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> {
                    if ("sort".equals(parameter.getName())) {
                        // Замінюємо некоректний приклад на правильний
                        parameter.setExample("documentDate,desc");
                        parameter.setDescription("Sort order. Format: property,(asc|desc). Example: documentDate,desc");
                        parameter.setSchema(new StringSchema()
                                .example("documentDate,desc"));
                    }
                });
            }
            return operation;
        };
    }
}