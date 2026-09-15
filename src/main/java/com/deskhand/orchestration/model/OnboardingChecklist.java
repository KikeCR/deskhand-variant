package com.deskhand.orchestration.model;

/**
 * Final output of the pipeline, produced by {@link com.deskhand.orchestration.step.ReportingStep}:
 * a markdown Week-1 onboarding checklist with the same fixed section contract as the original
 * (IT & Equipment, Tool & System Access, From Company Policy), plus the deterministic decision
 * log rendered verbatim beneath it - never paraphrased by an LLM, exactly as in the original.
 *
 * @param markdownBody   the LLM-generated checklist body (the three fixed sections)
 * @param decisionLogMarkdown the "Why These Steps" explanation, rendered directly from
 *                            {@code DecisionResult.explanation()} with no LLM involvement
 */
public record OnboardingChecklist(String markdownBody, String decisionLogMarkdown) {
}
