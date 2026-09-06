import { Component, computed, effect, inject, signal } from '@angular/core';
import { StatsService } from '../../stats/stats.service';
import { ActivityEntry, DailyRedemptionCount, OrgVoucherStats, VoucherStats } from '../../stats/stats.model';
import { AuthService } from '../../auth/auth.service';
import { OrgContextService } from '../../orgs/org-context.service';
import { BarChart, BarChartItem } from '../../charts/bar-chart/bar-chart';
import { RedemptionTrendChart } from '../../stats/redemption-trend-chart/redemption-trend-chart';

@Component({
  imports: [BarChart, RedemptionTrendChart],
  selector: 'app-overview-page',
  styleUrl: './overview-page.css',
  templateUrl: './overview-page.html',
})
export class OverviewPage {
  private readonly statsService = inject(StatsService);
  private readonly authService = inject(AuthService);
  private readonly orgContext = inject(OrgContextService);

  readonly stats = signal<VoucherStats | null>(null);
  readonly activity = signal<ActivityEntry[]>([]);
  readonly byOrg = signal<OrgVoucherStats[]>([]);
  readonly dailyRedemptions = signal<DailyRedemptionCount[]>([]);
  readonly loading = signal(false);

  readonly isAdmin = computed(() => this.authService.role() === 'ADMIN');

  readonly statusBreakdownItems = computed<BarChartItem[]>(() => {
    const s = this.stats();
    if (!s) {
      return [];
    }
    return [
      { label: 'Active', value: s.activeVouchers },
      { label: 'Draft', value: s.draftVouchers },
      { label: 'Paused', value: s.pausedVouchers },
      { label: 'Expired', value: s.expiredVouchers },
      { label: 'Exhausted', value: s.exhaustedVouchers },
    ];
  });

  readonly orgBreakdownItems = computed<BarChartItem[]>(() =>
    this.byOrg().map((org) => ({ label: org.orgName, value: org.stats.totalVouchers })),
  );

  constructor() {
    effect(() => this.reload());
  }

  activityLabel(entry: ActivityEntry): string {
    const who = entry.userEmail ?? 'a user';
    switch (entry.eventType) {
      case 'CREATED':
        return `Voucher ${entry.voucherCode} created`;
      case 'ASSIGNED':
        return `${entry.voucherCode} linked to ${who}`;
      case 'REDEEMED':
        return `${entry.voucherCode} redeemed by ${who}`;
      case 'EXPIRED':
        return `${entry.voucherCode} expired`;
      case 'EXHAUSTED':
        return `${entry.voucherCode} exhausted`;
      case 'REVOKED':
        return `${entry.voucherCode} assignment revoked for ${who}`;
      case 'ACTIVATED':
        return `${entry.voucherCode} activated`;
      default:
        return `${entry.voucherCode} updated`;
    }
  }

  private reload(): void {
    const admin = this.isAdmin();
    const selectedOrgId = this.orgContext.selectedOrgId();
    const orgId = admin ? (selectedOrgId ?? undefined) : undefined;

    this.loading.set(true);
    this.statsService.vouchers(orgId).subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.statsService.activity(orgId, 15).subscribe((activity) => this.activity.set(activity));
    this.statsService.dailyRedemptions(orgId, 30).subscribe((rows) => this.dailyRedemptions.set(rows));

    if (admin && !selectedOrgId) {
      this.statsService.vouchersByOrg().subscribe((byOrg) => this.byOrg.set(byOrg));
    } else {
      this.byOrg.set([]);
    }
  }
}
