package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "jwt_blacklist")
public class JWTEntity {
    private String id;
    private Date expiration;
}
