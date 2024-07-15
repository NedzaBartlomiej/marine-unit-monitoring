package pl.bartlomiej.marineunitmonitoring.security.emailverification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "email_verifications") // todo refactor all entities (setters - only field which need to have setter, constructors)
public class EmailVerificationEntity {
    private String id;
    private String uid;

    public EmailVerificationEntity(String uid) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
    }
}
