package com.tarasantoniuk.finance.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local development server");

        Server productionServer = new Server();
        productionServer.setUrl("https://api.tarasantoniuk.com");
        productionServer.setDescription("Production server");

        Contact contact = new Contact();
        contact.setName("API Support Team");
        contact.setEmail("bronya2004@gmail.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Finance Accounting System API")
                .version("0.0.3")
                .description("Comprehensive accounting system for managing financial records, " +
                        "banking operations, and organizational accounts. " +
                        "Currently supports accounting and banking domains with planned warehouse inventory management with cell-based storage.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(productionServer, localServer));
    }
}