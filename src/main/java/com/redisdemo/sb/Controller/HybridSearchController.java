package com.redisdemo.sb.Controller;

import com.redisdemo.sb.Services.HybridSearchHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class HybridSearchController {

    @GetMapping("/api/hsearch")
    public String hybridSearch() {
        String dummySearchString ="Strong earnings from financial services";
        HybridSearchHelper hybridSearchHelper = new HybridSearchHelper(dummySearchString);
        return hybridSearchHelper.search(dummySearchString);
//        return dummySearchString;
    }
//curl -L "http://localhost:8080/api/hsearch2?searchString=searchme"
    @GetMapping("/api/hsearch2")
    public String hybridsearch2(
            @RequestParam String searchString) {
        HybridSearchHelper hybridSearchHelper = new HybridSearchHelper(searchString);
        return hybridSearchHelper.search(searchString);

    }
    @GetMapping("/api/hbsearch")
    public Map<String, Object> hbsearch(
            @RequestParam String searchString) {
        HybridSearchHelper hybridSearchHelper = new HybridSearchHelper(searchString);
        return hybridSearchHelper.searchwithDuration(searchString);

    }
}
