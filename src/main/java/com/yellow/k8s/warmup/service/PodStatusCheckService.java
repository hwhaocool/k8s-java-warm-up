package com.yellow.k8s.warmup.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yellow.k8s.warmup.model.ContainerStatus;
import com.yellow.k8s.warmup.model.PodEvent;
import com.yellow.k8s.warmup.model.PodInfo;
import com.yellow.k8s.warmup.model.WarmUpInfo;
import com.yellow.k8s.warmup.utils.Null;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    private Cache<String, WarmUpInfo> cache;

    public void afterPropertiesSet() throws Exception {

        initCache();

        watch();
    }

    public boolean isPodReady(String ip) {

        WarmUpInfo ifPresent = cache.getIfPresent(ip);

        // 存在且为 true， 才 返回 true
        if (null == ifPresent) {

            //不存在的时候，说明是第一次进来，此时也就是第一次预热，设置一下时间戳
            cache.put(ip, new WarmUpInfo(System.currentTimeMillis()));

            LOGGER.info("isPodReady, first_check add_2_cache, ip {}", ip);

            return false;
        }

        boolean ready = ifPresent.isReady();

        if (ready) {
            long gap = System.currentTimeMillis() - ifPresent.getStartTime();

            String seconds = String.format("%.2f", (double) gap / 1000);
            LOGGER.info("pod {} have been warm up {} ms,  {} s", ip, gap, seconds);
        } else {
            // 不 ready 的时候， 看下
        }

        return ready;
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
                .accept(MediaType.ALL)
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

        try {
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
        } catch (Exception e) {
            LOGGER.error("occur exception, ", e);
        }

    }

    private void findAddEvent(PodInfo podInfo) {
        // 添加，默认状态为 false
        String podIP = podInfo.getStatus().getPodIP();

        if (StringUtils.isBlank(podIP)) {
            // 真正在创建pod的时候， 是没有ip的
            return;
        }

        String name = Null.of(() -> podInfo.getStatus().getContainerStatuses().get(0).getName());
        LOGGER.info("findAddEvent name {}, ip {}", name, podIP);
    }

    private void findUpdateEvent(PodInfo podInfo) {

        String podIP = Null.of(() -> podInfo.getStatus().getPodIP());
        if (StringUtils.isBlank(podIP)) {
            return;
        }

        List<ContainerStatus> containerStatuses = podInfo.getStatus().getContainerStatuses();
        if (CollectionUtils.isEmpty(containerStatuses)) {
            return;
        }

        WarmUpInfo ifPresent = cache.getIfPresent(podIP);

        if (null == ifPresent) {
            ifPresent = new WarmUpInfo();
        } else {
            ifPresent.setEventUpdateTime(System.currentTimeMillis());
        }

        boolean ready = containerStatuses.get(0).isReady();
        if (ready) {
            // 已就绪，状态更新为 true

            ifPresent.setReady(true);

            String name = Null.of(() -> containerStatuses.get(0).getName());
            LOGGER.info("findUpdateEvent, pod_is_ready, name {}, ip {}", name, podIP);
        }

        cache.put(podIP, ifPresent);
    }

    private void findDelEvent(PodInfo podInfo) {
        //删除
        String podIP = podInfo.getStatus().getPodIP();

        if (StringUtils.isBlank(podIP)) {
            // pod 被删除之后， 是没有ip的
            return;
        }

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
