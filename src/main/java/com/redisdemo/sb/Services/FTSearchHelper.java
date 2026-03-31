package com.redisdemo.sb.Services;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RediSearchAsyncCommands;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.search.SearchReply;
import io.lettuce.core.search.arguments.CreateArgs;
import io.lettuce.core.search.arguments.FieldArgs;
import io.lettuce.core.search.arguments.NumericFieldArgs;
import io.lettuce.core.search.arguments.TextFieldArgs;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FTSearchHelper {
    String serarchPrefix;
    String searchQuery;
    SearchReply.SearchResult<String, String> ftsearchresults;
    final String redisConnection = "redis://localhost:6479";

    public FTSearchHelper(String serarchPrefix, String searchQuery) {
        this.searchQuery = searchQuery;
        this.serarchPrefix = serarchPrefix;
    }

    public  List<SearchReply.SearchResult<String, String>> search() {

        RedisClient redisClient = RedisClient.create(redisConnection);
        SearchReply.SearchResult<String, String> getSearchResults = new SearchReply.SearchResult<String, String>();
        AtomicReference<List<SearchReply.SearchResult<String, String>>> redisResults = new AtomicReference<>(new ArrayList<>());
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisAsyncCommands<String, String> asyncCommands = connection.async();
            RediSearchAsyncCommands<String, String> searchCommands = connection.async();

            List<FieldArgs<String>> schema = Arrays.asList(TextFieldArgs.<String>builder().name("$.source").as("source").build(),
                    NumericFieldArgs.<String>builder().name("$.classIndex").as("classIndex").build(),
                    TextFieldArgs.<String>builder().name("$.description").as("description").build());

            CreateArgs<String, String> createArgs = CreateArgs.<String, String>builder().on(CreateArgs.TargetType.JSON)
                    .withPrefix("news:").build();

            CompletableFuture<Void> make_index = searchCommands.ftCreate("idx:news", createArgs, schema)
                    .thenAccept(System.out::println) // >>> OK
                    .toCompletableFuture();

            CompletableFuture<SearchReply<String, String>> query1 =
                    searchCommands.ftSearch("idx:news", searchQuery) //"@source:(Reuters) @description:(oil price) @classIndex:[1 1]")
                            .thenApply(res -> {
                                List<SearchReply.SearchResult<String, String>> results = res.getResults();
                                results.forEach(result -> {
                                    getSearchResults.addFields(result.getFields());
                                    System.out.println(result.getFields());
                                    //redisResults.get().add(getSearchResults);
                                });
                                redisResults.set(res.getResults());
                                return res;
                            }).toCompletableFuture();

            long startTime = System.currentTimeMillis();
            query1.join();
            long endTime = System.currentTimeMillis();
            System.out.println("Elapsed time (ms): " + (endTime - startTime));

        }
        return redisResults.get();
    }

    public  Map<String, Object> searchwithDuration() {

        RedisClient redisClient = RedisClient.create(redisConnection);
        SearchReply.SearchResult<String, String> getSearchResults = new SearchReply.SearchResult<String, String>();
        AtomicReference<List<SearchReply.SearchResult<String, String>>> redisResults = new AtomicReference<>(new ArrayList<>());
        Map<String, Object> response = new HashMap<>();

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisAsyncCommands<String, String> asyncCommands = connection.async();
            RediSearchAsyncCommands<String, String> searchCommands = connection.async();

            List<FieldArgs<String>> schema = Arrays.asList(TextFieldArgs.<String>builder().name("$.source").as("source").build(),
                    NumericFieldArgs.<String>builder().name("$.classIndex").as("classIndex").build(),
                    TextFieldArgs.<String>builder().name("$.description").as("description").build());

            CreateArgs<String, String> createArgs = CreateArgs.<String, String>builder().on(CreateArgs.TargetType.JSON)
                    .withPrefix("news:").build();

            CompletableFuture<Void> make_index = searchCommands.ftCreate("idx:news", createArgs, schema)
                    .thenAccept(System.out::println) // >>> OK
                    .toCompletableFuture();

            CompletableFuture<SearchReply<String, String>> query1 =
                    searchCommands.ftSearch("idx:news", searchQuery) //"@source:(Reuters) @description:(oil price) @classIndex:[1 1]")
                            .thenApply(res -> {
                                List<SearchReply.SearchResult<String, String>> results = res.getResults();
                                results.forEach(result -> {
                                    getSearchResults.addFields(result.getFields());
                                    System.out.println(result.getFields());
                                    //redisResults.get().add(getSearchResults);
                                });
                                redisResults.set(res.getResults());
                                return res;
                            }).toCompletableFuture();

            long startTime = System.currentTimeMillis();
            query1.join();
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;

            response.put("results", redisResults.get());
            response.put("timeMs", durationMs);
            response.put("count", redisResults.get().size());

            System.out.println("Elapsed time (ms): " + (endTime - startTime));

        }
        return response;
    }
}


