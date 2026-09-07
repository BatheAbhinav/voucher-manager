package abhinav.projects.vouchermanager.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record UserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 255) String name,
        UserStatus status,
        Map<String, Object> attributes,
        UUID orgId
) {
}
