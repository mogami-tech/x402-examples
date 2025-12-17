package tech.mogami.spring.example;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import tech.mogami.java.client.helper.X402PaymentHelper;

import static io.netty.handler.codec.http.HttpResponseStatus.PAYMENT_REQUIRED;
import static tech.mogami.commons.constant.X402Constants.X402_X_PAYMENT_HEADER;

/**
 * Spring Boot server example.
 */
@SpringBootApplication
@SuppressWarnings({"checkstyle:FinalClass", "checkstyle:HideUtilityClassConstructor"})
public class Application {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * This method is only run once when the application starts.
     * It is used to test real payment on base with the tip method.
     *
     * @return a CommandLineRunner to execute on startup
     */
    @Bean
    CommandLineRunner init() {
        return args -> {
            final String tipUrl = StringUtils.firstNonBlank(System.getenv("TIP_URL"), "http://localhost:4021/tip");
            final String tipPrivateKey = System.getenv("TIP_PRIVATE_KEY");

            if (StringUtils.isNotBlank(tipPrivateKey)) {
                final String tipAddress = Credentials.create(tipPrivateKey).getAddress();
                System.out.println("✅ Environment variables are set - Running a payment on base on the /tip endpoint");
                RestTemplate restTemplate = new RestTemplate();

                // Call the /tip endpoint which requires a payment
                try {
                    restTemplate.getForEntity(tipUrl, String.class);
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode().value() == PAYMENT_REQUIRED.code()) {
                        // Retrieve the payment required from the response body.
                        var paymentRequired = X402PaymentHelper.getPaymentRequiredFromBody(e.getResponseBodyAsString())
                                .orElseThrow(() -> new IllegalStateException("Payment requirements not found in response"));

                        // Generate a payment payload (without a signature) from the payment requirements.
                        var paymentPayload = X402PaymentHelper.getPayloadFromPaymentRequirements(
                                null,
                                tipAddress,
                                paymentRequired.accepts().getFirst());

                        // Sign the payment payload.
                        var signedPayload = X402PaymentHelper.getSignedPayload(
                                Credentials.create(tipPrivateKey),
                                paymentRequired.accepts().getFirst(),
                                paymentPayload);

                        // Perform the request with the signed payment payload in the header.
                        HttpHeaders headers = new HttpHeaders();
                        headers.set(X402_X_PAYMENT_HEADER, X402PaymentHelper.getPayloadHeader(signedPayload));
                        HttpEntity<Void> entity = new HttpEntity<>(headers);

                        try {
                            ResponseEntity<String> response = restTemplate.exchange(
                                    tipUrl,
                                    HttpMethod.GET,
                                    entity,
                                    String.class
                            );

                            if (!response.getStatusCode().is2xxSuccessful()) {
                                System.out.println("⚠️ Unexpected status code after payment: " + response.getStatusCode());
                            } else {
                                System.out.println("✅ Status: " + response.getStatusCode());
                                System.out.println("✅ Body: " + response.getBody());
                                signedPayload.getNonce().ifPresent(nonce -> System.out.println("✅ Payment nonce: " + nonce));
                            }
                        } catch (HttpClientErrorException httpClientErrorException) {
                            System.out.println("⚠️ HTTP error: " + httpClientErrorException.getStatusCode());
                            System.out.println("⚠️ Response body:");
                            System.out.println(httpClientErrorException.getResponseBodyAsString());
                        }
                    }
                }


            }

        };
    }

}
