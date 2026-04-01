package com.chtrembl.petstore.order.config;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCosmosRepositories(basePackages = "com.chtrembl.petstore.order.repository")
public class CosmosConfiguration extends AbstractCosmosConfiguration {

    @Value("${azure.cosmos.db.endpoint}")
    private String endpoint;

    @Value("${azure.cosmos.db.key}")
    private String key;

    @Value("${azure.cosmos.db.database}")
    private String database;

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key);
    }

    @Override
    protected String getDatabaseName() {
        return database;
    }
}
