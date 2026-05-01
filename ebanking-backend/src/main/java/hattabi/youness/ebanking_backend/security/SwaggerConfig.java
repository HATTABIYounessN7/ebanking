package hattabi.youness.ebanking_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Banking")
                        .version("1.0")
                        .description("REST API for E-Banking App")
                        .contact(new Contact()
                                .name("Hattabi Youness")
                                .email("hattabiyouness@gmail.com")));
    }
}
