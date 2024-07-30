package pl.bartlomiej.marineunitmonitoring.common.helper.repository;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

public interface CustomRepository {

    Query getIdValidQuery(String id);

    Mono<UpdateResult> updateOne(String id, String updateFieldName, Object updateValue, Class<?> entityClass);

    Mono<UpdateResult> updateMulti(String id, String updateFieldName, Object updateValue, Class<?> entityClass);
}