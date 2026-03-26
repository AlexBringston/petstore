package com.om.azurefunction;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

import static com.om.azurefunction.constants.Constants.CONNECTION_STRING;

public class OrderItemsReserver {

    private static final String SESSION_ID = "sessionId";
    private static final String CONTAINER_NAME = "petstore-container";

    @FunctionName("orderItemsReserver")
    public HttpResponseMessage orderItemsReserver(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        String sessionId = request.getQueryParameters().get(SESSION_ID);

        String orderJSON = null;
        if (request.getBody().isPresent()) {
            orderJSON = request.getBody().get();
            context.getLogger().info("OrderItemsReserver received following sessionId: "
                    + sessionId + " and cart JSON: " + orderJSON);
            sendToBlobStorage(orderJSON, sessionId, context);
        }


        return request.createResponseBuilder(HttpStatus.OK)
                .body("JSON cart received")
                .header("Content-Type", "application/json")
                .build();
    }

    private void sendToBlobStorage(String orderJSON, String sessionId, ExecutionContext context) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
        BlobClient blobClient = containerClient.getBlobClient(sessionId + ".json");
        blobClient.upload(BinaryData.fromString(orderJSON), true);
        context.getLogger().info("Uploaded JSON to Blob Storage with blob name: " + sessionId);
    }
}
