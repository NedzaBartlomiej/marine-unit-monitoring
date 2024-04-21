package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.READING_TIME;

@Repository
@RequiredArgsConstructor
public class CustomShipTrackHistoryRepositoryImpl implements CustomShipTrackHistoryRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Flux<ShipTrack> findByMmsiInAndReadingTimeBetween(List<Long> mmsis, LocalDateTime from, LocalDateTime to) {
        Query q = new Query().addCriteria(
                Criteria
                        .where(MMSI.fieldName).in(mmsis)
                        .and(READING_TIME.fieldName).gte(from).lte(to)
        );
        return reactiveMongoTemplate.find(q, ShipTrack.class);
    }
}
