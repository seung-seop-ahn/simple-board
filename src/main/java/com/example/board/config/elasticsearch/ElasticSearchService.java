package com.example.board.config.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ElasticSearchService {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @Autowired
    public ElasticSearchService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public List<Long> search(String index, String keyword) throws ExecutionException, InterruptedException {
        // Fetch 10 article _id list
        String query = String.format("{ \"_source\": false, \"query\": { \"match\": { \"contents\": \"%s\" } }, \"fields\": [\"_id\"], \"size\": 10 }", keyword);
        return webClient.post()
                .uri("/{index}/_search", index)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(query)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::extractIds)
                .toFuture().get();
    }

    public Mono<String> indexDocument(String index, String id, String document) {
        return webClient.put()
                .uri("/{index}/_doc/{id}", index, id)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(document)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<List<Long>> extractIds(String body) {
        List<Long> ids = new ArrayList<>();
        try {
            JsonNode hits = objectMapper.readTree(body).path("hits").path("hits");
            hits.forEach(hit -> ids.add(hit.path("_id").asLong()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Mono.just(ids);
    }
}
