package com.sittingspot.searchservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.searchservice.model.Area;
import com.sittingspot.searchservice.model.Location;
import com.sittingspot.searchservice.model.QueryResult;
import com.sittingspot.searchservice.model.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class SearchServiceController {

    @Value("${sittingspot.queryoptimizer.url}")
    private String queryOptimizerUrl;
    @Value("${sittingspot.searchadapter.url}")
    private String searchAdapterUrl;

    @GetMapping
    public List<QueryResult> searchSpot(@RequestParam("x") Double x,
                                        @RequestParam("y") Double y,
                                        @RequestParam("area") Double area,
                                        @RequestParam(value = "tags",required = false) List<Tag> tags,
                                        @RequestParam(value = "labels",required = false) List<String> labels) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();

        var queryOptimizerRequestUrl = "http://" + queryOptimizerUrl + "?x=" + x + "&y=" + y + "&area="+area;
        if(tags != null){
            queryOptimizerRequestUrl += URLEncoder.encode("&tags="+tags,"UTF-8");
        }
        if(labels != null){
            queryOptimizerRequestUrl += URLEncoder.encode("&labels="+labels,"UTF-8");
        }

        log.info("Sending request: " + queryOptimizerRequestUrl);
        // first forward the request to the query optimizer
        // if it's possible to answer the query without invoking the adapter it does so.
        var optimizeRequest = HttpRequest.newBuilder()
                .uri(URI.create(queryOptimizerRequestUrl))
                .header("Content-Type", "application/json")
                .build();
        var optimizeResult = client.send(optimizeRequest, HttpResponse.BodyHandlers.ofString());

        log.info("Got response code " + optimizeResult.statusCode());
        if (optimizeResult.statusCode() == 200) {
            List<QueryResult> data = (new ObjectMapper()).readerForListOf(QueryResult.class).readValue(optimizeResult.body());
            return data;
        }

        var searchAdapterRequestUrl = "http://" + searchAdapterUrl + "?x=" + x + "&y=" + y + "&area="+area;
        if(tags != null){
            searchAdapterRequestUrl += URLEncoder.encode("&tags="+tags,"UTF-8");
        }
        if(labels != null){
            searchAdapterRequestUrl +=  URLEncoder.encode("&labels="+labels,"UTF-8");
        }

        log.info("Sending request: " + searchAdapterRequestUrl);
        // as the optimizer didn't have enough past query data to answer the current query
        // it forwoards it to the adapter.
        var searchRequest = HttpRequest.newBuilder()
                .uri(URI.create(searchAdapterRequestUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        var searchResult = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());

        log.info("Got response code " + searchResult.statusCode());
        if (searchResult.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        List<QueryResult> data = (new ObjectMapper()).readerForListOf(QueryResult.class).readValue(searchResult.body());
        return data;
    }
}
