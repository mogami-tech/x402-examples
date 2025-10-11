package tech.mogami.spring.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.web3j.crypto.Credentials;
import tech.mogami.java.client.helper.X402PaymentHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.constant.X402Constants.X402_X_PAYMENT_HEADER;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests")
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get payment requirements and pay for it")
    void integration() throws Exception {
        MvcResult result = mockMvc.perform(get("/weather"))
                .andExpect(status().isPaymentRequired())
                .andReturn();

        // Retrieve the payment required from the response body.
        var paymentRequired = X402PaymentHelper.getPaymentRequiredFromBody(result.getResponse().getContentAsString())
                .orElseThrow(() -> new IllegalStateException("Payment requirements not found in response"));

        // Generate a payment payload (without a signature) from the payment requirements.
        var paymentPayload = X402PaymentHelper.getPayloadFromPaymentRequirements(
                null,
                TEST_CLIENT_WALLET_ADDRESS_1,
                paymentRequired.accepts().getFirst());

        // Sign the payment payload.
        var signedPayload = X402PaymentHelper.getSignedPayload(
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY),
                paymentRequired.accepts().getFirst(),
                paymentPayload);

        // Perform the request with the signed payment payload in the header.
        mockMvc.perform(get("/weather")
                .header(X402_X_PAYMENT_HEADER, X402PaymentHelper.getPayloadHeader(signedPayload)));

        // Display nonce
        var nonce = signedPayload.getNonce();
        if (nonce.isEmpty()) {
            System.err.println("No nonce payment");
        } else {
            System.out.println("Payment nonce: " + nonce.get());
            System.out.println("View your payment at: https://console.mogami.tech/payments/by-nonce/" + nonce.get());
        }
    }

}
