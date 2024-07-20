package com.sittingspot.searchservice.controller;

import com.sittingspot.searchservice.model.Area;
import com.sittingspot.searchservice.model.Location;
import com.sittingspot.searchservice.model.QueryResult;
import com.sittingspot.searchservice.model.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("search-logic/api/v1")
public class SearchServiceController {

    @GetMapping("/")
    public List<QueryResult> search(@RequestParam("queryId")UUID queryID,
                                    @RequestParam("location") Area location,
                                    @RequestParam(value = "tags",required = false) List<Tag> tags,
                                    @RequestParam(value = "labels",required = false) List<String> labels) {
        return List.of();
    }
}
