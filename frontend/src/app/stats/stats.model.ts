export interface VoucherStats {
  totalVouchers: number;
  activeVouchers: number;
  draftVouchers: number;
  pausedVouchers: number;
  expiredVouchers: number;
  exhaustedVouchers: number;
  totalUsers: number;
  totalRedemptions: number;
}

export interface OrgVoucherStats {
  orgId: string;
  orgName: string;
  stats: VoucherStats;
}

export type ActivityEventType =
  | 'CREATED'
  | 'ACTIVATED'
  | 'ASSIGNED'
  | 'REDEEMED'
  | 'EXPIRED'
  | 'EXHAUSTED'
  | 'REVOKED'
  | 'UPDATED';

export interface ActivityEntry {
  id: string;
  voucherId: string;
  voucherCode: string;
  userId: string | null;
  userEmail: string | null;
  eventType: ActivityEventType;
  eventData: Record<string, unknown>;
  createdAt: string;
}

export interface DailyRedemptionCount {
  day: string;
  count: number;
}
