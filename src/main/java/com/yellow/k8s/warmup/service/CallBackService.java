package com.yellow.k8s.warmup.service;

import com.yellow.k8s.warmup.contant.WarmUpConstants;
import com.yellow.k8s.warmup.dao.HttpStatusRepository;
import com.yellow.k8s.warmup.dao.RequestRepository;
import com.yellow.k8s.warmup.dbdoc.HttpStatusDocument;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
import com.yellow.k8s.warmup.dbdoc.SingleRequestParam;
import com.yellow.k8s.warmup.vo.BaseRequest;
import com.yellow.k8s.warmup.vo.WarmUpBatchRequest;
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
    @Qualifier("restClient")
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
        Mono<String> result = Mono.just(requestId.toHexString());

        // 2. 开关， 环境变量： warm-up-single: on/off, 可以关闭，以便对比效果
        if (! isSingleTurnOn()) {
            LOGGER.info("CallBackService single trun off, will not send request {}", request);
            return result;
        }

        // 3. 生成 uri 列表
        final List<URI> uriList = genUri(request);

        // 4. 保存请求信息
        requestRepository.save(genRequestDoc(request, requestId))
            .subscribe();

        // 5. 发送
        sendOneByOne(request, uriList, 0, requestId);

        return result;
    }

    public Mono<String> multi(WarmUpBatchRequest request) {
        // 1. 生成 请求id
        final ObjectId requestId = ObjectId.get();

        Mono<String> result = Mono.just(requestId.toHexString());

        // 2. 开关， 环境变量： warm-up-multi: on/off, 可以关闭，以便对比效果
        if (! isMultiTurnOn()) {
            LOGGER.info("CallBackService multi trun off, will not send request {}", request);
            return result;
        }

        // 3. 生成 uri 列表
        final List<URI> uriList = genUri(request);

        // 4. 保存请求信息
        requestRepository.save(genRequestDoc(request, requestId))
                .subscribe();

        // 5. 发送, url 列表长度为队列数量，每一列都是 一个接一个
        List<List<URI>> collect = uriList.stream()
                .map(List::of)
                .collect(Collectors.toList());

        collect.forEach( k -> sendOneByOne(request, k, 0, requestId));

        return result;
    }



    private void sendOneByOne(final BaseRequest request, final List<URI> uriList, final int index, final ObjectId requestId) {

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

        LOGGER.info("sendOneByOne podName {}, podIp {}, current index {}", request.getPodName(), podIp, index);

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
                .exchange()
                .elapsed()
                .doOnNext(tuple -> saveCost2Db(uri, podIp, tuple.getT1(), requestId, index))                                         // 耗时
                .map(Tuple2::getT2)
                .subscribe( clientResponse -> {

                    if (clientResponse.statusCode().is5xxServerError() || clientResponse.statusCode().is4xxClientError()) {
                        LOGGER.info("uri {}, status is {}", uri, clientResponse.statusCode().value());
                    }

                    clientResponse
                            .toBodilessEntity()
//                            .releaseBody()
                            .subscribe(k -> sendOneByOne(request, uriList, nextIndex, requestId));              // 递归调用， one by one


                })
        ;
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

    /**
     * 生成 uri 列表
     * @param request
     * @author YellowTail
     * @since 2020-06-16
     */
    private List<URI> genUri(WarmUpBatchRequest request) {

        List<String> uriList = request.getUriList();
        if (CollectionUtils.isEmpty(uriList)) {
            return List.of();
        }

        return uriList.stream()
                .map(k ->
                        new DefaultUriBuilderFactory()
                                .builder()
                                .scheme("http")
                                .host(request.getIp())
                                .port(request.getPort())
                                .path(k)
                                .build()
                ).collect(Collectors.toList());
    }

    private void saveCost2Db(final URI uri, final String podIp, final Long cost, final ObjectId requestId, final int index) {

        LOGGER.info("saveCost2Db, uir {}, podIp {}, index {}, cost {}", uri, podIp, index, cost);

//        httpStatusRepository.save(genRequestDoc(uri, cost, requestId))
//                .subscribe();
    }

    private RequestDocument genRequestDoc(final WarmUpRequest request, final ObjectId requestId) {
        String type = WarmUpConstants.RequestType.Single.getValue();

        SingleRequestParam singleRequestParam = new SingleRequestParam();
        singleRequestParam.setQueryParamName(request.getQueryParamName());
        singleRequestParam.setParamList(request.getParamList());

        RequestDocument requestDocument = genCommonDocument(request, requestId);

        // 不一样的字段
        requestDocument.setUri(request.getUri());
        requestDocument.setType(type);
        requestDocument.setSingleRequestParam(singleRequestParam);

        return requestDocument;
    }

    private RequestDocument genRequestDoc(final WarmUpBatchRequest request, final ObjectId requestId) {
        String type = WarmUpConstants.RequestType.Multi.getValue();

        RequestDocument requestDocument = genCommonDocument(request, requestId);

        // 不一样的字段
        requestDocument.setUriList(request.getUriList());
        requestDocument.setType(type);

        return requestDocument;
    }

    private RequestDocument genCommonDocument(final BaseRequest request, final ObjectId requestId) {
        RequestDocument requestDocument = new RequestDocument();

        requestDocument.set_id(requestId.toHexString());

        requestDocument.setIp(request.getIp());
        requestDocument.setPort(request.getPort());
        requestDocument.setMethod(request.getMethod());
        requestDocument.setPodName(request.getPodName());

        requestDocument.setCreateTime(new Date());

        Map<String, String> headers = request.getHeaders();
        if (MapUtils.isNotEmpty(headers)) {
            requestDocument.setHeaders(headers.toString());
        }

        return requestDocument;
    }

    private HttpStatusDocument genRequestDoc(final URI uri, final Long cost, final ObjectId requestId) {
        HttpStatusDocument httpStatusDocument = new HttpStatusDocument();

        httpStatusDocument.setRequestId(requestId.toHexString());
        httpStatusDocument.setRequestObjectId(requestId);
        httpStatusDocument.setCost(null == cost ? 0 : (int)(long) cost);

        httpStatusDocument.setCreateTime(new Date());

        httpStatusDocument.setDesc(uri.getPath());

        return httpStatusDocument;
    }

    /**
     * single 是否打开
     * <br>默认打开， 默认返回 true
     * <br>配置 on， 返回 true
     * <br>配置 off， 返回 false
     * @param
     * @author YellowTail
     * @since 2020-09-26
     */
    private boolean isSingleTurnOn() {
        String value = System.getenv().get(WarmUpConstants.Flag.Single.getValue());

        if (StringUtils.isBlank(value)) {
            return true;
        }

        return WarmUpConstants.FlagValue.On.getValue().equals(value);
    }

    /**
     * multi 是否打开
     * <br>默认打开， 默认返回 true
     * <br>配置 on， 返回 true
     * <br>配置 off， 返回 false
     * @param
     * @author YellowTail
     * @since 2020-09-26
     */
    private boolean isMultiTurnOn() {
        String value = System.getenv().get(WarmUpConstants.Flag.Multi.getValue());

        if (StringUtils.isBlank(value)) {
            return true;
        }

        return WarmUpConstants.FlagValue.On.getValue().equals(value);
    }
}
