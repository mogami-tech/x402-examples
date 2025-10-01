package tech.mogami.spring.example.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for the Mogami Facilitator.
 */
@Configuration
public class OpenAPIConfiguration {

    /**
     * Custom OpenAPI bean for the Mogami Facilitator API.
     *
     * @return OpenAPI instance with API information
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Mogami playground API"));
    }

}
