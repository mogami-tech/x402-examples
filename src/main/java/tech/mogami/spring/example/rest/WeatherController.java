package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PayUSDC;

@RestController
@Tag(name = "Weather", description = "WeatherController provides weather information with and without payment requirements")
public class WeatherController {

    @GetMapping("/freeWeather")
    @Operation(summary = "Provides free weather information without any payment requirements")
    @SuppressWarnings("checkstyle:MagicNumber")
    public final WeatherResponse freeWeather() {
        return new WeatherResponse(new WeatherResponse.Report("rainy", 25));
    }

    @X402PayUSDC(amount = "3") // 3 USDC
    @GetMapping("/weather")
    @Operation(summary = "Provides weather information with payment requirements")
    @SuppressWarnings("checkstyle:MagicNumber")
    public final WeatherResponse weather() {
        return new WeatherResponse(new WeatherResponse.Report("sunny", 25));
    }

}
