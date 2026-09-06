package abhinav.projects.vouchermanager.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;
import java.util.UUID;

@Table("app_user")
public record User(
        @Id UUID id,
        UUID orgId,
        String email,
        String name,
        UserStatus status,
        Map<String, Object> attributes
) {
}
