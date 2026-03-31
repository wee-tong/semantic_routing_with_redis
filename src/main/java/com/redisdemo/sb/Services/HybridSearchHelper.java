package com.redisdemo.sb.Services;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import com.redis.vl.index.SearchIndex;
import com.redis.vl.query.Filter;
import com.redis.vl.query.VectorQuery;
import com.redis.vl.schema.IndexSchema;
import com.redis.vl.utils.rerank.HFCrossEncoderReranker;
import com.redis.vl.utils.rerank.RerankResult;
import com.redis.vl.utils.vectorize.SentenceTransformersVectorizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HybridSearchHelper {
    String hybridSearchString;
    static final String redisHost = "localhost";
    static final int redisPort = 6479;

    public HybridSearchHelper(String hybridSearchString) {
        this.hybridSearchString = hybridSearchString;
    }

    public static String search(String searchQuery) {
        // Connect to Redis
        UnifiedJedis jedis = new UnifiedJedis(
                new HostAndPort(redisHost, redisPort)
        );
        Map<String, Object> schema = Map.of(
                "index", Map.of(
                        "name", "news-index",
                        "prefix", "news",
                        "storage_type", "json"
                ),
                "fields", List.of(
                        Map.of("name", "classIndex", "type", "numeric"),
                        Map.of("name", "title", "type", "text"),
                        Map.of("name", "description", "type", "text"),
                        Map.of("name", "source", "type", "text"),
                        Map.of(
                                "name", "embeddings",
                                "type", "vector",
                                "attrs", Map.of(
                                        "dims", 384,
                                        "distance_metric", "cosine",
                                        "algorithm", "hnsw",
                                        "datatype", "float16"
                                )
                        )
                )
        );
        // Load schema from Dictionary
        // IndexSchema schema2 = IndexSchema.fromYaml(schemaYaml);
        IndexSchema schema2 = IndexSchema.fromDict(schema);
        // Create the search index
        SearchIndex index = new SearchIndex(schema2, jedis);
        index.create(true);  // true = overwrite if exists

        ///  user input
        String queryQuestion = "Strong earnings from financial services";
        String queryQuestion2 = "Football match of World cup";
        String queryQuestion3 = "any news about blade server";
        queryQuestion = searchQuery;
        float[] queryVector = generateEmbedding(queryQuestion);
        // Set the pre-filter
        Filter filter1_source = Filter.and(
                Filter.text("$.source", "Reuters")
        );
        Filter filter2_classindex = Filter.numeric("$.classIndex").between(1, 3);

        // Build the hybrid query
        VectorQuery query1 = VectorQuery.builder()
                .vector(queryVector)
                .field("embeddings")
                .numResults(5)
                .returnScore(true)
                .withPreFilter(filter1_source.build())
                .withPreFilter(filter2_classindex.build())
                .returnFields("$.title", "$.description", "$.source")
                .build();
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> results1 = index.query(query1);
        long endTime = System.currentTimeMillis();

        List<String> resultDocs = new ArrayList<>();

        System.out.println("Elapsed time (ms): " + (endTime - startTime));
        System.out.println("User quesrtion: " + queryQuestion);
        System.out.println("Found " + results1.size() + " results:");
        for (Map<String, Object> result : results1) {
//            System.out.println("Title:"+result.get("$.title").toString()+"-->"+"Vector distance:"+result.get("vector_distance")+ "==>"+result.get("$.description").toString());
            System.out.println("Title:" + result.get("$.title").toString() + "==>" + result.get("$.description").toString());
            resultDocs.add(result.get("$.description").toString());
        }

        HFCrossEncoderReranker reranker = new HFCrossEncoderReranker();
        RerankResult rerankresult = reranker.rank(queryQuestion, resultDocs);

        List<?> rerankedDocs = rerankresult.getDocuments();
        System.out.println("Top result after reranking: " + rerankedDocs.get(0));

        return rerankedDocs.get(0).toString();
    }
    public static Map<String, Object>  searchwithDuration(String searchQuery) {
        // Connect to Redis
        UnifiedJedis jedis = new UnifiedJedis(
                new HostAndPort(redisHost, redisPort)
        );
        Map<String, Object> schema = Map.of(
                "index", Map.of(
                        "name", "news-index",
                        "prefix", "news",
                        "storage_type", "json"
                ),
                "fields", List.of(
                        Map.of("name", "classIndex", "type", "numeric"),
                        Map.of("name", "title", "type", "text"),
                        Map.of("name", "description", "type", "text"),
                        Map.of("name", "source", "type", "text"),
                        Map.of(
                                "name", "embeddings",
                                "type", "vector",
                                "attrs", Map.of(
                                        "dims", 384,
                                        "distance_metric", "cosine",
                                        "algorithm", "hnsw",
                                        "datatype", "float16"
                                )
                        )
                )
        );

        Map<String, Object> response = new HashMap<>();

        // Load schema from Dictionary
        // IndexSchema schema2 = IndexSchema.fromYaml(schemaYaml);
        IndexSchema schema2 = IndexSchema.fromDict(schema);
        // Create the search index
        SearchIndex index = new SearchIndex(schema2, jedis);
//        index.create(false);  // true = overwrite if exists

        ///  user input
        String queryQuestion = "Strong earnings from financial services";
        String queryQuestion2 = "Football match of World cup";
        String queryQuestion3 = "any news about blade server";
        queryQuestion = searchQuery;
        float[] queryVector = generateEmbedding(queryQuestion);
        // Set the pre-filter
        Filter filter1_source = Filter.and(
                Filter.fuzzy("$.source", "Reuters")
        );
        Filter filter2_classindex = Filter.numeric("$.classIndex").between(1, 4);

        // Build the hybrid query
        VectorQuery query1 = VectorQuery.builder()
                .vector(queryVector)
                .field("embeddings")
                .numResults(5)
                .returnScore(true)
                .withPreFilter(filter1_source.build())
                .withPreFilter(filter2_classindex.build())
                .returnFields("$.title", "$.description", "$.source")
                .build();
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> results1 = index.query(query1);
        long endTime = System.currentTimeMillis();

        List<String> resultDocs = new ArrayList<>();
        List<HashMap<String, Object>> resultQuery = new ArrayList<>();
        long durationMs = endTime - startTime;
        System.out.println("Elapsed time (ms): " + (endTime - startTime));
        System.out.println("User quesrtion: " + queryQuestion);
        System.out.println("Found " + results1.size() + " results:");
        for (Map<String, Object> result : results1) {
//            System.out.println("Title:"+result.get("$.title").toString()+"-->"+"Vector distance:"+result.get("vector_distance")+ "==>"+result.get("$.description").toString());
            System.out.println("source:"+result.get("$.source")+" classIndex:"+result.get("$.classIndex")+" Title:" + result.get("$.title").toString() + "==>" + result.get("$.description").toString());
            resultDocs.add(result.get("$.description").toString());
            HashMap <String, Object> midResult = new HashMap<>();
            midResult.put("source", result.get("$.source").toString());
            midResult.put("description", result.get("$.description").toString());
            midResult.put("classIndex", result.get("$.classIndex").toString());
            resultQuery.add(midResult);
        }

        HFCrossEncoderReranker reranker = new HFCrossEncoderReranker();
        RerankResult rerankresult = reranker.rank(queryQuestion, resultDocs);

        List<?> rerankedDocs = rerankresult.getDocuments();
        System.out.println("Top result after reranking: " + rerankedDocs.get(0));

        response.put("results", resultQuery);
        response.put("timeMs", durationMs);
        response.put("count", results1.size());
        response.put("rerankingResult", rerankedDocs.get(0));
        return response;

    }
    private static float[] generateEmbedding(String text) {

        String cacheDir = "./models/all-MiniLM-L6-v2";
        SentenceTransformersVectorizer vectorizer =
                new SentenceTransformersVectorizer("sentence-transformers/all-MiniLM-L6-v2", cacheDir);

        float[] embedding = vectorizer.embed(text);
        return embedding;
    }
}


