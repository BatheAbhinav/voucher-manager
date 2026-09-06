package abhinav.projects.vouchermanager.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("admin")
public record Admin(
        @Id UUID id,
        String email,
        String passwordHash
) {
}
