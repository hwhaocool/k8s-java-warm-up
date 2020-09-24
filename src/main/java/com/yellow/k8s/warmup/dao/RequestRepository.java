package com.yellow.k8s.warmup.dao;

import com.yellow.k8s.warmup.dbdoc.PodTopDocument;
import com.yellow.k8s.warmup.dbdoc.RequestDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

/**
 * @author YellowTail
 * @since 2020-09-22
// */
@Repository
public interface RequestRepository  extends ReactiveCrudRepository<RequestDocument, String> {

    // PageRequest
//    @Query(sort="{_id: -1}")
//    Flux<RequestDocument> getList(Pageable pageable);

//    Mono<RequestDocument> findById(String id);


    @Query(value = "{ name: ?0, _id: {$gt: ?1}}", sort="{_id: -1}")
    Flux<RequestDocument> findByName(String name, ObjectId minId);

    @Query("{ 'name': ?0, 'imageUrl': ?1}")
    Mono<RequestDocument> findByNameAndImageUrl(String name, String imageUrl);

    @Query(value = "{'_id': {$lt: ?0} }", delete = true)
    Mono<Void> deleteBeforeId(ObjectId id);

    @DeleteQuery(value = "{ createTime: ?0}")
    Mono<Void> deleteByCreateTime(Date creteTime);
}
