package abhinav.projects.vouchermanager.user;

import abhinav.projects.vouchermanager.auth.CurrentPrincipal;
import abhinav.projects.vouchermanager.error.ConflictException;
import abhinav.projects.vouchermanager.error.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class UserService {

    private final UserRepository userRepository;
    private final CurrentPrincipal currentPrincipal;

    UserService(UserRepository userRepository, CurrentPrincipal currentPrincipal) {
        this.userRepository = userRepository;
        this.currentPrincipal = currentPrincipal;
    }

    User create(UserRequest request) {
        UUID orgId = currentPrincipal.resolveOrgId(request.orgId());
        UserStatus status = request.status() != null ? request.status() : UserStatus.ACTIVE;
        Map<String, Object> attributes = request.attributes() != null ? request.attributes() : Map.of();
        User user = new User(null, orgId, request.email(), request.name(), status, attributes);
        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("User already exists with email: " + request.email());
        }
    }

    User get(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        currentPrincipal.requireAccess(user.orgId());
        return user;
    }

    List<User> list(UUID orgId) {
        UUID scopedOrgId = currentPrincipal.resolveOrgIdOrNull(orgId);
        return scopedOrgId != null ? userRepository.findByOrgId(scopedOrgId) : userRepository.findAll();
    }
}
