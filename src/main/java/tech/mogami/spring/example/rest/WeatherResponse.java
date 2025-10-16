package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "WeatherResponse represents the weather information response")
public record WeatherResponse(Report report) {

    @Schema(description = "Report contains detailed weather information")
    public record Report(
            @Schema(description = "Weather condition", example = "sunny")
            String weather,
            @Schema(description = "Temperature in Celsius", example = "25")
            int temperature
    ) {
    }

}

