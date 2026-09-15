package com.deskhand.api;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.api.dto.OnboardingResponse;
import com.deskhand.orchestration.OnboardingPipeline;
import com.deskhand.orchestration.model.OnboardingRunResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Java equivalent of the original's {@code POST /api/onboarding/run} FastAPI route: accepts a
 * hire profile and returns the full generated checklist, synchronously - a single request/response
 * cycle covering the full ~15-40s pipeline run (decision engine, then Intake, Research, Reporting),
 * matching the original's behavior exactly. See the README for why this stays synchronous
 * (Azure App Service, not Functions - the request model comfortably covers this run time).
 */
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingPipeline pipeline;

    public OnboardingController(OnboardingPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @PostMapping("/run")
    public OnboardingResponse run(@Valid @RequestBody HireProfileRequest request) {
        OnboardingRunResult result = pipeline.run(request);
        return new OnboardingResponse(
                result.hire(),
                OnboardingResponse.DecisionView.from(result.decision()),
                result.researchNotes(),
                result.checklist().markdownBody() + "\n\n" + result.checklist().decisionLogMarkdown(),
                null
        );
    }
}
