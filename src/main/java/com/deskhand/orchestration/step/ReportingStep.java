package com.deskhand.orchestration.step;

import com.deskhand.llm.ChatService;
import com.deskhand.orchestration.PipelineContext;
import com.deskhand.orchestration.model.OnboardingChecklist;
import com.deskhand.orchestration.model.ResearchFinding;
import com.deskhand.orchestration.model.ResearchResult;
import org.springframework.stereotype.Component;

/**
 * Java equivalent of the original's "Onboarding Report Writer" agent: synthesizes the Intake
 * confirmation, Research findings, and the decision engine's steps (via {@link PipelineContext})
 * into a markdown checklist with a strict prompt template enforcing three fixed sections
 * (IT & Equipment, Tool & System Access, From Company Policy) - same contract as the original.
 * A post-generation check confirms all three headers are present, retrying once with a stricter
 * reminder rather than silently returning malformed output.
 * <p>
 * The decision log itself is appended separately and verbatim
 * ({@link OnboardingChecklist#decisionLogMarkdown()}) - never paraphrased by this or any other LLM
 * call, matching the original exactly.
 */
@Component
public class ReportingStep implements OnboardingStep<ResearchResult, OnboardingChecklist> {

    private static final String IT_HEADER = "## IT & Equipment";
    private static final String ACCESS_HEADER = "## Tool & System Access";
    private static final String POLICY_HEADER = "## From Company Policy";

    private static final String SYSTEM_PROMPT = """
            You are an Onboarding Report Writer. Produce a markdown Week 1 onboarding checklist
            with exactly this structure, in this order:

            # Week 1 Onboarding Checklist
            %s
            (bullet list of IT/equipment steps)
            %s
            (bullet list of tool/system access steps)
            %s
            (2-4 bullets citing specific findings from the research notes, each ending with
            "(Source: <filename>)")

            Use only the steps and findings given to you - never invent steps or policy details
            not present in the input.""".formatted(IT_HEADER, ACCESS_HEADER, POLICY_HEADER);

    private final ChatService chatService;

    public ReportingStep(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public OnboardingChecklist execute(ResearchResult input, PipelineContext context) {
        String userPrompt = buildUserPrompt(input, context);

        String markdown = chatService.complete(SYSTEM_PROMPT, userPrompt);
        if (!hasAllRequiredSections(markdown)) {
            markdown = chatService.complete(
                    SYSTEM_PROMPT,
                    userPrompt + "\n\nYour previous attempt was missing one of the three required "
                            + "headers. Include all three exactly as specified, verbatim."
            );
        }

        String decisionLog = renderDecisionLog(context);
        return new OnboardingChecklist(markdown, decisionLog);
    }

    private static String buildUserPrompt(ResearchResult input, PipelineContext context) {
        StringBuilder findingsBlock = new StringBuilder();
        for (ResearchFinding finding : input.findings()) {
            findingsBlock.append("- ").append(finding.excerpt())
                    .append(" (Source: ").append(finding.sourceDocument()).append(")\n");
        }

        return """
                Hire: %s
                Intake confirmation: %s

                IT/equipment steps to include:
                %s

                Tool/system access steps to include:
                %s

                Research notes: %s

                Findings to cite from:
                %s
                """.formatted(
                input.intake().hire().name(),
                input.intake().confirmationText(),
                String.join("\n", context.decision().locationSteps().stream().map(s -> "- " + s).toList()),
                String.join("\n", context.decision().departmentSteps().stream().map(s -> "- " + s).toList()),
                input.notes(),
                findingsBlock
        );
    }

    private static boolean hasAllRequiredSections(String markdown) {
        return markdown.contains(IT_HEADER) && markdown.contains(ACCESS_HEADER) && markdown.contains(POLICY_HEADER);
    }

    private static String renderDecisionLog(PipelineContext context) {
        StringBuilder log = new StringBuilder("## Why These Steps\n\n");
        for (String line : context.decision().explanation()) {
            log.append("- ").append(line).append('\n');
        }
        return log.toString();
    }
}
