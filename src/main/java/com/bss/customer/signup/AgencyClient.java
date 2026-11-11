package com.bss.customer.signup;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.bss.customer.common.ApiException;

@Component
public class AgencyClient {

    private record CreatedRequest(String redirectUrl) {
    }

    private final RestClient restClient;

    public AgencyClient(RestClient.Builder builder,
                        @Value("${app.identity.agency.base-url}") String baseUrl,
                        @Value("${app.identity.agency.client-key}") String clientKey) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
                .defaultHeader("X-Client-Key", clientKey)
                .build();
    }

    public String createRequest(String verificationId) {
        try {
            return restClient.post()
                    .uri("/verifications")
                    .body(Map.of("verification_id", verificationId))
                    .retrieve()
                    .body(CreatedRequest.class)
                    .redirectUrl();
        } catch (RestClientException e) {
            throw unavailable();
        }
    }

    /** 기관이 발급한 적 없는 참조값이면 null을 돌려준다. */
    public AgencyResult fetchResult(String agencyResultRef) {
        try {
            return restClient.get()
                    .uri("/results/{ref}", agencyResultRef)
                    .retrieve()
                    .body(AgencyResult.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            throw unavailable();
        }
    }

    private ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AGENCY_UNAVAILABLE", "본인 확인 기관에 연결하지 못했습니다.");
    }
}
