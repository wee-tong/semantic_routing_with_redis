package com.redisdemo.sb.Services;

import com.redis.vl.extensions.router.RouteMatch;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Map;

public class SemanticRouterServiceTester {

    public static void main(String[] args) throws Exception
    {
        final String apiKey = System.getenv("OPENAIAPI_KEY");
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6379");
        SemanticRouterService router = new SemanticRouterService(jedis,apiKey);
        RouteMatch routeMatch = router.build().route("Can you tell me about the latest in artificial intelligence?");
        System.out.println(routeMatch);
//
//        RouteMatch routeMatch2 = router.build().route("Tell my about Michael Jordan?");
//        System.out.println(routeMatch2);

//        List<Map<String, Object>> refs = router.build().getRouteReferences("technology",null,null);
//        System.out.println(refs);
        System.out.println(router.getRouter());
    }
}
