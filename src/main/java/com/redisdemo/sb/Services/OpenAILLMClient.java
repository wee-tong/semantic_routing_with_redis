package com.redisdemo.sb.Services;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAILLMClient implements LLMClient {

    final String apiKey = System.getenv("OPENAIAPI_KEY");

    public OpenAILLMClient() {
        // Initialize OpenAI client with your API key
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("OPENAI_API_KEY environment variable not set.");
            return;
        }
    }

    public String complete(String prompt) {
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(this.apiKey)
                    .modelName("gpt-4o-mini") // Specify the model you want to use
                    .build();
            // Use the model to generate a response
            String response = model.generate(prompt);
            System.out.println(response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during completion: " + e.getMessage();
        }
    }

}


