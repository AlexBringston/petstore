package com.chtrembl.petstore.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemsReserverService {

    private final RestTemplate restTemplate;

    @Value("${petstore.service.order.item.reserver.url:http://localhost:7071}")
    private String azureFunctionUrl;

    public String reserveOrderItems(String orderJSON, String sessionId) {
        log.info("Sending cart JSON to {}/api/orderItemsReserver",
                azureFunctionUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> entity = new HttpEntity<>(orderJSON, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    String.format("%s/api/orderItemsReserver?sessionId=%s", azureFunctionUrl, sessionId),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Received response from orderItemsReserver: {}", response.getBody());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error updating cart JSON for sessionId: {}. Message: {}", sessionId, e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }
}
