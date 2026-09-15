package com.deskhand.orchestration;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.config.AppProperties;
import com.deskhand.decision.DecisionEngine;
import com.deskhand.llm.ChatService;
import com.deskhand.orchestration.model.OnboardingRunResult;
import com.deskhand.orchestration.step.IntakeStep;
import com.deskhand.orchestration.step.ReportingStep;
import com.deskhand.orchestration.step.ResearchStep;
import com.deskhand.rag.SearchCompanyDocsTool;
import com.deskhand.rag.model.DocChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link OnboardingPipeline} against fakes for {@link ChatService} and
 * {@link SearchCompanyDocsTool} - the only two collaborators that would otherwise reach Azure - no
 * live Azure calls in CI, mirroring the original's fully offline test suite. Everything else
 * (DecisionEngine, IntakeStep, ResearchStep, ReportingStep) is the real production wiring, so this
 * exercises the actual sequential hand-off (decision -> intake -> research -> reporting) end to
 * end and confirms the decision log is rendered verbatim rather than passed through an LLM.
 */
@ExtendWith(MockitoExtension.class)
class OnboardingPipelineTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SearchCompanyDocsTool searchCompanyDocsTool;

    @Test
    void runsAllStepsInSequenceAndThreadsOutputs() {
        when(chatService.complete(anyString(), anyString()))
                .thenReturn("Confirmed hire profile.")
                .thenReturn("Research notes synthesized from retrieved excerpts.")
                .thenReturn("""
                        # Week 1 Onboarding Checklist
                        ## IT & Equipment
                        - Install the company VPN client and connect to the corporate network
                        ## Tool & System Access
                        - Accept your GitHub organization invite
                        ## From Company Policy
                        - Remote employees get a $500 stipend (Source: it_setup_checklist.md)
                        """);
        when(searchCompanyDocsTool.search(anyString())).thenReturn(List.of(
                new DocChunk("Remote employees get a $500 stipend.", "it_setup_checklist.md", "For Remote Employees", 0.9)
        ));

        AppProperties properties = new AppProperties(
                null, null, new AppProperties.Onboarding(4, 3, 1536));

        DecisionEngine decisionEngine = new DecisionEngine();
        IntakeStep intakeStep = new IntakeStep(chatService);
        ResearchStep researchStep = new ResearchStep(searchCompanyDocsTool, chatService, properties);
        ReportingStep reportingStep = new ReportingStep(chatService);
        OnboardingPipeline pipeline = new OnboardingPipeline(decisionEngine, intakeStep, researchStep, reportingStep);

        HireProfileRequest hire = new HireProfileRequest(
                "Priya Nakamura", "Software Engineer II", "Engineering", "remote", "2026-09-15");

        OnboardingRunResult result = pipeline.run(hire);

        assertThat(result.hire()).isEqualTo(hire);
        assertThat(result.decision().locationTrack()).isEqualTo("remote");
        assertThat(result.researchNotes()).isEqualTo("Research notes synthesized from retrieved excerpts.");
        assertThat(result.checklist().markdownBody()).contains("## IT & Equipment", "## Tool & System Access", "## From Company Policy");
        assertThat(result.checklist().decisionLogMarkdown()).contains("## Why These Steps");
        assertThat(result.checklist().decisionLogMarkdown()).contains("Location='remote'");
        assertThat(result.checklist().decisionLogMarkdown()).contains("Department='Engineering'");

        // decision log is rendered verbatim from DecisionResult, never passed through the LLM
        verify(chatService, times(3)).complete(anyString(), anyString());
        // Research runs at least the configured minimum number of searches
        verify(searchCompanyDocsTool, atLeast(3)).search(anyString());
    }
}
