package com.tarasantoniuk.finance.common;


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

        Contact contact = new Contact();
        contact.setName("API Support Team");
        contact.setEmail("support@example.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Accounting Management API")
                .version("1.0.0")
                .description("REST API for managing countries, organizations, currencies, " +
                        "accounting policies, and external exchange rates")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}