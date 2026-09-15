package com.deskhand.rag.model;

/**
 * A single retrieved chunk from Azure AI Search, analogous to one entry in the original's
 * {@code [{"content": doc, "source": meta["source"], "distance": dist}, ...]} retriever output.
 *
 * @param content        the chunk text (one H2 markdown section, see MarkdownH2Chunker)
 * @param sourceDocument the originating file name, e.g. "onboarding_policy.md" - used for citation
 * @param heading        the H2 heading text for this chunk, useful for citation display
 * @param score          the search relevance score (hybrid: combined keyword+vector via RRF)
 */
public record DocChunk(
        String content,
        String sourceDocument,
        String heading,
        double score
) {
}
