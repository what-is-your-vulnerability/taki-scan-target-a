package com.bss.customer.support;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.bss.customer.common.ApiException;

@Service
public class AgencyCheckService {

    private final URI agencyBase;
    private final RestClient restClient;

    public AgencyCheckService(@Value("${app.identity.agency.base-url}") String baseUrl) {
        this.agencyBase = URI.create(baseUrl);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public AgencyCheckResult check(String url) {
        URI target = parse(url);
        if (!sameEndpoint(target, agencyBase)) {
            throw ApiException.badRequest();
        }
        return probe(target);
    }

    private AgencyCheckResult probe(URI target) {
        try {
            int status = restClient.get().uri(target).retrieve().toBodilessEntity().getStatusCode().value();
            return new AgencyCheckResult(true, status);
        } catch (RestClientResponseException e) {
            return new AgencyCheckResult(true, e.getStatusCode().value());
        } catch (RuntimeException e) {
            return new AgencyCheckResult(false, null);
        }
    }

    private URI parse(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getHost() == null || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) {
                throw ApiException.badRequest();
            }
            return uri;
        } catch (URISyntaxException e) {
            throw ApiException.badRequest();
        }
    }

    private boolean sameEndpoint(URI a, URI b) {
        return a.getScheme().equalsIgnoreCase(b.getScheme())
                && a.getHost().equalsIgnoreCase(b.getHost());
    }

    public record AgencyCheckResult(boolean reachable, Integer status) {
    }
}
