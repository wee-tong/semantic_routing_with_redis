package com.redisdemo.sb.Services;

import com.redis.vl.extensions.cache.CacheHit;
import com.redis.vl.extensions.cache.SemanticCache;
import com.redis.vl.schema.TagField;
import com.redis.vl.utils.vectorize.LangChain4JVectorizer;
import redis.clients.jedis.UnifiedJedis;
import com.redis.vl.query.Filter;

import java.util.*;

public class SupportBotService {
    private final SemanticCache cache;
    private final SemanticCache privateCache;
    private LLMClient llmClient;

    public SupportBotService(UnifiedJedis jedis, LangChain4JVectorizer vectorizer) {
        this.cache =  new SemanticCache.Builder()
                .redisClient(jedis)       // set client
                .vectorizer(vectorizer)   // set vectorizer
                .name("llm-cache")        // index/cache name
                .distanceThreshold(0.75f)  // distance threshold (lower = stricter)
                .ttl(7200)              // optional TTL (seconds)
                .build();

        this.privateCache =  new SemanticCache.Builder()
                .redisClient(jedis)       // set client
                .vectorizer(vectorizer)   // set vectorizer
                .name("private-cache")        // index/cache name
                .distanceThreshold(0.4f)  // distance threshold (lower = stricter)
                .ttl(7200)
                .build();

        List<Map<String, Object>> userphonemap= new ArrayList<>();
        userphonemap.add(Map.of("user", "abc"));
        userphonemap.add(Map.of("user", "xyz"));
        userphonemap.add(Map.of("user", "boss"));

        this.privateCache.store("What is the phone number linked in my account?",
                "The number on file is 123-456-11111",
                userphonemap.get(0)
        );
        this.privateCache.store("What is the phone number linked in my account?",
                "The number on file is 321-999-10101",
                userphonemap.get(1)
        );
        this.privateCache.store("Where is the gold stored?",
                "The gold is stored in the Suntec City, Singapore",
                userphonemap.get(2)
        );
//        this.llmClient = new MockLLMClient(); // your LLM client
        this.llmClient = new OpenAILLMClient(); // your LLM client
    }

    public Map<String, Object> answerQuestionwithStatistics(String userQuestion) {
        // Check cache first
        Optional<CacheHit> hit = cache.check(userQuestion);
        Map<String, Object> responseWrapper = new HashMap<>();

        if (hit.isPresent()) {
            System.out.println("✓ Cache hit! Saved API call and time.");
            responseWrapper.put("cacheresponse", Map.of("cacheresponse", hit.get().getResponse(),
                    "hit","true","hitrate",cache.getHitRate()));
            return responseWrapper;
        }

        // Cache miss - call LLM
        System.out.println("✗ Cache miss - calling LLM...");
        String response = llmClient.complete(userQuestion);
        // Store for future use
        cache.store(userQuestion, response);
        responseWrapper.put("cacheresponse", Map.of("cacheresponse", response,
                "hit","false","hitrate",cache.getHitRate()));
        return responseWrapper;

    }
    public String answerQuestion(String userQuestion) {
        // Check cache first
        Optional<CacheHit> hit = cache.check(userQuestion);

        if (hit.isPresent()) {
            System.out.println("✓ Cache hit! Saved API call and time.");
            return hit.get().getResponse();
        }

        // Cache miss - call LLM
        System.out.println("✗ Cache miss - calling LLM...");
        String response = llmClient.complete(userQuestion);

        // Store for future use
        cache.store(userQuestion, response);

        return response;
    }

    public Map<String, Object> answerQuestionwithFilter(String userQuestion, Filter filter) {
        // Check cache first
        Optional<CacheHit> hit = privateCache.check(userQuestion, filter);
        Map<String, Object> responseWrapper = new HashMap<>();
        if (hit.isPresent()) {
            System.out.println("✓ Cache hit! Saved API call and time.");
            responseWrapper.put("cacheresponse", Map.of("cacheresponse", hit.get().getResponse(),
                    "hit","true","hitrate",privateCache.getHitRate()));
            return responseWrapper;
        }

        // Cache miss - call LLM
        System.out.println("✗ Cache miss - calling LLM...");
        String response = llmClient.complete(userQuestion);

        // Store for future use
        cache.store(userQuestion, response);
        responseWrapper.put("cacheresponse", Map.of("cacheresponse", response,
                "hit","false","hitrate",cache.getHitRate()));
        return responseWrapper;
    }
    public String answerQuestion(String userQuestion, Filter filter) {
        // Check cache first
        Optional<CacheHit> hit = privateCache.check(userQuestion, filter);

        if (hit.isPresent()) {
            System.out.println("✓ Cache hit! Saved API call and time.");
            return hit.get().getResponse();
        }

        // Cache miss - call LLM
        System.out.println("✗ Cache miss - calling LLM...");
        String response = llmClient.complete(userQuestion);

        // Store for future use
        cache.store(userQuestion, response);

        return response;
    }
    public void clearOldEntries() {
        // Clear cache entries older than 7 days
        this.cache.clear();
    }
}

