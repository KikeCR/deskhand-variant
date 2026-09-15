package com.deskhand.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Root configuration properties, bound from the {@code deskhand.*} keys in application.yml
 * (themselves sourced from environment variables / a local .env file - see .env.example).
 * Centralizing these here, rather than scattering @Value annotations, keeps every externalized
 * setting (endpoints, deployment names, index name) visible in one place for review.
 *
 * @param azureOpenAi   Azure OpenAI endpoint + deployment names (chat and embedding are separate
 *                      deployments in Azure OpenAI, unlike OpenAI's flat model-name-as-id approach)
 * @param azureSearch   Azure AI Search endpoint, key, and target index name
 * @param onboarding    tunables for the RAG/orchestration pipeline itself
 */
@ConfigurationProperties(prefix = "deskhand")
public record AppProperties(
        AzureOpenAi azureOpenAi,
        AzureSearch azureSearch,
        Onboarding onboarding
) {
    public record AzureOpenAi(
            String endpoint,
            String apiKey,
            String chatDeploymentName,
            String embeddingDeploymentName
    ) {
    }

    public record AzureSearch(
            String endpoint,
            String apiKey,
            String indexName
    ) {
    }

    public record Onboarding(
            int retrievalTopK,
            int minResearchSearches,
            int embeddingDimensions
    ) {
    }
}
