package com.redisdemo.sb.Services;

import dev.langchain4j.service.SystemMessage;

public interface ChatAssistant {

    @SystemMessage("You are a helpful assistant.")
    String chat(String userMessage);
}
