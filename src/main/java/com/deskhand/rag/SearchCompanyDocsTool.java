package com.deskhand.rag;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorizedQuery;
import com.deskhand.config.AppProperties;
import com.deskhand.llm.EmbeddingService;
import com.deskhand.rag.model.DocChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of the original's CrewAI {@code BaseTool} ("search_company_docs"): a plain,
 * directly-callable search method rather than a tool exposed for LLM function-calling. In this
 * port, {@link com.deskhand.orchestration.step.ResearchStep} calls this method itself (in a loop,
 * at least {@code deskhand.onboarding.min-research-searches} times) to decide what and how much to
 * search - preserving agent-driven retrieval without paying for an extra tool-selection LLM call.
 * <p>
 * Runs a hybrid query (BM25 keyword + vector, combined server-side via RRF when both a search text
 * and a vector query are present on the same request) against the Azure AI Search index,
 * requesting the configured top-k results - the Azure equivalent of the original's
 * {@code collection.query(query_texts=[...], n_results=4)}.
 */
@Component
public class SearchCompanyDocsTool {

    private final SearchClient searchClient;
    private final EmbeddingService embeddingService;
    private final AppProperties properties;

    public SearchCompanyDocsTool(SearchClient searchClient, EmbeddingService embeddingService, AppProperties properties) {
        this.searchClient = searchClient;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    public List<DocChunk> search(String query) {
        int topK = properties.onboarding().retrievalTopK();

        float[] embedding = embeddingService.embed(query);
        List<Float> vector = new ArrayList<>(embedding.length);
        for (float value : embedding) {
            vector.add(value);
        }

        VectorizedQuery vectorQuery = new VectorizedQuery(vector)
                .setKNearestNeighbors(topK)
                .setFields(AzureSearchIndexSchema.FIELD_CONTENT_VECTOR);

        SearchOptions options = new SearchOptions()
                .setSearchText(query)
                .setVectorQueries(vectorQuery)
                .setTop(topK);

        List<DocChunk> results = new ArrayList<>();
        for (SearchResult result : searchClient.search(options)) {
            Map<String, Object> fields = result.getAdditionalProperties();
            results.add(new DocChunk(
                    (String) fields.get(AzureSearchIndexSchema.FIELD_CONTENT),
                    (String) fields.get(AzureSearchIndexSchema.FIELD_SOURCE_DOCUMENT),
                    (String) fields.get(AzureSearchIndexSchema.FIELD_HEADING),
                    result.getScore()
            ));
        }
        return results;
    }
}
