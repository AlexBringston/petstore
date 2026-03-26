package com.chtrembl.petstore.order.service;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
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


    @Value("${petstore.service.order.item.reserver.url:http://localhost:7071}")
    private String azureFunctionUrl;
    @Value("${petstore.service.order.item.reserver.namespace}")
    private String fullNamespace;
    @Value("${petstore.service.order.item.reserver.queue}")
    private String queue;

    public String reserveOrderItems(String orderJSON, String sessionId) {
        log.info("Sending cart JSON to Azure service bus queue {}",
                queue);

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(fullNamespace)
                .credential(credential)
                .sender()
                .queueName(queue)
                .buildClient()) {

            ServiceBusMessage message = new ServiceBusMessage(orderJSON);
            message.setMessageId(sessionId);
            senderClient.sendMessage(message);
            return orderJSON;

        }  catch (Exception e) {
            log.error("Error updating cart JSON for sessionId: {}. Message: {}", sessionId, e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }
}
