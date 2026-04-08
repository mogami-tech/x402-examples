package tech.mogami.spring.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tech.mogami.commons.constant.BodyType;
import tech.mogami.commons.constant.HttpMethod;
import tech.mogami.java.client.X402V2Client;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("X402 Bazaar extension tests")
public class X402BazaarTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should extract bazaar extension from payment required response")
    void shouldExtractBazaarExtensionFromPaymentRequiredResponse() throws Exception {
        var result = mockMvc.perform(post("/bazaar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isPaymentRequired())
                .andReturn();

        var paymentRequired = X402V2Client.extractPaymentRequired(getHeaders(result.getResponse()))
                .orElseThrow(() -> new IllegalStateException("PaymentRequired should be present"));

        X402V2Client.extractBazaarExtension(paymentRequired).ifPresentOrElse(
                bazaarExtension -> {
                    assertThat(bazaarExtension.info()).isNotNull();

                    assertThat(bazaarExtension.info().input())
                            .isNotNull()
                            .satisfies(input -> {
                                assertThat(input.type()).isEqualTo("http");
                                assertThat(input.method()).isEqualTo(HttpMethod.POST);
                                assertThat(input.bodyType()).isEqualTo(BodyType.JSON);
                                assertThat(input.body()).isNotNull();
                                assertThat(input.body().path("recipient_first_name").asText()).isEqualTo("Jane");
                                assertThat(input.body().path("recipient_last_name").asText()).isEqualTo("Doe");
                                assertThat(input.body().path("recipient_email").asText()).isEqualTo("jane@example.com");
                                assertThat(input.body().path("from_name").asText()).isEqualTo("Your Friendly AI");
                                assertThat(input.body().path("message").asText()).isEqualTo("Here is your digital memo.");
                            });

                    assertThat(bazaarExtension.info().output())
                            .isNotNull()
                            .satisfies(output -> {
                                assertThat(output.type()).isEqualTo("json");
                                assertThat(output.example()).isNotNull();
                                assertThat(output.example().path("success").asBoolean()).isTrue();
                                assertThat(output.example().path("id").asText()).isEqualTo("memo-uuid");
                            });

                    assertThat(bazaarExtension.schema()).isNotNull();
                    assertThat(bazaarExtension.schema().path("$schema").asText())
                            .isEqualTo("https://json-schema.org/draft/2020-12/schema");
                },
                () -> fail("No BazaarExtension found in the PaymentRequired response")
        );
    }

    private static Map<String, String> getHeaders(MockHttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(name -> Map.entry(name, Objects.requireNonNull(response.getHeader(name))))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
