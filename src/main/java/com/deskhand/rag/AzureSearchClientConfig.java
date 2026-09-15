package com.deskhand.rag;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.deskhand.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the Azure AI Search {@link SearchClient} (query/document operations against a single,
 * pre-existing index - used by {@link SearchCompanyDocsTool}) and {@link SearchIndexClient}
 * (schema/index management - used by the ingestion pipeline to create the index) from
 * {@link AppProperties}, using key-based authentication for a portfolio project's simplicity.
 * <p>
 * Production use would prefer a managed identity / {@code DefaultAzureCredential} over a static
 * admin key - noted here rather than built, since local dev has no equivalent managed-identity
 * story anyway.
 */
@Configuration
public class AzureSearchClientConfig {

    @Bean
    public SearchIndexClient searchIndexClient(AppProperties properties) {
        AppProperties.AzureSearch config = properties.azureSearch();
        return new SearchIndexClientBuilder()
                .endpoint(config.endpoint())
                .credential(new AzureKeyCredential(config.apiKey()))
                .buildClient();
    }

    @Bean
    public SearchClient searchClient(AppProperties properties) {
        AppProperties.AzureSearch config = properties.azureSearch();
        return new SearchClientBuilder()
                .endpoint(config.endpoint())
                .credential(new AzureKeyCredential(config.apiKey()))
                .indexName(config.indexName())
                .buildClient();
    }
}
