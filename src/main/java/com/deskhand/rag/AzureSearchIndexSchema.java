package com.deskhand.rag;

import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;

import java.util.List;

/**
 * Defines the Azure AI Search index schema used for both ingestion and retrieval: a searchable
 * text field for BM25/keyword matching, a vector field for embedding similarity, and filterable
 * metadata fields for citation - the two halves that make hybrid search possible.
 * <p>
 * Verified against the resolved azure-search-documents 12.0.1 client on the classpath (inspected
 * via javap rather than assumed from docs, since this SDK line moved to a from-scratch document
 * model - flat {@code Map<String, Object>} "additionalProperties" instead of the old 11.x
 * generic-POJO mapping). Field/API shapes here are confirmed correct for that resolved version.
 */
public final class AzureSearchIndexSchema {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_CONTENT_VECTOR = "contentVector";
    public static final String FIELD_SOURCE_DOCUMENT = "sourceDocument";
    public static final String FIELD_HEADING = "heading";

    private static final String HNSW_ALGORITHM_NAME = "deskhand-hnsw";
    private static final String VECTOR_PROFILE_NAME = "deskhand-vector-profile";

    private AzureSearchIndexSchema() {
    }

    /**
     * Builds the {@link SearchIndex} definition. {@code vectorDimensions} must match the
     * configured Azure OpenAI embedding deployment's output size (text-embedding-3-small defaults
     * to 1536 - confirm against your actual deployment before first use, since a mismatch here
     * means recreating the index).
     */
    public static SearchIndex buildIndex(String indexName, int vectorDimensions) {
        List<SearchField> fields = List.of(
                new SearchField(FIELD_ID, SearchFieldDataType.STRING)
                        .setKey(true)
                        .setSearchable(false)
                        .setFilterable(false),
                new SearchField(FIELD_CONTENT, SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(false),
                new SearchField(FIELD_CONTENT_VECTOR, SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                        .setSearchable(true)
                        .setVectorSearchDimensions(vectorDimensions)
                        .setVectorSearchProfileName(VECTOR_PROFILE_NAME),
                new SearchField(FIELD_SOURCE_DOCUMENT, SearchFieldDataType.STRING)
                        .setFilterable(true)
                        .setSearchable(false),
                new SearchField(FIELD_HEADING, SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(false)
        );

        VectorSearch vectorSearch = new VectorSearch()
                .setAlgorithms(new HnswAlgorithmConfiguration(HNSW_ALGORITHM_NAME))
                .setProfiles(new VectorSearchProfile(VECTOR_PROFILE_NAME, HNSW_ALGORITHM_NAME));

        return new SearchIndex(indexName, fields).setVectorSearch(vectorSearch);
    }
}
