package abhinav.projects.vouchermanager.stats;

import abhinav.projects.vouchermanager.voucher.VoucherHistoryEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ActivityEntry(
        UUID id,
        UUID voucherId,
        String voucherCode,
        UUID userId,
        String userEmail,
        VoucherHistoryEventType eventType,
        Map<String, Object> eventData,
        Instant createdAt
) {
}
