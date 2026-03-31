package com.redisdemo.sb.Controller;

import com.redis.vl.extensions.router.RouteMatch;
import com.redisdemo.sb.Services.SemanticRouterService;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class SemanticRouterController {
    final String apiKey = System.getenv("OPENAIAPI_KEY");
    final UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6479");

    @GetMapping("/api/getrouter")
    public Map<String, List<String>> getRouter()  {
        SemanticRouterService router = new SemanticRouterService(jedis, apiKey);
        return router.getRouter();
    }

    @GetMapping("/api/routequestion")
    public String routequestion(
            @RequestParam String userquestion)  {
        SemanticRouterService router = new SemanticRouterService(jedis,apiKey);
        RouteMatch routeMatch = router.build().route(userquestion);
        return routeMatch.getName();
    }

    @PostMapping("/api/loaddefaultrouteconfig")
    public Boolean loaddefaultrouterquestion()  {
        SemanticRouterService router = new SemanticRouterService(jedis,apiKey);
        return router.loaddefaultRouterConfig();
    }

    @PostMapping("/api/updateroutequestion")
    public Map<String, Object> updateroutequestion(
            @RequestBody Map<String, List<String>> payload)
       {
        SemanticRouterService router = new SemanticRouterService(jedis,apiKey);
        Boolean routUpdateStatus = router.updateRouter(payload);
        System.out.println(routUpdateStatus);

        // Loop through each class
        //        for (Map.Entry<String, List<String>> entry : payload.entrySet()) {
        //
        //            String className = entry.getKey();
        //            List<String> questions = entry.getValue();
        //
        //            System.out.println("Class: " + className);
        //
        //            // Print questions one by one
        //            for (String question : questions) {
        //                System.out.println("  Question: " + question);
        //            }
        //
        //            System.out.println("---------------------------");
        //        }

        return Map.of("status", "received successfully");
    }
}
