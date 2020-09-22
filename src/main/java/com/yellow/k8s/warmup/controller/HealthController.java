package com.yellow.k8s.warmup.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    @RequestMapping(value="/info")
    public Mono<String> health() {
        return Mono.just("ok");
    }

}
