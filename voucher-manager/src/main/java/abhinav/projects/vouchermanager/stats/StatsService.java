package abhinav.projects.vouchermanager.stats;

import abhinav.projects.vouchermanager.auth.CurrentPrincipal;
import abhinav.projects.vouchermanager.auth.Org;
import abhinav.projects.vouchermanager.auth.OrgRepository;
import abhinav.projects.vouchermanager.user.User;
import abhinav.projects.vouchermanager.user.UserRepository;
import abhinav.projects.vouchermanager.voucher.MappingStatus;
import abhinav.projects.vouchermanager.voucher.Voucher;
import abhinav.projects.vouchermanager.voucher.VoucherHistory;
import abhinav.projects.vouchermanager.voucher.VoucherHistoryRepository;
import abhinav.projects.vouchermanager.voucher.VoucherRepository;
import abhinav.projects.vouchermanager.voucher.VoucherStatus;
import abhinav.projects.vouchermanager.voucher.VoucherUserMappingRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
class StatsService {

    private static final String DAILY_REDEMPTIONS_ALL = """
            WITH days AS (
              SELECT generate_series(CURRENT_DATE - (:days - 1), CURRENT_DATE, INTERVAL '1 day')::date AS day
            ),
            redemptions AS (
              SELECT h.created_at::date AS day, COUNT(*) AS cnt
              FROM voucher_history h
              WHERE h.event_type = 'REDEEMED'
              GROUP BY h.created_at::date
            )
            SELECT d.day AS day, COALESCE(r.cnt, 0) AS cnt
            FROM days d
            LEFT JOIN redemptions r ON r.day = d.day
            ORDER BY d.day
            """;

    private static final String DAILY_REDEMPTIONS_BY_ORG = """
            WITH days AS (
              SELECT generate_series(CURRENT_DATE - (:days - 1), CURRENT_DATE, INTERVAL '1 day')::date AS day
            ),
            redemptions AS (
              SELECT h.created_at::date AS day, COUNT(*) AS cnt
              FROM voucher_history h
              JOIN voucher v ON v.id = h.voucher_id
              WHERE h.event_type = 'REDEEMED' AND v.org_id = :orgId
              GROUP BY h.created_at::date
            )
            SELECT d.day AS day, COALESCE(r.cnt, 0) AS cnt
            FROM days d
            LEFT JOIN redemptions r ON r.day = d.day
            ORDER BY d.day
            """;

    private final VoucherRepository voucherRepository;
    private final VoucherUserMappingRepository mappingRepository;
    private final VoucherHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;
    private final CurrentPrincipal currentPrincipal;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    StatsService(VoucherRepository voucherRepository,
                 VoucherUserMappingRepository mappingRepository,
                 VoucherHistoryRepository historyRepository,
                 UserRepository userRepository,
                 OrgRepository orgRepository,
                 CurrentPrincipal currentPrincipal,
                 NamedParameterJdbcTemplate jdbcTemplate) {
        this.voucherRepository = voucherRepository;
        this.mappingRepository = mappingRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.orgRepository = orgRepository;
        this.currentPrincipal = currentPrincipal;
        this.jdbcTemplate = jdbcTemplate;
    }

    List<DailyRedemptionCount> dailyRedemptions(UUID orgId, int days) {
        UUID scopedOrgId = currentPrincipal.resolveOrgIdOrNull(orgId);
        int clampedDays = Math.min(Math.max(days, 1), 90);

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("days", clampedDays);
        String sql;
        if (scopedOrgId != null) {
            params.addValue("orgId", scopedOrgId);
            sql = DAILY_REDEMPTIONS_BY_ORG;
        } else {
            sql = DAILY_REDEMPTIONS_ALL;
        }

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                new DailyRedemptionCount(rs.getDate("day").toLocalDate(), rs.getLong("cnt")));
    }

    VoucherStats stats(UUID orgId) {
        UUID scopedOrgId = currentPrincipal.resolveOrgIdOrNull(orgId);
        List<Voucher> vouchers = scopedOrgId != null ? voucherRepository.findByOrgId(scopedOrgId) : voucherRepository.findAll();
        List<User> users = scopedOrgId != null ? userRepository.findByOrgId(scopedOrgId) : userRepository.findAll();
        return buildStats(vouchers, users);
    }

    List<OrgVoucherStats> statsByOrg() {
        currentPrincipal.requireAdmin();
        List<Org> orgs = orgRepository.findAll();
        return orgs.stream()
                .map(org -> {
                    List<Voucher> vouchers = voucherRepository.findByOrgId(org.id());
                    List<User> users = userRepository.findByOrgId(org.id());
                    return new OrgVoucherStats(org.id(), org.name(), buildStats(vouchers, users));
                })
                .toList();
    }

    List<ActivityEntry> activity(UUID orgId, int limit) {
        UUID scopedOrgId = currentPrincipal.resolveOrgIdOrNull(orgId);
        List<VoucherHistory> entries = scopedOrgId != null
                ? historyRepository.findRecentByOrgId(scopedOrgId, limit)
                : historyRepository.findRecent(limit);

        Set<UUID> voucherIds = entries.stream().map(VoucherHistory::voucherId).collect(Collectors.toSet());
        Set<UUID> userIds = entries.stream().map(VoucherHistory::userId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, String> voucherCodes = StreamSupport.stream(voucherRepository.findAllById(voucherIds).spliterator(), false)
                .collect(Collectors.toMap(Voucher::id, Voucher::code));
        Map<UUID, String> userEmails = StreamSupport.stream(userRepository.findAllById(userIds).spliterator(), false)
                .collect(Collectors.toMap(User::id, User::email));

        return entries.stream()
                .map(h -> new ActivityEntry(
                        h.id(), h.voucherId(), voucherCodes.get(h.voucherId()),
                        h.userId(), h.userId() != null ? userEmails.get(h.userId()) : null,
                        h.eventType(), h.eventData(), h.createdAt()
                ))
                .toList();
    }

    private VoucherStats buildStats(List<Voucher> vouchers, List<User> users) {
        Map<VoucherStatus, Long> byStatus = vouchers.stream()
                .collect(Collectors.groupingBy(Voucher::status, Collectors.counting()));
        long totalRedemptions = vouchers.stream()
                .mapToLong(v -> mappingRepository.countByVoucherIdAndStatus(v.id(), MappingStatus.REDEEMED))
                .sum();
        return new VoucherStats(
                vouchers.size(),
                byStatus.getOrDefault(VoucherStatus.ACTIVE, 0L),
                byStatus.getOrDefault(VoucherStatus.DRAFT, 0L),
                byStatus.getOrDefault(VoucherStatus.PAUSED, 0L),
                byStatus.getOrDefault(VoucherStatus.EXPIRED, 0L),
                byStatus.getOrDefault(VoucherStatus.EXHAUSTED, 0L),
                users.size(),
                totalRedemptions
        );
    }
}
