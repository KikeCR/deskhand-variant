package com.deskhand.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/onboarding/run}, and the shape of each entry returned by
 * {@code GET /api/sample-hires}. Mirrors the original's required hire fields (name, role,
 * department, location, start_date) - the same five fields IntakeStep checks for completeness
 * before the pipeline proceeds.
 * <p>
 * {@code start_date} (not {@code startDate}) is used on the wire deliberately: the ported
 * DeskHand React frontend under {@code frontend/} was built against the original Python backend's
 * snake_case JSON contract, and this explicit {@code @JsonProperty} lets that frontend work
 * against this Java backend completely unmodified rather than silently reformatting the wire
 * format project-wide (a global Jackson naming strategy would also affect endpoints where the
 * original's naming doesn't apply).
 */
public record HireProfileRequest(
        @NotBlank String name,
        @NotBlank String role,
        @NotBlank String department,
        @NotBlank String location,
        @NotBlank @JsonProperty("start_date") String startDate
) {
}
