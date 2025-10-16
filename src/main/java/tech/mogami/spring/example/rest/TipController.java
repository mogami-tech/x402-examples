package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PaymentRequirements;

import static tech.mogami.commons.constant.network.base.BaseContracts.BASE_MAINNET_USDC_CONTRACT;

@RestController
@Tag(name = "Tip", description = "TipController provides an endpoint to send tips to the developer (Base mainnet only)")
public class TipController {

    @X402PaymentRequirements(
            scheme = "exact",
            network = "base",
            maximumAmountRequired = "10000",  // 0.1 USDC (6 decimals)/
            payTo = "0x2306e12F56e45E698bFAfa9c5E7D4e77cDEb4d06",
            asset = BASE_MAINNET_USDC_CONTRACT,
            extra = {
                    @X402PaymentRequirements.ExtraEntry(key = "name", value = "USD Coin"),
                    @X402PaymentRequirements.ExtraEntry(key = "version", value = "2")
            }
    )
    @GetMapping("/tip")
    @Operation(summary = "Send a tip to Mogami (Base mainnet only)")
    @SuppressWarnings("checkstyle:MagicNumber")
    public final String tip() {
        return "Thanks a lot from Mogami!";
    }

}
