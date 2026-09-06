package abhinav.projects.vouchermanager.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("org")
public record Org(
        @Id UUID id,
        String name,
        String email,
        String passwordHash
) {
}
