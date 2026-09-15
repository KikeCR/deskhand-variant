package com.deskhand.llm;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper over Spring AI's {@code EmbeddingModel} (backed by {@code OpenAiEmbeddingModel} -
 * Spring AI 2.0 discontinued the dedicated Azure OpenAI module, see application.yml), configured
 * against the embedding deployment (text-embedding-3-small) named in
 * {@link com.deskhand.config.AppProperties}.
 * Used by both {@link com.deskhand.ingestion.AzureSearchIndexer} (embedding chunks at index time)
 * and {@link com.deskhand.rag.SearchCompanyDocsTool} (embedding the query at retrieval time).
 * <p>
 * This is the one deliberate architectural departure from the original worth calling out clearly:
 * the original embeds locally via a vendored ONNX model with zero API dependency; this port trades
 * that for a managed embedding API call - a real latency/cost/availability tradeoff, not a
 * transparent swap.
 */
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }
}
