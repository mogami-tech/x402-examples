package tech.mogami.spring.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.spring.annotation.X402Bazaar;
import tech.mogami.spring.annotation.X402PayUSDC;

/**
 * BazaarExample demonstrates how to use the x402 bazaar extension
 * to publish structured, machine-readable endpoint metadata inside a 402 Payment Required response.
 */
@RestController
@Tag(name = "Bazaar", description = "BazaarExample demonstrates the x402 bazaar extension for structured endpoint discovery")
@SuppressWarnings("checkstyle:LineLength")
public class BazaarExample {

    @X402PayUSDC(amount = "1.00")
    @X402Bazaar(
            inputMethod = "POST",
            inputBodyType = "json",
            inputBody = "{\"recipient_first_name\":\"Jane\",\"recipient_last_name\":\"Doe\","
                    + "\"recipient_email\":\"jane@example.com\",\"from_name\":\"Your Friendly AI\","
                    + "\"message\":\"Here is your digital memo.\"}",
            outputType = "json",
            outputExample = "{\"success\":true,\"id\":\"memo-uuid\","
                    + "\"memo\":{\"id\":\"agent-memo-v1\",\"name\":\"Agent Digital Memo\"},"
                    + "\"delivery\":{\"status\":\"created\"},"
                    + "\"payment\":{\"amount_usdc\":\"1.00\",\"network\":\"eip155:84532\",\"payer\":\"0x...\"}}",
            schema = "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\","
                    + "\"properties\":{\"input\":{\"type\":\"object\","
                    + "\"properties\":{\"type\":{\"type\":\"string\",\"const\":\"http\"},"
                    + "\"method\":{\"type\":\"string\",\"enum\":[\"POST\"]},"
                    + "\"bodyType\":{\"type\":\"string\",\"enum\":[\"json\",\"form-data\",\"text\"]},"
                    + "\"body\":{\"type\":\"object\","
                    + "\"required\":[\"recipient_first_name\",\"recipient_last_name\",\"from_name\",\"message\"],"
                    + "\"properties\":{\"recipient_first_name\":{\"type\":\"string\",\"description\":\"First name of the recipient (1-80 chars)\"},"
                    + "\"recipient_last_name\":{\"type\":\"string\",\"description\":\"Last name of the recipient (1-80 chars)\"},"
                    + "\"recipient_email\":{\"type\":\"string\",\"description\":\"Email address for the recipient\"},"
                    + "\"from_name\":{\"type\":\"string\",\"description\":\"Sender name (1-80 chars)\"},"
                    + "\"message\":{\"type\":\"string\",\"description\":\"Message to deliver (1-280 chars)\"}}}},"
                    + "\"required\":[\"type\",\"bodyType\",\"body\",\"method\"],\"additionalProperties\":false},"
                    + "\"output\":{\"type\":\"object\","
                    + "\"properties\":{\"type\":{\"type\":\"string\"},\"example\":{\"type\":\"object\"}},"
                    + "\"required\":[\"type\"]}},"
                    + "\"required\":[\"input\"]}"
    )
    @PostMapping("/bazaar")
    @Operation(summary = "Send a digital memo — a bazaar-enabled x402 endpoint example")
    public final String sendMemo(@RequestBody final String ignoredBody) {
        return "{\"success\":true,\"id\":\"memo-uuid\","
                + "\"memo\":{\"id\":\"agent-memo-v1\",\"name\":\"Agent Digital Memo\"},"
                + "\"delivery\":{\"status\":\"created\"}}";
    }

}
