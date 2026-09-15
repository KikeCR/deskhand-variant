package com.deskhand.api.dto;

import com.deskhand.decision.model.DecisionResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response body for {@code POST /api/onboarding/run}. Shape matches the original Python backend's
 * {@code OnboardingResult} exactly (field names included, via {@code @JsonProperty}) so the ported
 * DeskHand React frontend under {@code frontend/} - built against that original contract - works
 * against this backend unmodified. {@code decision} is exposed as structured data (not just
 * rendered markdown) because the frontend's {@code DecisionLog} component renders it as separate
 * track cards, not as a markdown block.
 *
 * @param hire              the hire profile that was run
 * @param decision          the deterministic decision-engine output, structured
 * @param researchNotes     the Research step's synthesized notes (wire key: research_notes)
 * @param checklistMarkdown the checklist body plus the decision log appended verbatim, matching
 *                          the original's composition exactly (wire key: checklist_markdown) -
 *                          the frontend's {@code ChecklistView} renders this raw, while
 *                          {@code DecisionLog} separately renders the same decision data
 *                          structured, which is why it appears in both forms
 * @param outputPath        always {@code null} here: this deployment is stateless (Azure App
 *                          Service), unlike the original which persists this file locally - the
 *                          frontend's {@code ChecklistView} handles a missing path gracefully
 */
public record OnboardingResponse(
        HireProfileRequest hire,
        DecisionView decision,
        @JsonProperty("research_notes") String researchNotes,
        @JsonProperty("checklist_markdown") String checklistMarkdown,
        @JsonProperty("output_path") String outputPath
) {

    public record DecisionView(
            @JsonProperty("location_track") String locationTrack,
            @JsonProperty("location_steps") List<String> locationSteps,
            @JsonProperty("dept_track") String deptTrack,
            @JsonProperty("dept_steps") List<String> deptSteps,
            List<String> explanation
    ) {
        public static DecisionView from(DecisionResult decision) {
            return new DecisionView(
                    decision.locationTrack(),
                    decision.locationSteps(),
                    decision.departmentTrack(),
                    decision.departmentSteps(),
                    decision.explanation()
            );
        }
    }
}
