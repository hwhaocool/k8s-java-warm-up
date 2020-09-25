package com.yellow.k8s.warmup.service;

import com.yellow.k8s.warmup.dao.HttpStatusRepository;
import com.yellow.k8s.warmup.dao.RequestRepository;
import com.yellow.k8s.warmup.dbdoc.HttpStatusDocument;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
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
import reactor.util.function.Tuple2;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private HttpStatusRepository httpStatusRepository;

    @Autowired
    private PodStatusCheckService podStatusCheckService;

    public Mono<String> single(WarmUpRequest request) {
        // 1. 生成 请求id
        final ObjectId requestId = ObjectId.get();

        // 2. 生成 uri 列表
        final List<URI> uriList = genUri(request);

        // 3. 保存请求信息
        requestRepository.save(genRequestDoc(request, requestId))
            .subscribe();

        // 4. 发送
        sendOneByOne(request, uriList, 0, requestId);

        return Mono.just(requestId.toHexString());
    }



    private void sendOneByOne(final WarmUpRequest request, final List<URI> uriList, final int index, final ObjectId requestId) {

        LOGGER.info("sendOneByOne current index {}", index);

        // 检查 pod 状态是否 ready， 如果是就停止
        // 计算下次索引
        // 构造 请求
        // 收到response之后， 得到时间，记录时间到 db， 且 递归调用

        // 1. 检查pod 状态
        String podIp = request.getIp();
        if (podStatusCheckService.isPodReady(podIp)) {
            // 已就绪，就停止
            LOGGER.info("pod {} is ready, stop", podIp);
            return;
        }

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

        URI uri = uriList.get(index);

        requester.uri(uri)
                .retrieve()
                .toBodilessEntity()
                .elapsed()
                .doOnNext(tuple -> saveCost2Db(uri, podIp, tuple.getT1(), requestId))                                         // 耗时
                .map(Tuple2::getT2)
                .subscribe( k -> sendOneByOne(request, uriList, nextIndex, requestId))                                 // 递归调用， one by one
        ;
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

    private void saveCost2Db(final URI uri, final String podIp, final Long cost, final ObjectId requestId) {

        LOGGER.info("saveCost2Db, uir {}, podIp {}, cost {}", uri, podIp, cost);

        HttpStatusDocument httpStatusDocument = new HttpStatusDocument();

        httpStatusRepository.save(genRequestDoc(podIp, cost, requestId))
                .subscribe();
    }

    private RequestDocument genRequestDoc(final WarmUpRequest request, final ObjectId requestId) {
        RequestDocument requestDocument = new RequestDocument();

        requestDocument.set_id(requestId.toHexString());

        requestDocument.setIp(request.getIp());
        requestDocument.setPort(request.getPort());
        requestDocument.setMethod(request.getMethod());
        requestDocument.setPodName(request.getPodName());

        requestDocument.setCreateTime(new Date());

        requestDocument.setQueryParamName(request.getQueryParamName());
        requestDocument.setParamList(request.getParamList());

        Map<String, String> headers = request.getHeaders();
        if (MapUtils.isNotEmpty(headers)) {
            requestDocument.setHeaders(headers.toString());
        }

        return requestDocument;
    }

    private HttpStatusDocument genRequestDoc(final String podIp, final Long cost, final ObjectId requestId) {
        HttpStatusDocument httpStatusDocument = new HttpStatusDocument();

        httpStatusDocument.setRequestId(requestId.toHexString());
        httpStatusDocument.setRequestObjectId(requestId);
        httpStatusDocument.setCost(null == cost ? 0 : (int)(long) cost);

        httpStatusDocument.setCreateTime(new Date());

        return httpStatusDocument;
    }
}
