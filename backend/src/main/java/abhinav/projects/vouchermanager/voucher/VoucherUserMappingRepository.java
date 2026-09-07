package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherUserMappingRepository extends ListCrudRepository<VoucherUserMapping, UUID> {

    Optional<VoucherUserMapping> findByVoucherIdAndUserId(UUID voucherId, UUID userId);

    long countByVoucherIdAndStatus(UUID voucherId, MappingStatus status);

    List<VoucherUserMapping> findByVoucherIdAndStatus(UUID voucherId, MappingStatus status);

    List<VoucherUserMapping> findByVoucherId(UUID voucherId);
}
