package abhinav.projects.vouchermanager.voucher;

import jakarta.validation.constraints.Size;

public record ForceExpireRequest(@Size(max = 255) String reason) {
}
