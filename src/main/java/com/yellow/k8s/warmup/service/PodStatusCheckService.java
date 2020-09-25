package com.yellow.k8s.warmup.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yellow.k8s.warmup.model.ContainerStatus;
import com.yellow.k8s.warmup.model.PodEvent;
import com.yellow.k8s.warmup.model.PodInfo;
import com.yellow.k8s.warmup.utils.Null;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * pod 状态检查service， 当 ready 之后就停止测试
 *
 * @author YellowTail
 * @since 2020-09-23
 */
@Service
public class PodStatusCheckService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodStatusCheckService.class);

    @Autowired
    @Qualifier("k8sClient")
    private WebClient webClient;

    private Cache<String, Boolean> cache;

    public void afterPropertiesSet() throws Exception {

        initCache();

        watch();
    }

    public boolean isPodReady(String ip) {

        Boolean ifPresent = cache.getIfPresent(ip);

        // 存在且为 true， 才 返回 true
        return Boolean.TRUE.equals(ifPresent);
    }

    private void initCache() {
        cache = Caffeine.newBuilder()
                .initialCapacity(20)                                         //初始大小
                .expireAfterWrite(10, TimeUnit.MINUTES)              //过期时间, 写入10分钟后过期
                .build();
    }

    private void watch() {

        // 1. watch
        // 2. 全部添加到内存缓存中， 使用 caffeine
        // 3. 有使用者来请求的时候，就可以到内存中读取

        LOGGER.info("watch start");

        // https://kubernetes.cn/docs/reference/generated/kubernetes-api/v1.19/#list-all-namespaces-pod-v1-core

        // /api/v1/pods watch 所有 命名空间的pod， 排除一下已知的，如 kube-system istio等

        Flux<PodEvent> podEventFlux = webClient.get()
                .uri("/api/v1/pods?watch=true")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToFlux(PodEvent.class);

        podEventFlux
                .doOnError(this::onError)
                .subscribe(this::onEvent);

    }

    /**
     * <br> api-server 返回 200 时的处理逻辑
     *
     * @param k
     * @author YellowTail
     * @since 2019-11-19
     */
    private void onEvent(PodEvent k) {
        LOGGER.info("onEvent {}", k);

        //ADDED/MODIFIED/DELETED

        String type = k.getType();
        PodInfo podInfo = k.getObject();
        switch (type) {
            case "ADDED":
                findAddEvent(podInfo);
                return;
            case "MODIFIED":
                findUpdateEvent(podInfo);
                return;
            case "DELETED":
                findDelEvent(podInfo);
                return;
            default:
                LOGGER.error("bad type {}", type);
        }
    }

    private void findAddEvent(PodInfo podInfo) {
        // 添加，默认状态为 false
        String podIP = podInfo.getStatus().getPodIP();

        cache.put(podIP, false);

        String name = Null.of(() -> podInfo.getStatus().getContainerStatuses().get(0).getName());
        LOGGER.info("findAddEvent name {}, ip {}", name, podIP);
    }

    private void findUpdateEvent(PodInfo podInfo) {
        List<ContainerStatus> containerStatuses = podInfo.getStatus().getContainerStatuses();
        if (CollectionUtils.isEmpty(containerStatuses)) {
            return;
        }

        boolean ready = containerStatuses.get(0).isReady();
        if (ready) {
            // 已就绪，状态更新为 true
            String podIP = podInfo.getStatus().getPodIP();
            cache.put(podIP, true);

            String name = Null.of(() -> containerStatuses.get(0).getName());
            LOGGER.info("findUpdateEvent name {}, ip {}", name, podIP);
        }

    }

    private void findDelEvent(PodInfo podInfo) {
        //删除
        String podIP = podInfo.getStatus().getPodIP();
        cache.invalidate(podIP);

        String name = Null.of(() -> podInfo.getStatus().getContainerStatuses().get(0).getName());
        LOGGER.info("findDelEvent name {}, ip {}", name, podIP);
    }


    /**
     * <br>抛异常时的处理逻辑
     *
     * @param k
     * @author YellowTail
     * @since 2019-11-19
     */
    private void onError(Throwable k) {
        LOGGER.error("request k8s api-server occur exception, ", k);
    }



}
