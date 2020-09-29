package com.yellow.k8s.warmup.controller;

import com.yellow.k8s.warmup.service.CallBackService;
import com.yellow.k8s.warmup.vo.WarmUpBatchRequest;
import com.yellow.k8s.warmup.vo.WarmUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 预热回调接口
 * @author YellowTail
 * @since 2020-09-22
 */
@RestController
@RequestMapping("/callback")
public class CallBackController {

    @Autowired
    private CallBackService callBackService;

    @PostMapping(value="/single", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> single(@RequestBody final WarmUpRequest request) {
        return callBackService.single(request);
    }

    @PostMapping(value="/multi", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> multi(@RequestBody final WarmUpBatchRequest request) {
        return callBackService.multi(request);
    }
}
