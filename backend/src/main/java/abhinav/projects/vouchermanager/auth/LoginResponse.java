package abhinav.projects.vouchermanager.auth;

import java.util.UUID;

public record LoginResponse(String token, Role role, UUID id, String name) {
}
