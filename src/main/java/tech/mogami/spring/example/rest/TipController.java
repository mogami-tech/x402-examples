package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PaymentRequirements;

@RestController
@Tag(name = "Tip", description = "TipController provides an endpoint to send tips to the developer (Base mainnet only)")
public class TipController {

    @X402PaymentRequirements(
            scheme = "exact",
            network = "base",
            maximumAmountRequired = "1",
            payTo = "0xFE0920A0a7f0f8a1Ec689146c30C3BBef439bF8A",
            asset = "0x036CbD53842c5426634e7929541eC2318f3dCF7e",
            extra = {
                    @X402PaymentRequirements.ExtraEntry(key = "name", value = "USDC"),
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
