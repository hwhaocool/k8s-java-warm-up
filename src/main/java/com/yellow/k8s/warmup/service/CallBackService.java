package com.yellow.k8s.warmup.service;

import com.yellow.k8s.warmup.model.PodEvent;
import com.yellow.k8s.warmup.vo.WarmUpRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author YellowTail
 * @since 2020-09-22
 */
@Service
public class CallBackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallBackService.class);

    @Autowired
    @Qualifier("k8sClient")
    private WebClient webClient;

//    @Autowired
    private PodStatusCheckService podStatusCheckService;

    public Mono<String> single(WarmUpRequest request) {

        ObjectId objectId = ObjectId.get();

        List<URI> uriList = genUri(request);

        sendOneByOne(request, uriList, 0);

        return Mono.just(objectId.toHexString());
    }

    private void sendOneByOne(WarmUpRequest request, List<URI> uriList, int index) {

        LOGGER.info("sendOneByOne current index {}", index);

        // 检查 pod 状态是否 ready， 如果是就停止
        // 计算下次索引
        // 构造 请求
        // 收到response之后， 得到时间，记录时间到 db， 且 递归调用

        // 1. 检查pod 状态
        String podIp = request.getIp();
//        if (podStatusCheckService.isPodReady(podIp)) {
//            // 已就绪，就停止
//            LOGGER.info("pod {} is ready, stop", podIp);
//            return;
//        }



        // 2. 计算下次 index
        int nextIndex = genIndex(uriList, index);

        // 3. 构造 请求
        HttpMethod method = HttpMethod.GET;
        String requestMethod = request.getMethod();
        if (StringUtils.isNotBlank(requestMethod)) {
            method = HttpMethod.resolve(requestMethod.toUpperCase());
        }

        WebClient.RequestBodyUriSpec requester = webClient.method(method);

        Map<String, String> headers = request.getHeaders();
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(requester::header);
        }

        requester.uri(uriList.get(index))
                .retrieve()
                .toBodilessEntity()
                .elapsed()
                .doOnNext(tuple -> LOGGER.info("Milliseconds for retrieve {}" , tuple.getT1()))
                .map(k -> k.getT2())
                .subscribe( k -> sendOneByOne(request, uriList, nextIndex));
    }

    public Mono<String> multi(WarmUpRequest request) {
        ObjectId objectId = ObjectId.get();


        return Mono.just(objectId.toHexString());
    }

    private int genIndex(List<URI> uriList, int index) {

        return index + 1 >= uriList.size() ? 0 : index + 1;
    }

    /**
     * 生成 uri 列表
     * @param request
     * @author YellowTail
     * @since 2020-06-16
     */
    private List<URI> genUri(WarmUpRequest request) {
        List<String> paramList = request.getParamList();
        String queryParamName = request.getQueryParamName();

        if (CollectionUtils.isEmpty(paramList)) {
            return List.of(new DefaultUriBuilderFactory()
                    .builder()
                    .scheme("http")
                    .host(request.getIp())
                    .port(request.getPort())
                    .path(request.getUri())
                    .build());
        }

        return paramList.stream()
                .map(k -> {
                    return new DefaultUriBuilderFactory()
                            .builder()
                            .scheme("http")
                            .host(request.getIp())
                            .port(request.getPort())
                            .path(request.getUri())
                            .queryParam(queryParamName, k)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
