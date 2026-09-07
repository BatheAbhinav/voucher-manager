package abhinav.projects.vouchermanager.stats;

import java.util.UUID;

public record OrgVoucherStats(UUID orgId, String orgName, VoucherStats stats) {
}
