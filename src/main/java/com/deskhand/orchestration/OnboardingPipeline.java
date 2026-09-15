package com.deskhand.orchestration;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.decision.DecisionEngine;
import com.deskhand.decision.model.DecisionResult;
import com.deskhand.orchestration.model.IntakeResult;
import com.deskhand.orchestration.model.OnboardingChecklist;
import com.deskhand.orchestration.model.OnboardingRunResult;
import com.deskhand.orchestration.model.ResearchResult;
import com.deskhand.orchestration.step.IntakeStep;
import com.deskhand.orchestration.step.ReportingStep;
import com.deskhand.orchestration.step.ResearchStep;
import org.springframework.stereotype.Component;

/**
 * The Java equivalent of the original's {@code Crew(process=Process.sequential)}: runs the
 * decision engine once up front, then calls Intake, Research, and Reporting in order, threading
 * each step's typed output into the next. This is the one class that visibly encodes the
 * "Intake -> Research -> Reporting, sequential, output-feeds-input" pattern - the core "same
 * pattern, different stack" story for this port, expressed as plain orchestrating Java rather than
 * a framework's DSL (see {@link com.deskhand.orchestration.step.OnboardingStep} for why no
 * multi-agent framework was used here).
 * <p>
 * Implemented in the orchestration-layer phase, once Intake/Research/Reporting steps exist.
 */
@Component
public class OnboardingPipeline {

    private final DecisionEngine decisionEngine;
    private final IntakeStep intakeStep;
    private final ResearchStep researchStep;
    private final ReportingStep reportingStep;

    public OnboardingPipeline(
            DecisionEngine decisionEngine,
            IntakeStep intakeStep,
            ResearchStep researchStep,
            ReportingStep reportingStep
    ) {
        this.decisionEngine = decisionEngine;
        this.intakeStep = intakeStep;
        this.researchStep = researchStep;
        this.reportingStep = reportingStep;
    }

    public OnboardingRunResult run(HireProfileRequest hire) {
        DecisionResult decision = decisionEngine.decide(hire);
        PipelineContext context = new PipelineContext(decision);

        IntakeResult intakeResult = intakeStep.execute(hire, context);
        ResearchResult researchResult = researchStep.execute(intakeResult, context);
        OnboardingChecklist checklist = reportingStep.execute(researchResult, context);

        return new OnboardingRunResult(hire, decision, researchResult.notes(), checklist);
    }
}
