package abhinav.projects.vouchermanager.auth;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends ListCrudRepository<Admin, UUID> {

    Optional<Admin> findByEmail(String email);
}
