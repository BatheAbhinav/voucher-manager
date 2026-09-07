package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("voucher_user_mapping")
public record VoucherUserMapping(
        @Id UUID id,
        UUID voucherId,
        UUID userId,
        MappingStatus status,
        Instant assignedAt,
        Instant redeemedAt
) {
}
