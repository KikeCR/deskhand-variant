package com.deskhand.orchestration.step;

import com.deskhand.api.dto.HireProfileRequest;
import com.deskhand.llm.ChatService;
import com.deskhand.orchestration.PipelineContext;
import com.deskhand.orchestration.model.IntakeResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of the original's "HR Intake Coordinator" agent: validates the hire profile
 * against the required fields and produces a short confirmation via one {@link ChatService} call.
 * No tools, same narrow responsibility as the original - it does not touch RAG or the decision
 * engine.
 */
@Component
public class IntakeStep implements OnboardingStep<HireProfileRequest, IntakeResult> {

    private static final String SYSTEM_PROMPT = """
            You are an HR Intake Coordinator. Given a new hire's profile, write a short (2-3
            sentence) confirmation paragraph restating who they are, their role, and their start
            date. If any required fields are missing, call that out plainly rather than guessing.
            Do not invent any details not present in the profile.""";

    private final ChatService chatService;

    public IntakeStep(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public IntakeResult execute(HireProfileRequest input, PipelineContext context) {
        List<String> missingFields = findMissingFields(input);

        String userPrompt = """
                Hire profile:
                - Name: %s
                - Role: %s
                - Department: %s
                - Location: %s
                - Start date: %s

                Missing fields: %s
                """.formatted(
                input.name(), input.role(), input.department(), input.location(), input.startDate(),
                missingFields.isEmpty() ? "none" : String.join(", ", missingFields)
        );

        String confirmationText = chatService.complete(SYSTEM_PROMPT, userPrompt);
        return new IntakeResult(input, missingFields, confirmationText);
    }

    private static List<String> findMissingFields(HireProfileRequest hire) {
        List<String> missing = new ArrayList<>();
        if (isBlank(hire.name())) missing.add("name");
        if (isBlank(hire.role())) missing.add("role");
        if (isBlank(hire.department())) missing.add("department");
        if (isBlank(hire.location())) missing.add("location");
        if (isBlank(hire.startDate())) missing.add("startDate");
        return missing;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
