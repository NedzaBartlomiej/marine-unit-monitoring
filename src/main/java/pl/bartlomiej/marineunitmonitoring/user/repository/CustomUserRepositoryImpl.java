package pl.bartlomiej.marineunitmonitoring.user.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final MongoTemplate mongoTemplate;

    @Override // dodawanie dziala tylko jeszcze codec exception
    public TrackedShip pushTrackedShip(String id, TrackedShip trackedShip) {
        mongoTemplate.getDb().getCollection("users")
                .updateOne(
                        new Document("_id", new ObjectId(id)),
                        new Document(
                                "$push",
                                new Document("trackedShips", trackedShip) //todo codecexception
                        )
                );
        return trackedShip;
    }
}
