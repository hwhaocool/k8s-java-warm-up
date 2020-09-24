package com.yellow.k8s.warmup.dao;

import com.yellow.k8s.warmup.dbdoc.HttpStatusDocument;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Repository
public interface HttpStatusRepository extends ReactiveCrudRepository<HttpStatusDocument, String> {

    @Query("{'requestId': ?0}")
    Flux<HttpStatusDocument> getList(Publisher<String> requestId);

    @Query(value = "{'_id': {$lt: ?0} }", delete = true)
    Mono<Void> deleteBeforeId(ObjectId id);

    @DeleteQuery(value = "{ createTime: ?0}")
    Mono<Void> deleteByCreateTime(Date creteTime);


}
