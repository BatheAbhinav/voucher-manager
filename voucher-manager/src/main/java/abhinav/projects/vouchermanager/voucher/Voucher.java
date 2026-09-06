package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Table("voucher")
public record Voucher(
        @Id UUID id,
        UUID orgId,
        String code,
        String title,
        VoucherStatus status,
        VoucherScope scope,
        Instant startsAt,
        Instant expiresAt,
        Integer maxRedemptions,
        Map<String, Object> attributes
) {
}
