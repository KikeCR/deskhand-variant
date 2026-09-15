package com.deskhand.orchestration;

import com.deskhand.decision.model.DecisionResult;

/**
 * Cross-cutting values available to every step without growing each step's method signature.
 * Carries the {@link DecisionResult}, computed once by {@link com.deskhand.decision.DecisionEngine}
 * before the pipeline starts - directly mirroring the original, where the decision engine runs
 * before the Crew and its output is injected into task prompts rather than derived by any agent.
 *
 * @param decision the pre-computed, deterministic onboarding-track decision
 */
public record PipelineContext(DecisionResult decision) {
}
