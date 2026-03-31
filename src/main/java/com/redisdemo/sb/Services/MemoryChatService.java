package com.redisdemo.sb.Services;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import dev.langchain4j.service.AiServices;

@Component
@Scope("session") // memory per user session
public class MemoryChatService {

    private final ChatLanguageModel model;
    private final ChatMemory memory;
    private final ChatAssistant assistant;

    public MemoryChatService(@Value("${openai.api-key}") String apiKey) {
        this.model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-3.5-turbo")
                .temperature(0.7)
                .build();
        this.memory = MessageWindowChatMemory.withMaxMessages(10);

        this.assistant = AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(memory)
                .build();

    }

    public String chat(String userInput) {
        return assistant.chat(userInput);
    }

}
