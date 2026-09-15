package com.deskhand.llm;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin wrapper over Spring AI's {@code ChatModel} (backed by {@code OpenAiChatModel} - Spring AI
 * 2.0 discontinued the dedicated Azure OpenAI module, so Azure access goes through the generic
 * OpenAI client pointed at the Azure endpoint, see application.yml), configured against the chat
 * deployment named in {@link com.deskhand.config.AppProperties}. Every orchestration step
 * (Intake, Research, Reporting) calls this rather than Spring AI's ChatModel directly, so the
 * prompt-building convention and error handling live in exactly one place - the equivalent of the
 * original's {@code crewai.LLM(model="gpt-4o-mini")} construction, minus the multi-provider
 * fallback chain (explicitly out of scope; the model/deployment name is a config value instead,
 * preserving "swap providers via config" without the fallback-retry logic itself).
 */
@Service
public class ChatService {

    private final ChatModel chatModel;

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String complete(String systemPrompt, String userPrompt) {
        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
