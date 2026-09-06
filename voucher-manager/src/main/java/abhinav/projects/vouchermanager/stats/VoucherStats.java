package abhinav.projects.vouchermanager.stats;

public record VoucherStats(
        long totalVouchers,
        long activeVouchers,
        long draftVouchers,
        long pausedVouchers,
        long expiredVouchers,
        long exhaustedVouchers,
        long totalUsers,
        long totalRedemptions
) {
}
