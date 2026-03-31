package com.redisdemo.sb.Controller;

import com.redisdemo.sb.Services.FTSearchHelper;
import io.lettuce.core.search.SearchReply;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class FTSearchController {

    @GetMapping("/api/search")
    public List<SearchReply.SearchResult<String, String>> index() {

        final String searchPrefix = "idx:news";
        final String searchQuery = "@source:(Reuters) @description:(oil price) @classIndex:[1 1]";
        FTSearchHelper mySearchHelper = new FTSearchHelper(searchPrefix, searchQuery);
        List<SearchReply.SearchResult<String, String>> mySearchResult = mySearchHelper.search();
        return mySearchResult;
    }

    @GetMapping("/api/search2")
    public List<SearchReply.SearchResult<String, String>> search(
            @RequestParam String source,
            @RequestParam String description,
            @RequestParam Integer classIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("@source:(").
                append(source).
                append(")").
                append("@description:(").
                append(description).
                append(")").append("@classIndex:[").
                append(classIndex).
                append(" ").
                append(classIndex).append("]");

        final String searchQuery = sb.toString();
        System.out.println("searchQuery:"+searchQuery);
        FTSearchHelper mySearchHelper = new FTSearchHelper("idx:news", searchQuery);
        List<SearchReply.SearchResult<String, String>> mySearchResult = mySearchHelper.search();
        return mySearchResult;
// curl -L "http://localhost:8080/api/search2?source=Reuters&description=oil%20price&classIndex=1"

    }

    @GetMapping("/api/ftsearch")
    public Map<String, Object> ftsearch(
            @RequestParam String source,
            @RequestParam String description,
            @RequestParam Integer classIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("@source:(").
                append(source).
                append(")").
                append("@description:(").
                append(description).
                append(")").append("@classIndex:[").
                append(classIndex).
                append(" ").
                append(classIndex).append("]");

        final String searchQuery = sb.toString();
        System.out.println("searchQuery:"+searchQuery);
        FTSearchHelper mySearchHelper = new FTSearchHelper("idx:news", searchQuery);
        Map<String, Object> mySearchResult = mySearchHelper.searchwithDuration();
        return mySearchResult;
// curl -L "http://localhost:8080/api/search2?source=Reuters&description=oil%20price&classIndex=1"
    }
}
