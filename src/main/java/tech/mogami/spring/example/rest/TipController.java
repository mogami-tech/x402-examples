package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402PayUSDC;

@RestController
@Tag(name = "Tip", description = "TipController provides an endpoint to send tips to the developer (Base mainnet only)")
public class TipController {

    @X402PayUSDC(
            amount = "0.01", // 0.0 1 USDC
            payTo = "0x2306e12F56e45E698bFAfa9c5E7D4e77cDEb4d06",
            network = "base"
    )
    @GetMapping("/tip")
    @Operation(summary = "Send a 0.1 USDC tip to Mogami on base mainnet")
    @SuppressWarnings({"checkstyle:MagicNumber", "SameReturnValue"})
    public final String tip() {
        return "Thanks a lot from Mogami!";
    }

}
