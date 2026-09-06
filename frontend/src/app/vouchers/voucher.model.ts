export type VoucherStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'EXPIRED' | 'EXHAUSTED';
export type VoucherScope = 'FREE' | 'USER_SPECIFIC';
export type MappingStatus = 'ASSIGNED' | 'REDEEMED' | 'EXPIRED' | 'REVOKED';

export interface Voucher {
  id: string;
  orgId: string;
  code: string;
  title: string | null;
  status: VoucherStatus;
  scope: VoucherScope;
  startsAt: string | null;
  expiresAt: string | null;
  maxRedemptions: number | null;
  attributes: Record<string, unknown>;
}

export interface CreateVoucherRequest {
  code: string;
  title?: string;
  status?: VoucherStatus;
  scope?: VoucherScope;
  startsAt?: string;
  expiresAt?: string;
  maxRedemptions?: number;
  attributes?: Record<string, unknown>;
  orgId?: string;
  assignedUserIds?: string[];
}

export interface VoucherUserMapping {
  id: string;
  voucherId: string;
  userId: string;
  status: MappingStatus;
  assignedAt: string;
  redeemedAt: string | null;
}
