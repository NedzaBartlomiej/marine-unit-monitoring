package pl.bartlomiej.marineunitmonitoring.common.helper.repository;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;
import reactor.core.publisher.Mono;

@Repository
public class CustomMongoRepository implements CustomRepository {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomMongoRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Query getIdValidQuery(String id) {
        return new Query(Criteria.where(CommonFields.ID).is(id));
    }

    @Override
    public Mono<UpdateResult> updateOne(String id, String updateFieldName, Object updateValue, Class<?> entityClass) {
        return reactiveMongoTemplate.updateFirst(
                this.getIdValidQuery(id),
                this.getFieldUpdate(updateFieldName, updateValue),
                entityClass
        );
    }

    @Override
    public Mono<UpdateResult> updateMulti(String id, String updateFieldName, Object updateValue, Class<?> entityClass) {
        return reactiveMongoTemplate.updateMulti(
                this.getIdValidQuery(id),
                this.getFieldUpdate(updateFieldName, updateValue),
                entityClass
        );
    }

    private Update getFieldUpdate(String updateFieldName, Object updateValue) {
        return new Update().set(updateFieldName, updateValue);
    }
}
