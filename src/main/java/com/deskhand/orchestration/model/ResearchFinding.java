package com.deskhand.orchestration.model;

/**
 * One cited finding produced by {@link com.deskhand.orchestration.step.ResearchStep}, pairing a
 * synthesized point with the source excerpt/document it's grounded in - mirroring the original
 * Research agent's requirement to cite an excerpt and source for every finding, never inventing
 * policy.
 *
 * @param excerpt        the grounding excerpt, taken from a retrieved {@code DocChunk}
 * @param sourceDocument the originating file name, e.g. "benefits_faq.md"
 */
public record ResearchFinding(String excerpt, String sourceDocument) {
}
