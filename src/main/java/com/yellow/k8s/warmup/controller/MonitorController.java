package com.yellow.k8s.warmup.controller;

import com.github.abel533.echarts.Option;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
import com.yellow.k8s.warmup.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 预热页面查看、监控接口
 * @author YellowTail
 * @since 2020-09-22
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @GetMapping(value="/list", produces= MediaType.APPLICATION_JSON_VALUE)
    public Flux<RequestDocument> list(@RequestParam(value = "page", required = false, defaultValue = "0") final Integer page) {

        return monitorService.getList(page);
    }

    @GetMapping(value="/info", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<Option> info(@RequestParam("id") final String id) {

        return monitorService.getOne(id);
    }


}
