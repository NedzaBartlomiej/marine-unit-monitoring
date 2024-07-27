package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackConstants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.from;

@Repository
public class CustomShipTrackRepositoryImpl implements CustomShipTrackRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomShipTrackRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<ShipTrack> findByMmsiInAndReadingTimeBetween(List<String> mmsis, LocalDateTime from, LocalDateTime to) {
        Query q = new Query().addCriteria(
                Criteria
                        .where(ShipTrackConstants.MMSI).in(mmsis)
                        .and(ShipTrackConstants.READING_TIME).gte(from).lte(to)
        );
        return reactiveMongoTemplate.find(q, ShipTrack.class);
    }

    @Override
    public Mono<ShipTrack> getLatest(String mmsi) {
        Query q = new Query();
        q.addCriteria(where(ShipTrackConstants.MMSI).is(mmsi));
        q.with(by(DESC, ShipTrackConstants.READING_TIME));
        q.limit(1);

        return from(
                reactiveMongoTemplate.find(q, ShipTrack.class)
                        .switchIfEmpty(error(NotFoundException::new))
        );
    }
}