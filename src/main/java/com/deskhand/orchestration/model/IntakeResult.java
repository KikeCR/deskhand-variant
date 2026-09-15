package com.deskhand.orchestration.model;

import com.deskhand.api.dto.HireProfileRequest;

import java.util.List;

/**
 * Output of {@link com.deskhand.orchestration.step.IntakeStep} - the Java equivalent of the
 * original Intake agent's task output: a restated/confirmed hire profile plus any missing
 * required fields, and a short natural-language confirmation for display.
 *
 * @param hire            the validated hire profile (echoed through for downstream steps)
 * @param missingFields   required fields not present/blank on the incoming request, if any
 * @param confirmationText a short LLM-generated confirmation paragraph, calling out any gaps
 */
public record IntakeResult(
        HireProfileRequest hire,
        List<String> missingFields,
        String confirmationText
) {
}
