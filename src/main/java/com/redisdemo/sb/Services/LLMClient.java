package com.redisdemo.sb.Services;

public interface LLMClient {

    /**
     * Sends a prompt to the LLM and returns the generated response.
     *
     * @param prompt user question or input
     * @return LLM response text
     */
    String complete(String prompt);

}