package com.deskhand.api;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.decision.model.DecisionResult;
import com.deskhand.orchestration.OnboardingPipeline;
import com.deskhand.orchestration.model.OnboardingChecklist;
import com.deskhand.orchestration.model.OnboardingRunResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the REST layer end to end against a faked {@link OnboardingPipeline} - no Azure/Spring
 * AI beans are involved at all, since {@code @WebMvcTest} only loads the web layer, sidestepping
 * the need to fake every Azure client individually. {@code @MockitoBean} (spring-test) replaces
 * the now-removed {@code @MockBean} (spring-boot-test), which Spring Boot 4 dropped.
 * <p>
 * Asserts on the original's snake_case wire field names (start_date, location_track, dept_track,
 * research_notes, checklist_markdown) since the ported DeskHand React frontend depends on that
 * exact contract - see {@link com.deskhand.api.dto.OnboardingResponse}.
 */
@WebMvcTest(OnboardingController.class)
class OnboardingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingPipeline pipeline;

    @Test
    void postOnboardingRunReturnsChecklistWithAllRequiredSections() throws Exception {
        HireProfileRequest hire = new HireProfileRequest(
                "Priya Nakamura", "Software Engineer II", "Engineering", "remote", "2026-09-15");
        DecisionResult decision = new DecisionResult(
                "remote", List.of("Ship a laptop"), "engineering", List.of("GitHub invite"),
                List.of("Location='remote' matched track 'remote'"));
        OnboardingChecklist checklist = new OnboardingChecklist(
                "# Week 1 Onboarding Checklist\n## IT & Equipment\n- step\n## Tool & System Access\n- step\n## From Company Policy\n- step",
                "## Why These Steps\n- Location='remote' matched track 'remote'"
        );
        when(pipeline.run(any())).thenReturn(new OnboardingRunResult(hire, decision, "Research notes here.", checklist));

        String requestBody = """
                {
                  "name": "Priya Nakamura",
                  "role": "Software Engineer II",
                  "department": "Engineering",
                  "location": "remote",
                  "start_date": "2026-09-15"
                }""";

        mockMvc.perform(post("/api/onboarding/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hire.name").value("Priya Nakamura"))
                .andExpect(jsonPath("$.hire.start_date").value("2026-09-15"))
                .andExpect(jsonPath("$.decision.location_track").value("remote"))
                .andExpect(jsonPath("$.decision.dept_track").value("engineering"))
                .andExpect(jsonPath("$.research_notes").value("Research notes here."))
                .andExpect(jsonPath("$.checklist_markdown").value(containsString("## IT & Equipment")))
                .andExpect(jsonPath("$.checklist_markdown").value(containsString("## Tool & System Access")))
                .andExpect(jsonPath("$.checklist_markdown").value(containsString("## From Company Policy")))
                .andExpect(jsonPath("$.checklist_markdown").value(containsString("Why These Steps")))
                .andExpect(jsonPath("$.output_path").doesNotExist());
    }

    @Test
    void postOnboardingRunRejectsMissingRequiredFields() throws Exception {
        String requestBody = """
                {
                  "name": "",
                  "role": "Software Engineer II",
                  "department": "Engineering",
                  "location": "remote",
                  "start_date": "2026-09-15"
                }""";

        mockMvc.perform(post("/api/onboarding/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
