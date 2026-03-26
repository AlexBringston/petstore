package com.om.azurefunction;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;
import io.netty.util.internal.StringUtil;

public class OrderItemsReserver {

    private static final String CONTAINER_NAME = "petstore-container";
    private static final String DLQ_QUEUE_NAME = "orderqueue/$DeadLetterQueue";
    private static final int MAX_RETRIES = 3;

    @FunctionName("orderItemsReserver")
    public String orderItemsReserver(
            @ServiceBusQueueTrigger(
                    name = "message",
                    queueName = "orderqueue",
                    connection = "ServiceBusConnection"
            ) String message,
            @BindingName("MessageId") String sessionId,
            ExecutionContext context) {

        if (!StringUtil.isNullOrEmpty(message)) {
            context.getLogger().info("OrderItemsReserver received following sessionId: "
                    + sessionId + " and cart JSON: " + message);
            if (!sendToBlobStorage(message, sessionId, context)) {
                sendToDeadLetterQueue(message, sessionId, context);
            }
        }

        return message;
    }

    private boolean sendToBlobStorage(String orderJSON, String sessionId, ExecutionContext context) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv("BlobStorageConnection"))
                .buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
        BlobClient blobClient = containerClient.getBlobClient(sessionId + ".json");

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                context.getLogger().info("Attempt " + attempt + " to upload JSON to Blob Storage...");
                blobClient.upload(BinaryData.fromString(orderJSON), true);
                context.getLogger().info("Uploaded JSON to Blob Storage with blob name: " + sessionId);
                return true;
            } catch (Exception e) {
                context.getLogger().warning("Upload attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    context.getLogger().severe("Max retries reached. Failed to upload JSON to Blob Storage.");
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return false;
    }

    private void sendToDeadLetterQueue(String orderJSON, String sessionId, ExecutionContext context) {
        try {
            String connectionString = System.getenv("ServiceBusConnection");
            ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .queueName(DLQ_QUEUE_NAME)
                    .buildClient();

            ServiceBusMessage message = new ServiceBusMessage(orderJSON);
            message.setMessageId(sessionId);
            senderClient.sendMessage(message);

            context.getLogger().info("Message sent to Dead-Letter Queue for sessionId: " + sessionId);
            senderClient.close();
        } catch (Exception e) {
            context.getLogger().severe("Failed to send message to Dead-Letter Queue: " + e.getMessage());
        }
    }
}
