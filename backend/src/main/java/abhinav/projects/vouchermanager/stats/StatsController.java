package abhinav.projects.vouchermanager.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stats")
class StatsController {

    private final StatsService statsService;

    StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/vouchers")
    VoucherStats vouchers(@RequestParam(required = false) UUID orgId) {
        return statsService.stats(orgId);
    }

    @GetMapping("/vouchers/by-org")
    List<OrgVoucherStats> vouchersByOrg() {
        return statsService.statsByOrg();
    }

    @GetMapping("/activity")
    List<ActivityEntry> activity(@RequestParam(required = false) UUID orgId,
                                  @RequestParam(defaultValue = "20") int limit) {
        return statsService.activity(orgId, limit);
    }

    @GetMapping("/redemptions/daily")
    List<DailyRedemptionCount> dailyRedemptions(@RequestParam(required = false) UUID orgId,
                                                 @RequestParam(defaultValue = "30") int days) {
        return statsService.dailyRedemptions(orgId, days);
    }
}
