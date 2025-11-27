package com.progressoft.fxdeals.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI fxDealsOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setEmail("theshamkhi1@gmail.com");
        contact.setName("ClusteredData Warehouse Team");
        contact.setUrl("https://github.com/theshamkhi");

        Info info = new Info()
                .title("ClusteredData Warehouse API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API for managing Foreign Exchange (FX) deals. " +
                        "Supports importing deals from CSV files and performing CRUD operations via REST endpoints.")
                .termsOfService("https://github.com/theshamkhi/ProgressSoftAssignment");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}