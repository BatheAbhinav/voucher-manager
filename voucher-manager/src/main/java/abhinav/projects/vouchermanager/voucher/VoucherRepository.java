package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends ListCrudRepository<Voucher, UUID> {

    Optional<Voucher> findByCode(String code);

    @Query("SELECT * FROM voucher WHERE code = :code FOR UPDATE")
    Optional<Voucher> findByCodeForUpdate(@Param("code") String code);

    List<Voucher> findByOrgId(UUID orgId);
}
