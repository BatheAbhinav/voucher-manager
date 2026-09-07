package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VoucherHistoryRepository extends ListCrudRepository<VoucherHistory, UUID> {

    @Query("SELECT h.* FROM voucher_history h JOIN voucher v ON v.id = h.voucher_id "
            + "WHERE v.org_id = :orgId ORDER BY h.created_at DESC LIMIT :limit")
    List<VoucherHistory> findRecentByOrgId(@Param("orgId") UUID orgId, @Param("limit") int limit);

    @Query("SELECT * FROM voucher_history ORDER BY created_at DESC LIMIT :limit")
    List<VoucherHistory> findRecent(@Param("limit") int limit);
}
