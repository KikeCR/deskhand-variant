package com.deskhand.ingestion;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.deskhand.config.AppProperties;
import com.deskhand.ingestion.MarkdownH2Chunker.ChunkedSection;
import com.deskhand.llm.EmbeddingService;
import com.deskhand.rag.AzureSearchIndexSchema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embeds each chunked section (via {@link EmbeddingService}, backed by the Azure OpenAI embedding
 * deployment) and upserts it into the Azure AI Search index, creating the index first if it
 * doesn't exist. The Java/Azure equivalent of the original's {@code build_vectorstore()} -
 * similarly idempotent: re-running ingestion re-uploads the same documents under the same
 * deterministic ids (MERGE_OR_UPLOAD) rather than duplicating them.
 * <p>
 * Document keys must match Azure AI Search's key charset (letters, digits, {@code _}, {@code -},
 * {@code =} only - no dots or colons), so ids are derived from the source filename and heading
 * with disallowed characters replaced, unlike the original's raw {@code f"{source}::{i}"} format.
 */
@Component
public class AzureSearchIndexer {

    private final SearchIndexClient searchIndexClient;
    private final SearchClient searchClient;
    private final EmbeddingService embeddingService;
    private final AppProperties properties;

    public AzureSearchIndexer(
            SearchIndexClient searchIndexClient,
            SearchClient searchClient,
            EmbeddingService embeddingService,
            AppProperties properties
    ) {
        this.searchIndexClient = searchIndexClient;
        this.searchClient = searchClient;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    public void indexAll(List<ChunkedSection> sections) {
        searchIndexClient.createOrUpdateIndex(
                AzureSearchIndexSchema.buildIndex(
                        properties.azureSearch().indexName(),
                        properties.onboarding().embeddingDimensions()
                )
        );

        List<IndexAction> actions = new ArrayList<>();
        for (ChunkedSection section : sections) {
            actions.add(toIndexAction(section));
        }
        searchClient.indexDocuments(new IndexDocumentsBatch(actions));
    }

    private IndexAction toIndexAction(ChunkedSection section) {
        float[] embedding = embeddingService.embed(section.content());
        List<Float> vector = new ArrayList<>(embedding.length);
        for (float value : embedding) {
            vector.add(value);
        }

        Map<String, Object> fields = new HashMap<>();
        fields.put(AzureSearchIndexSchema.FIELD_ID, toDocumentId(section));
        fields.put(AzureSearchIndexSchema.FIELD_CONTENT, section.content());
        fields.put(AzureSearchIndexSchema.FIELD_CONTENT_VECTOR, vector);
        fields.put(AzureSearchIndexSchema.FIELD_SOURCE_DOCUMENT, section.sourceDocument());
        fields.put(AzureSearchIndexSchema.FIELD_HEADING, section.heading());

        return new IndexAction()
                .setActionType(IndexActionType.MERGE_OR_UPLOAD)
                .setAdditionalProperties(fields);
    }

    private static String toDocumentId(ChunkedSection section) {
        String safeSource = section.sourceDocument().replaceAll("[^a-zA-Z0-9_-]", "_");
        String safeHeading = section.heading().replaceAll("[^a-zA-Z0-9_-]", "_");
        return safeSource + "__" + safeHeading;
    }
}
