package com.deskhand.orchestration.step;

import com.deskhand.config.AppProperties;
import com.deskhand.llm.ChatService;
import com.deskhand.orchestration.PipelineContext;
import com.deskhand.orchestration.model.IntakeResult;
import com.deskhand.orchestration.model.ResearchFinding;
import com.deskhand.orchestration.model.ResearchResult;
import com.deskhand.rag.SearchCompanyDocsTool;
import com.deskhand.rag.model.DocChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of the original's "Onboarding Research Specialist" agent: derives query terms
 * from the hire profile and the pre-computed {@link com.deskhand.decision.model.DecisionResult}
 * (available via {@link PipelineContext}), calls {@link SearchCompanyDocsTool} at least
 * {@code deskhand.onboarding.min-research-searches} times, then synthesizes the findings with
 * citations via one {@link ChatService} call - matching the original's "run >= 3 targeted
 * searches, cite excerpt+source, never invent policy" contract.
 * <p>
 * The original leaves "how many searches, on what terms" to the agent's own reasoning; here that
 * decision moves into explicit Java control flow (a loop over derived query terms) - arguably more
 * deterministic and testable, worth noting as a deliberate simplification rather than a limitation.
 */
@Component
public class ResearchStep implements OnboardingStep<IntakeResult, ResearchResult> {

    private static final String SYSTEM_PROMPT = """
            You are an Onboarding Research Specialist. You are given excerpts retrieved from the
            company's policy documents. Write a short synthesized summary (3-5 sentences) of what
            these excerpts mean for this specific new hire. Only state things directly supported
            by the excerpts - never invent policy details that aren't present in them.""";

    private final SearchCompanyDocsTool searchTool;
    private final ChatService chatService;
    private final AppProperties properties;

    public ResearchStep(SearchCompanyDocsTool searchTool, ChatService chatService, AppProperties properties) {
        this.searchTool = searchTool;
        this.chatService = chatService;
        this.properties = properties;
    }

    @Override
    public ResearchResult execute(IntakeResult input, PipelineContext context) {
        List<String> queries = deriveQueries(input, context);

        Map<String, DocChunk> uniqueChunks = new LinkedHashMap<>();
        for (String query : queries) {
            for (DocChunk chunk : searchTool.search(query)) {
                uniqueChunks.putIfAbsent(chunk.sourceDocument() + "::" + chunk.heading(), chunk);
            }
        }

        List<ResearchFinding> findings = new ArrayList<>();
        StringBuilder excerptsForPrompt = new StringBuilder();
        for (DocChunk chunk : uniqueChunks.values()) {
            findings.add(new ResearchFinding(chunk.content(), chunk.sourceDocument()));
            excerptsForPrompt.append("[Source: ").append(chunk.sourceDocument()).append("]\n")
                    .append(chunk.content()).append("\n\n");
        }

        String userPrompt = """
                New hire: %s, %s in %s, location track: %s, department track: %s.

                Retrieved excerpts:
                %s
                """.formatted(
                input.hire().name(), input.hire().role(), input.hire().department(),
                context.decision().locationTrack(), context.decision().departmentTrack(),
                excerptsForPrompt
        );

        String notes = chatService.complete(SYSTEM_PROMPT, userPrompt);
        return new ResearchResult(input, findings, notes);
    }

    private List<String> deriveQueries(IntakeResult input, PipelineContext context) {
        List<String> queries = new ArrayList<>(List.of(
                "onboarding steps for " + context.decision().locationTrack() + " employees",
                "IT setup and equipment for " + input.hire().location(),
                context.decision().departmentTrack() + " department onboarding and tool access"
        ));
        int minSearches = properties.onboarding().minResearchSearches();
        while (queries.size() < minSearches) {
            queries.add("benefits and PTO for new hires");
        }
        return queries;
    }
}
