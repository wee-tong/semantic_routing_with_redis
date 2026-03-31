package com.redisdemo.sb.Services;

import com.redis.vl.index.SearchIndex;
import com.redis.vl.query.Filter;
import com.redis.vl.query.VectorQuery;
import com.redis.vl.schema.IndexSchema;
import com.redis.vl.utils.rerank.HFCrossEncoderReranker;
import com.redis.vl.utils.rerank.RerankResult;
import com.redis.vl.utils.vectorize.SentenceTransformersVectorizer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class TweetHybridSearchService {
    String searchQuery;
    float radius;
    static final String redisHost ="localhost";
    static final int redisPort  = 6479;;

    public TweetHybridSearchService(String searchQuery, int radius) {
        this.searchQuery = searchQuery;
        this.radius = radius;
    }

    public  Map<String, Object> search(String searchQuery) {
        // Connect to Redis
        UnifiedJedis jedis = new UnifiedJedis(
                new HostAndPort(redisHost, redisPort)
        );
        Map<String, Object> schema = Map.of(
                "index", Map.of(
                        "name", "tweet-index",
                        "prefix", "tweet",
                        "storage_type", "json"
                ),
                "fields", List.of(
                        Map.of("name", "text", "type", "text"),
                        Map.of("name", "location", "type", "geo"),
                        Map.of("name", "sentiment", "type", "numeric"),
                        Map.of(
                                "name", "embedding",
                                "type", "vector",
                                "attrs", Map.of(
                                        "dims", 384,
                                        "distance_metric", "cosine",
                                        "algorithm", "flat",
                                        "datatype", "float16"
                                )
                        )
                )
        );
        IndexSchema schema2 = IndexSchema.fromDict(schema);
        SearchIndex index = new SearchIndex(schema2, jedis);
        index.create(true);  // true = overwrite if exists

        List<Map<String, Object>> myTweetHybridSearchResult = new ArrayList<>();
        Map<String, Object> myTweetHybridSearchObj = new HashMap<>();
        double totalDuration = 0.0;

        ///  user input
        String queryQuestion = searchQuery;

        float[] queryVector = generateEmbedding(queryQuestion);
        // Set the pre-filter - positive sentiment
        Filter filter1_source = Filter.numeric("$.sentiment").gt(0.0);

        // Build the hybrid query
        VectorQuery query1 = VectorQuery.builder()
                .vector(queryVector)
                .field("embedding")
                .numResults(5)
                .returnScore(true)
                .withPreFilter(filter1_source.build())
                .returnFields("$.text", "$.location", "$.sentiment")
                .build();
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> results1 = index.query(query1);
        long endTime = System.currentTimeMillis();

        List<String> resultDocs = new ArrayList<>();
        Map<String,Object> locationMap = new HashMap<>();

        totalDuration = endTime - startTime;

        System.out.println("Elapsed time (ms): " + (endTime - startTime));
        System.out.println("User question: " + queryQuestion);
        System.out.println("Found " + results1.size() + " results:");
        for (Map<String, Object> result : results1) {
            System.out.println("text:"+result.get("$.text")+" location:"+result.get("$.location")+" sentiment:"+result.get("$.sentiment").toString());
            resultDocs.add(result.get("$.text").toString());
            locationMap.put(result.get("$.key").toString(), result.get("$.text").toString());
        }
        HFCrossEncoderReranker reranker = new HFCrossEncoderReranker();
        RerankResult rerankresult = reranker.rank(queryQuestion, resultDocs);
        List<?> rerankedDocs = rerankresult.getDocuments();
        System.out.println("Top result after reranking: " + rerankedDocs.get(0));

        for (Map.Entry<String, Object> entry : locationMap.entrySet()) {
            if (Objects.equals(entry.getValue(), rerankedDocs.get(0))) {
                System.out.println("key is:" + entry.getKey());
                String focusKey = entry.getKey();
                //get the focused key
                Map<String, Object> anchorDoc = index.fetch(focusKey);

                Object locObj = anchorDoc.get("location");
                myTweetHybridSearchObj.put("location", locObj);
                if (locObj instanceof String location) {

                    String[] parts = location.split(",");

                    if (parts.length == 2) {
                        double lon = Double.parseDouble(parts[0]);
                        double lat = Double.parseDouble(parts[1]);

                        System.out.println("Lon: " + lon + ", Lat: " + lat);

                        Filter filter2_sentiment = Filter.numeric("$.sentiment").gt(-0.0);
                        Filter filter2_geo= Filter.geo("$.location").radius(lon,lat,radius, Filter.GeoUnit.KM);

                        VectorQuery query2 = VectorQuery.builder()
                                .vector(queryVector)
                                .field("embedding")
                                .numResults(20)
                                .returnScore(true)
                                .withPreFilter(filter2_geo.build())
//                                .withPreFilter(filter2_sentiment.build())
                                .returnFields("$.text", "$.location", "$.sentiment")
                                .build();
                        long startTime2 = System.currentTimeMillis();
                        List<Map<String, Object>> results2 = index.query(query2);
                        long endTime2 = System.currentTimeMillis();
                        totalDuration += endTime2 - startTime2;
                        System.out.println("Elapsed time (ms): " + (endTime2 - startTime2));
                        System.out.println("Found " + results2.size() + " results:");
                        for (Map<String, Object> result : results2) {
                            myTweetHybridSearchResult.add(Map.of("text",result.get("$.text"),
                                    "location",result.get("$.location"),
                                    "sentiment",result.get("$.sentiment")));
                            System.out.println("text:"+result.get("$.text")+" location:"+result.get("$.location")+" sentiment:"+result.get("$.sentiment").toString());
                        }
                        myTweetHybridSearchObj.put("totalDuration", totalDuration);
                        myTweetHybridSearchObj.put("results", myTweetHybridSearchResult);


                    }
                }

            }

        }
        return myTweetHybridSearchObj;
    }

    private static float[] generateEmbedding(String text) {

        String cacheDir = "./models/all-MiniLM-L6-v2";
        SentenceTransformersVectorizer vectorizer =
                new SentenceTransformersVectorizer("sentence-transformers/all-MiniLM-L6-v2",cacheDir);
        float[] embedding = vectorizer.embed(text);
        return embedding;
    }


}
