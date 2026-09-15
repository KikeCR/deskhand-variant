package com.deskhand.decision;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.decision.model.DecisionResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deterministic, LLM-free onboarding-track decision step - ported as-is from the original's
 * {@code decide_onboarding_track()}. This is plain Java, not a model call: the two branches
 * (location, department) are resolved from fixed lookup tables in {@link LocationRules} and
 * {@link DepartmentRules}, exactly matching the original's explicit design rationale ("leaving
 * this to an LLM's discretion risks silently inconsistent results between runs").
 * <p>
 * Runs once, before {@link com.deskhand.orchestration.OnboardingPipeline} starts, and its result
 * is threaded through every step as data - never re-derived or paraphrased by an LLM.
 * <p>
 * Zero Azure/Spring AI dependencies by design: this class and its rule tables are plain-JUnit
 * testable with no mocking, mirroring the original's fully offline decision-engine tests.
 */
@Component
public class DecisionEngine {

    public DecisionResult decide(HireProfileRequest hire) {
        String locationTrack = LocationRules.normalizeTrack(hire.location());
        List<String> locationSteps = LocationRules.stepsFor(locationTrack);

        String departmentTrack = DepartmentRules.normalizeTrack(hire.department());
        List<String> departmentSteps = DepartmentRules.stepsFor(departmentTrack);

        List<String> explanation = List.of(
                "Location='%s' matched track '%s' -> added %d IT/equipment step(s)."
                        .formatted(hire.location(), locationTrack, locationSteps.size()),
                "Department='%s' matched track '%s' -> added %d provisioning step(s)."
                        .formatted(hire.department(), departmentTrack, departmentSteps.size())
        );

        return new DecisionResult(locationTrack, locationSteps, departmentTrack, departmentSteps, explanation);
    }
}
