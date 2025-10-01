package tech.mogami.spring.example.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PaymentRequirements;

@RestController
public class WeatherController {

    @GetMapping("/freeWeather")
    public WeatherResponse freeWeather() {
        return new WeatherResponse(new WeatherResponse.Report("rainy", 25));
    }

    @X402PaymentRequirements(
            scheme = "exact",
            network = "base-sepolia",
            maximumAmountRequired = "3000",
            payTo = "0x7553F6FA4Fb62986b64f79aEFa1fB93ea64A22b1",
            asset = "0x036CbD53842c5426634e7929541eC2318f3dCF7e",
            extra = {
                    @X402PaymentRequirements.ExtraEntry(key = "name", value = "USDC"),
                    @X402PaymentRequirements.ExtraEntry(key = "version", value = "2")
            }
    )
    @GetMapping("/weather")
    public WeatherResponse weather() {
        return new WeatherResponse(new WeatherResponse.Report("sunny", 25));
    }

}
