package abhinav.projects.vouchermanager.voucher;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RedeemRequest(@NotNull UUID userId) {
}
