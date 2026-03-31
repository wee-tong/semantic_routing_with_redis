package com.redisdemo.sb.Controller;

import com.redis.vl.query.Filter;
import com.redisdemo.sb.Services.SupportBotService;
import com.redisdemo.sb.Services.Vectorizer;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.UnifiedJedis;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class SemanticCacheController {

    final String apiKey = System.getenv("OPENAIAPI_KEY");
    final UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6379");
    final String TextModelName = "text-embedding-3-small";

    @PostMapping("/api/getsemanticcachewithFilter")
    public Map<String, Object> getSemanticAnswerwithFilter(@RequestBody Map<String, String> payload)
       {

        String userquestion = payload.get("userquestion");
        String user = payload.get("user");
        System.out.println(user);
        Vectorizer vectorizer = new Vectorizer(apiKey, TextModelName);
        SupportBotService bot = new SupportBotService(jedis, vectorizer.vectorizer());
        Filter f = Filter.tag("user", user);
        return bot.answerQuestionwithFilter(userquestion,f);
    }


    @PostMapping("/api/getsemanticcache")
    public Map<String, Object> getSemanticAnswer(@RequestBody Map<String, String> payload)
        {
        String userquestion = payload.get("userquestion");
        Vectorizer vectorizer = new Vectorizer(apiKey, TextModelName);
        SupportBotService bot = new SupportBotService(jedis, vectorizer.vectorizer());
        return bot.answerQuestionwithStatistics(userquestion);
    }

    @GetMapping("/api/clearsemanticcache")
    public Boolean clearEntries()
         {
        Vectorizer vectorizer = new Vectorizer(apiKey,TextModelName);
        SupportBotService bot = new SupportBotService(jedis, vectorizer.vectorizer());
        bot.clearOldEntries();
        return true;
    }
}
