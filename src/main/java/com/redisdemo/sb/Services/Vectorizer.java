package com.redisdemo.sb.Services;

import com.redis.vl.utils.vectorize.LangChain4JVectorizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

public class Vectorizer {
    private EmbeddingModel embeddingModel;
    private final String modelName;

    public Vectorizer(String key, String modelName) {
        this.modelName = modelName;
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(key)
                .modelName(modelName)
                .build();
        this.embeddingModel = embeddingModel;
    }

    public LangChain4JVectorizer vectorizer() {
        return new LangChain4JVectorizer(this.modelName,embeddingModel,1536);
    }

    float[] embed(String text){
        LangChain4JVectorizer vectorizer = this.vectorizer();
        float[] embedding = vectorizer.embed(text);
        return embedding;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
