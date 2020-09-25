package com.yellow.k8s.warmup.service;

import com.github.abel533.echarts.Option;
import com.yellow.k8s.warmup.dao.HttpStatusRepository;
import com.yellow.k8s.warmup.dao.RequestRepository;
import com.yellow.k8s.warmup.dbdoc.HttpStatusDocument;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
import com.yellow.k8s.warmup.utils.Domain2VoUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

/**
 * @author YellowTail
 * @since 2020-09-22
 */
@Service
public class MonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private HttpStatusRepository httpStatusRepository;


    /**
     * 查询列表， 可以翻页查询
     * @param page
     * @author YellowTail
     * @since 2020-09-22
     */
    public Flux<RequestDocument> getList(Integer page) {

        LOGGER.info("MonitorService getList page {}", page);

        // page 从 0 开始

        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "_id"));

//        Page<RequestDocument> requestDocuments = requestRepository.findAll(pageRequest);
//
//        Stream<RequestDocument> requestDocumentStream = requestDocuments.get();
//        Flux.str

        Flux<RequestDocument> list = requestRepository.getList(null);

        return list.limitRequest(20);

    }

    /**
     * 得到一个记录的 echarts
     * @param _id
     * @author YellowTail
     * @since 2020-09-22
     */
    public Mono<Option> getOne(String _id) {

        LOGGER.info("MonitorService getOne _id {}", _id);

        if (! ObjectId.isValid(_id)) {
            throw new RuntimeException("_id is invalid");
        }

        Mono<RequestDocument> documentMono = requestRepository.findById(_id);

        Mono<String> stringMono = documentMono.map(RequestDocument::get_id);


        Flux<HttpStatusDocument> list = httpStatusRepository.getList(stringMono);

        return Domain2VoUtils.onePodTopStack(documentMono, list);
    }
}
