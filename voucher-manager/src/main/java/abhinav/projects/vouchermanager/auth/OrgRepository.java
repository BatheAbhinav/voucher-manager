package abhinav.projects.vouchermanager.auth;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrgRepository extends ListCrudRepository<Org, UUID> {

    Optional<Org> findByEmail(String email);
}
