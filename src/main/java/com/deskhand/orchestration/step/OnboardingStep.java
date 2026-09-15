package com.deskhand.orchestration.step;

import com.deskhand.orchestration.PipelineContext;

/**
 * Shared contract for every stage of the pipeline (Intake, Research, Reporting), mirroring the
 * original's CrewAI agent/task hand-off - but where CrewAI passes context via a loosely-typed
 * runtime {@code Task(context=[...])} list, here the hand-off is the generic signature itself:
 * {@code ResearchStep implements OnboardingStep<IntakeResult, ResearchResult>} can only compile
 * against Intake's actual output shape. The type system enforces the contract that CrewAI enforces
 * at runtime - a genuine, explainable difference worth calling out in an interview.
 *
 * @param <I> the input type this step consumes (typically the previous step's output)
 * @param <O> the output type this step produces
 */
public interface OnboardingStep<I, O> {

    O execute(I input, PipelineContext context);
}
