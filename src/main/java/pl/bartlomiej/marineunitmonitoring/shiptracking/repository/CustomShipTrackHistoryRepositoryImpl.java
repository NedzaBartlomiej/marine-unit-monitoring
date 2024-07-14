package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.READING_TIME;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.from;

@Repository
public class CustomShipTrackHistoryRepositoryImpl implements CustomShipTrackHistoryRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomShipTrackHistoryRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<ShipTrack> findByMmsiInAndReadingTimeBetween(List<String> mmsis, LocalDateTime from, LocalDateTime to) {
        Query q = new Query().addCriteria(
                Criteria
                        .where(MMSI.fieldName).in(mmsis)
                        .and(READING_TIME.fieldName).gte(from).lte(to)
        );
        return reactiveMongoTemplate.find(q, ShipTrack.class);
    }

    @Override
    public Mono<ShipTrack> getLatest(String mmsi) {
        Query q = new Query();
        q.addCriteria(where(MMSI.fieldName).is(mmsi));
        q.with(by(DESC, READING_TIME.fieldName));
        q.limit(1);

        return from(
                reactiveMongoTemplate.find(q, ShipTrack.class)
                        .switchIfEmpty(error(NotFoundException::new))
        );
    }
}