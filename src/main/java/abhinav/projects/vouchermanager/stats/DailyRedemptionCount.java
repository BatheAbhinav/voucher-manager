package abhinav.projects.vouchermanager.stats;

import java.time.LocalDate;

public record DailyRedemptionCount(LocalDate day, long count) {
}
