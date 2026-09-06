import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActivityEntry, DailyRedemptionCount, OrgVoucherStats, VoucherStats } from './stats.model';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly http = inject(HttpClient);

  vouchers(orgId?: string): Observable<VoucherStats> {
    const params = orgId ? new HttpParams().set('orgId', orgId) : undefined;
    return this.http.get<VoucherStats>('/stats/vouchers', { params });
  }

  vouchersByOrg(): Observable<OrgVoucherStats[]> {
    return this.http.get<OrgVoucherStats[]>('/stats/vouchers/by-org');
  }

  activity(orgId?: string, limit = 20): Observable<ActivityEntry[]> {
    let params = new HttpParams().set('limit', limit);
    if (orgId) {
      params = params.set('orgId', orgId);
    }
    return this.http.get<ActivityEntry[]>('/stats/activity', { params });
  }

  dailyRedemptions(orgId?: string, days = 30): Observable<DailyRedemptionCount[]> {
    let params = new HttpParams().set('days', days);
    if (orgId) {
      params = params.set('orgId', orgId);
    }
    return this.http.get<DailyRedemptionCount[]>('/stats/redemptions/daily', { params });
  }
}
