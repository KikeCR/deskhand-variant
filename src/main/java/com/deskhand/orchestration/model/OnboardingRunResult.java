package com.deskhand.orchestration.model;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.decision.model.DecisionResult;

/**
 * The full output of {@link com.deskhand.orchestration.OnboardingPipeline#run}, carrying
 * everything the API layer needs to build the frontend-facing response: the original hire input,
 * the deterministic decision, the Research step's notes, and the final checklist. Introduced
 * specifically so the REST layer isn't limited to just the checklist text - the ported frontend's
 * {@code DecisionLog} and {@code ResearchSources} components need the decision and research notes
 * as separate structured/text fields, not just the combined markdown.
 */
public record OnboardingRunResult(
        HireProfileRequest hire,
        DecisionResult decision,
        String researchNotes,
        OnboardingChecklist checklist
) {
}
