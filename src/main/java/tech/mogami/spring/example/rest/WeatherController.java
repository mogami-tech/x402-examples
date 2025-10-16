package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PaymentRequirements;

import static tech.mogami.commons.constant.network.base.BaseContracts.BASE_SEPOLIA_USDC_CONTRACT;

@RestController
@Tag(name = "Weather", description = "WeatherController provides weather information with and without payment requirements")
public class WeatherController {

    @GetMapping("/freeWeather")
    @Operation(summary = "Provides free weather information without any payment requirements")
    @SuppressWarnings("checkstyle:MagicNumber")
    public final WeatherResponse freeWeather() {
        return new WeatherResponse(new WeatherResponse.Report("rainy", 25));
    }

    @X402PaymentRequirements(
            scheme = "exact",
            network = "base-sepolia",
            maximumAmountRequired = "3000",
            payTo = "0x7553F6FA4Fb62986b64f79aEFa1fB93ea64A22b1",
            asset = BASE_SEPOLIA_USDC_CONTRACT,
            extra = {
                    @X402PaymentRequirements.ExtraEntry(key = "name", value = "USDC"),
                    @X402PaymentRequirements.ExtraEntry(key = "version", value = "2")
            }
    )
    @GetMapping("/weather")
    @Operation(summary = "Provides weather information with payment requirements")
    @SuppressWarnings("checkstyle:MagicNumber")
    public final WeatherResponse weather() {
        return new WeatherResponse(new WeatherResponse.Report("sunny", 25));
    }

}
