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

        System.out.println(router.getRouter());
    }
}
