package abhinav.projects.vouchermanager.voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record VoucherRequest(
        @NotBlank @Size(max = 64) String code,
        @Size(max = 255) String title,
        VoucherStatus status,
        VoucherScope scope,
        Instant startsAt,
        Instant expiresAt,
        @Positive Integer maxRedemptions,
        Map<String, Object> attributes,
        UUID orgId,
        List<UUID> assignedUserIds
) {
}
