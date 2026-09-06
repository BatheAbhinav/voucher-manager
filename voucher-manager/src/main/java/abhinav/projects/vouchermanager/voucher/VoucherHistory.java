package abhinav.projects.vouchermanager.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Table("voucher_history")
public record VoucherHistory(
        @Id UUID id,
        UUID voucherId,
        UUID userId,
        VoucherHistoryEventType eventType,
        Map<String, Object> eventData,
        Instant createdAt
) {
}
