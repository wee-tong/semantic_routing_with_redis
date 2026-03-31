package com.redisdemo.sb.Services;

import com.redis.vl.extensions.router.DistanceAggregationMethod;
import com.redis.vl.extensions.router.Route;
import com.redis.vl.extensions.router.RoutingConfig;
import com.redis.vl.extensions.router.SemanticRouter;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

public class SemanticRouterService {

    private UnifiedJedis  jedis;
    private final Vectorizer vectorizer;
    private Map<String, List<String>> currentRouterConfig;

    public SemanticRouterService(UnifiedJedis jedis, String apiKey) {
        this.jedis = jedis;
        this.vectorizer = new Vectorizer(apiKey,"text-embedding-3-small");

//        Map<String, List<String>> defaultRouterConfig = new HashMap<>();
//
//        defaultRouterConfig.put("sports", List.of(
//                "who won the game last night?",
//                "tell me about the upcoming sports events",
//                "what's the latest in the world of sports?",
//                "sports",
//                "basketball and football"
//        ));
//        defaultRouterConfig.put("entertainment", List.of(
//                "what are the top movies right now?",
//                "who won the best actor award?",
//                "what's new in the entertainment industry?"
//        ));
//        defaultRouterConfig.put("technology", List.of(
//                "what are the latest advancements in AI?",
//                "tell me about the newest gadgets",
//                "what's trending in tech?"
//        ));
//        defaultRouterConfig.put("politics", List.of(
//                "what are the latest political developments?",
//                "who won the recent election?",
//                "what policies are being debated right now?",
//                "politics",
//                "government and public policy"
//        ));
//        defaultRouterConfig.put("business", List.of(
//                "what are the latest stock market trends?",
//                "tell me about major company mergers?",
//                "what's happening in the global economy?",
//                "business",
//                "finance and investment news"
//        ));
////        this.currentRouterConfig= defaultRouterConfig;
//
//        System.out.println("Map size: " + defaultRouterConfig.size());
//        System.out.println(defaultRouterConfig.keySet());
//
//        for (Map.Entry<String, List<String>> entry : defaultRouterConfig.entrySet()) {
//
//            String redisKey = "router:" + entry.getKey();
//
//            int index = 0;
//            for (String question : entry.getValue()) {
//                jedis.hset(redisKey, String.valueOf(index), question);
//                index++;
//            }
//        }

    }
    public Boolean loaddefaultRouterConfig() {

        Map<String, List<String>> defaultRouterConfig = new HashMap<>();

        defaultRouterConfig.put("sports", List.of(
                "who won the game last night?",
                "tell me about the upcoming sports events",
                "what's the latest in the world of sports?",
                "sports",
                "basketball and football"
        ));
        defaultRouterConfig.put("entertainment", List.of(
                "what are the top movies right now?",
                "who won the best actor award?",
                "what's new in the entertainment industry?"
        ));
        defaultRouterConfig.put("technology", List.of(
                "what are the latest advancements in AI?",
                "tell me about the newest gadgets",
                "what's trending in tech?"
        ));
        defaultRouterConfig.put("politics", List.of(
                "what are the latest political developments?",
                "who won the recent election?",
                "what policies are being debated right now?",
                "politics",
                "government and public policy"
        ));
        defaultRouterConfig.put("business", List.of(
                "what are the latest stock market trends?",
                "tell me about major company mergers?",
                "what's happening in the global economy?",
                "business",
                "finance and investment news"
        ));

        System.out.println("Map size: " + defaultRouterConfig.size());
        System.out.println(defaultRouterConfig.keySet());

        try {
            //delete all old keys
            String pattern = "router:*";
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }

            for (Map.Entry<String, List<String>> entry : defaultRouterConfig.entrySet()) {

                String redisKey = "router:" + entry.getKey();

                int index = 0;
                for (String question : entry.getValue()) {
                    jedis.hset(redisKey, String.valueOf(index), question);
                    index++;
                }
            }

            return true;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    public Boolean updateRouter(Map<String, List<String>> newRouterConfig)
    {

       newRouterConfig.forEach((className, questions) -> {
           System.out.println("Class: " + className);

           questions.forEach(q ->
                   System.out.println("Question: " + q));

           String redisKey = "router:"+className;
           jedis.del(redisKey);

           int index = 0;
           for (String question : questions) {
               jedis.hset(redisKey, String.valueOf(index), question);
               index++;
           }

       });

        return true;


    }
    public Map<String, List<String>> getRouter()
    {
        Map<String, List<String>> currentRouterConfig = new HashMap<>();
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams params = new ScanParams().match("router:*").count(100);
        do {
            ScanResult<String> scanResult = jedis.scan(cursor, params);
            List<String> keys = scanResult.getResult();

            for (String key : keys) {

                // Remove "router:" prefix
                String category = key.replace("router:", "");

                // Get HASH fields
                Map<String, String> hashValues = jedis.hgetAll(key);

                if (!hashValues.isEmpty()) {

                    // Convert hash values → ordered list
                    List<String> questions = new ArrayList<>();

                    // 🔥 Important: sort by numeric field key (0,1,2)
                    hashValues.entrySet().stream()
                            .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())))
                            .forEach(e -> questions.add(e.getValue()));

                    currentRouterConfig.put(category, questions);
                }
            }

            cursor = scanResult.getCursor();

        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        return currentRouterConfig;
    }
    public SemanticRouter build(){

        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams params = new ScanParams().match("router:*").count(500);
        List<Route> routeList = new ArrayList<Route>() ;

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, params);

            List<String> scannedKeys = scanResult.getResult();
            if (!scannedKeys.isEmpty()) {
                for (String key : scannedKeys) {

                    Map<String, String> hashValues = jedis.hgetAll(key);
                    List<String> values = new ArrayList<>(hashValues.values());
//                    List<String> values = jedis.lrange(key, 0, -1);
                    Route routes = Route.builder()
                            .name(key)
                            .references(values)
                            .metadata(Map.of("category", key, "priority", 1))
                            .distanceThreshold(0.71)
                            .build();
                    routeList.add(routes);
                }
            }

            cursor = scanResult.getCursor();

        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        // maxK = how many routes you get back;
        // aggregationMethod = how their per-reference distances are combined into a single route distance for ranking.
        RoutingConfig cfg = RoutingConfig.builder()
                .maxK(5)
                .aggregationMethod(DistanceAggregationMethod.AVG) // or MIN / SUM
                .build();

        SemanticRouter router =  SemanticRouter.builder()
                .name("my-router")
                .routes(routeList)
                .routingConfig(cfg)
                .vectorizer(vectorizer.vectorizer())
                .jedis(jedis)
                .overwrite(true)
                .build();
        return  router;
    }
}
