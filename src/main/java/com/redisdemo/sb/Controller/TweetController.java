package com.redisdemo.sb.Controller;

import com.redisdemo.sb.Services.HybridSearchHelper;
import com.redisdemo.sb.Services.TweetHybridSearchService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class TweetController {
    @GetMapping("/api/tweethybridsearch")
    public Map<String, Object> tweetHybridsearch(
            @RequestParam String searchString, @RequestParam int radius) {
        TweetHybridSearchService tweethybridSearchHelper = new TweetHybridSearchService(searchString,radius);
        return tweethybridSearchHelper.search(searchString);

    }
}

