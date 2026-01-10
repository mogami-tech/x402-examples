package tech.mogami.spring.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.web3j.crypto.Credentials;
import tech.mogami.java.client.X402V2Client;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.constant.X402Constants.X402_PAYMENT_SIGNATURE_HEADER;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Extracts single-value headers from a MockHttpServletResponse.
     *
     * @param response the MockHttpServletResponse
     * @return a map of header names to their single values
     */
    protected static Map<String, String> getHeaders(MockHttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(name -> Map.entry(name, Objects.requireNonNull(response.getHeader(name))))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Test
    @DisplayName("Get payment requirements and pay for it")
    void integration() throws Exception {
        var result = mockMvc.perform(get("/weather"))
                .andExpect(status().isPaymentRequired())
                .andReturn();

        // Extract the PaymentRequired header from the response ========================================================
        var PaymentRequired = X402V2Client.extractPaymentRequired(getHeaders(result.getResponse()))
                .orElseThrow(() -> new IllegalStateException("PaymentRequired should be present"));

        // Building a valid payment payload ============================================================================
        var paymentPayload = X402V2Client.buildPaymentPayload(
                PaymentRequired,
                PaymentRequired.accepts().getFirst(),
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY)
        );
        final Map<String, String> paymentHeaders = X402V2Client.buildPaymentHeaders(paymentPayload);
        HttpHeaders headers = new HttpHeaders();
        headers.add(X402_PAYMENT_SIGNATURE_HEADER, paymentHeaders.get(X402_PAYMENT_SIGNATURE_HEADER));
        if (paymentPayload.getNonce().isPresent()) {
            System.out.println("=> Payment nonce: " + paymentPayload.getNonce());
        }

        // Calling the API with the payment header.
        result = mockMvc.perform(get("/weather").headers(headers)).andReturn();

        // Checking the response =======================================================================================
        assertThat(X402V2Client.extractSettlementResponse(getHeaders(result.getResponse())))
                .isPresent().get()
                .satisfies(settlementResponse -> {
                    assertThat(settlementResponse.success()).isTrue();
                    assertThat(settlementResponse.errorReason()).isBlank();
                    assertThat(settlementResponse.payer()).isEqualToIgnoringCase(TEST_CLIENT_WALLET_ADDRESS_1);
                    assertThat(settlementResponse.network()).isEqualTo(BASE_SEPOLIA.networkId());
                });
        // Check that the payment was successful.
        assertEquals(200, result.getResponse().getStatus(),
                "result HTTP status 200 after payment, got " + result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("sunny"),
                "Expected weather response to contain 'sunny', got: " + result.getResponse().getContentAsString());
    }

}
