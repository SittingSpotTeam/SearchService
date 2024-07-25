package com.sittingspot.searchservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sittingspot.searchservice.model.Area;
import com.sittingspot.searchservice.model.Location;
import com.sittingspot.searchservice.model.QueryResult;
import com.sittingspot.searchservice.model.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@RestController("/api/v1")
public class SearchServiceController {

    @Value("${sittingspot.queryoptimizer.url}")
    private String queryOptimizerUrl;

    @Value("${sittingspot.queryoptimizer.api.version}")
    private String queryOptimizerApiVersion;

    @Value("${sittingspot.searchadapter.url}")
    private String searchAdapterUrl;

    @Value("${sittingspot.searchadapter.api.version}")
    private String searchAdapterApiVersion;

    @GetMapping("/")
    public List<QueryResult> search(@RequestParam("location") Area location,
                                    @RequestParam(value = "tags",required = false) List<Tag> tags,
                                    @RequestParam(value = "labels",required = false) List<String> labels) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();

        var optimizeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + queryOptimizerUrl + queryOptimizerApiVersion +
                        "/?location="+location+"&tags="+tags+"&labels="+labels))
                .GET().build();
        var optimizeResult = client.send(optimizeRequest, HttpResponse.BodyHandlers.ofString());

        if (optimizeResult.statusCode() == 200) {
            List<QueryResult> data = (new ObjectMapper()).readerForListOf(QueryResult.class).readValue(optimizeResult.body());
            return data;
        }

        var searchRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://"+ searchAdapterUrl + searchAdapterApiVersion +
                        "/?location="+location+"&tags="+tags+"&labels="+labels))
                .GET()
                .build();
        var searchResult = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());

        if (searchResult.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        List<QueryResult> data = (new ObjectMapper()).readerForListOf(QueryResult.class).readValue(searchResult.body());
        return data;
    }
}
