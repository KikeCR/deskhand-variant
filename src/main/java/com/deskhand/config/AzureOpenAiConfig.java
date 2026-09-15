package com.deskhand.config;

import org.springframework.context.annotation.Configuration;

/**
 * Intentionally empty. Spring AI 2.0 discontinued the dedicated Azure OpenAI module, but its
 * generic OpenAI starter has explicit "Microsoft Foundry" (Azure OpenAI's current branding)
 * support built in - {@code spring.ai.openai.microsoft-foundry=true} plus
 * {@code spring.ai.openai.{chat,embedding}.microsoft-deployment-name} (see application.yml) is
 * enough for its autoconfiguration to wire correct {@code ChatModel}/{@code EmbeddingModel} beans
 * with no custom code needed here. Confirmed by inspecting
 * spring-ai-autoconfigure-model-openai's own configuration metadata directly, not assumed from
 * docs, and confirmed end-to-end at runtime: a request made with a placeholder key reached Azure's
 * endpoint and got back Azure OpenAI's own 401 error message (not a connection failure or a
 * generic OpenAI error), proving the request was correctly shaped for Azure. Only a real key and
 * deployment are needed for actual completions (see {@link com.deskhand.llm.ChatService} /
 * {@link com.deskhand.llm.EmbeddingService}).
 * <p>
 * Kept as a placeholder in case a future need (e.g. Azure AD / managed-identity auth instead of a
 * static API key, via {@code spring.ai.openai.*.credential}) requires an explicit bean here.
 */
@Configuration
public class AzureOpenAiConfig {
}
