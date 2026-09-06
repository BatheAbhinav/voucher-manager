package abhinav.projects.vouchermanager.user;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends ListCrudRepository<User, UUID> {

    List<User> findByOrgId(UUID orgId);
}
