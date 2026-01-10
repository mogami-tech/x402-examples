package tech.mogami.spring.example;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.web3j.crypto.Credentials;
import tech.mogami.commons.api.facilitator.settle.SettlementResponse;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequired;
import tech.mogami.java.client.X402V2Client;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

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
            final OkHttpClient okHttpClient = new OkHttpClient();
            final String tipUrl = StringUtils.firstNonBlank(System.getenv("TIP_URL"), "http://localhost:4021/tip");
            final String tipPrivateKey = System.getenv("TIP_PRIVATE_KEY");
            PaymentRequired paymentRequired = null;

            if (StringUtils.isNotBlank(tipPrivateKey)) {
                // final String tipAddress = Credentials.create(tipPrivateKey).getAddress();
                System.out.println("✅ Environment variables are set - Running a payment on base on the /tip endpoint");

                // We make the initial request without any payment =====================================================
                try (Response initialResponse = okHttpClient.newCall(new Request.Builder().url(tipUrl).get().build()).execute()) {
                    // Extracting the payments requirements from the header
                    paymentRequired = X402V2Client.extractPaymentRequired(getHeaders(initialResponse))
                            .orElseThrow(() -> new IllegalStateException("PaymentRequired should be present"));
                } catch (IOException e) {
                    System.err.println("IOException during HTTP request to " + tipUrl + ": " + e.getMessage());
                    System.exit(-1);
                }

                // We create a payment with a valid PaymentPayload =====================================================
                assertThat(paymentRequired).isNotNull();
                assertThat(paymentRequired.accepts()).isNotEmpty();
                final PaymentPayload payload = X402V2Client.buildPaymentPayload(
                        paymentRequired,
                        paymentRequired.accepts().getFirst(),
                        Credentials.create(tipPrivateKey)
                );

                // We call the protected resource with the payment =====================================================
                try (Response paidResponse = okHttpClient.newCall(new Request.Builder().url(tipUrl).get()
                        .headers(Headers.of(X402V2Client.buildPaymentHeaders(payload)))
                        .build()).execute()) {

                    final Optional<SettlementResponse> settlementResponse = X402V2Client.extractSettlementResponse(getHeaders(paidResponse));
                    if (settlementResponse.isPresent()) {
                        System.out.println("✅ Settlement response received: " + settlementResponse.get());
                    } else {
                        fail("No settlement response found in the paid request headers.");
                    }

                } catch (IOException e) {
                    System.err.println("IOException during HTTP request to " + tipUrl + ": " + e.getMessage());
                    System.exit(-1);
                }
            }

        };
    }

    /**
     * Extract headers from an OkHttp Response as a Map.
     *
     * @param response the OkHttp Response
     * @return a Map of header names to their first value
     */
    private Map<String, String> getHeaders(final Response response) {
        return response.headers().toMultimap()
                .entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));
    }

}
