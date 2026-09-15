package com.deskhand.decision.model;

import java.util.List;

/**
 * Structured output of {@link com.deskhand.decision.DecisionEngine}. Mirrors the original Python
 * DecisionResult dataclass field-for-field: two independent branch outcomes (location, department),
 * their associated onboarding steps, and a human-readable explanation trail.
 * <p>
 * This result is computed once, before the orchestration pipeline runs, and is injected as data
 * into later steps' prompts rather than being derived or paraphrased by any LLM - matching the
 * original's "plain deterministic Python, not an LLM call" design intentionally preserved here.
 *
 * @param locationTrack  normalized location bucket, e.g. "remote" or "in_office"
 * @param locationSteps  the hardcoded IT/equipment steps for that location track
 * @param departmentTrack normalized department bucket, e.g. "engineering", "sales", or "general"
 * @param departmentSteps the hardcoded provisioning steps for that department track
 * @param explanation    human-readable lines describing why each branch was taken, rendered
 *                       verbatim in the final checklist's "Why These Steps" section
 */
public record DecisionResult(
        String locationTrack,
        List<String> locationSteps,
        String departmentTrack,
        List<String> departmentSteps,
        List<String> explanation
) {
}
