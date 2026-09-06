import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateVoucherRequest, Voucher, VoucherUserMapping } from './voucher.model';

@Injectable({ providedIn: 'root' })
export class VoucherService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/vouchers';

  create(request: CreateVoucherRequest): Observable<Voucher> {
    return this.http.post<Voucher>(this.baseUrl, request);
  }

  get(id: string): Observable<Voucher> {
    return this.http.get<Voucher>(`${this.baseUrl}/${id}`);
  }

  list(orgId?: string): Observable<Voucher[]> {
    const params = orgId ? new HttpParams().set('orgId', orgId) : undefined;
    return this.http.get<Voucher[]>(this.baseUrl, { params });
  }

  redeem(code: string, userId: string): Observable<VoucherUserMapping> {
    return this.http.post<VoucherUserMapping>(`${this.baseUrl}/${code}/redeem`, { userId });
  }

  forceExpire(id: string, reason?: string): Observable<Voucher> {
    return this.http.post<Voucher>(`${this.baseUrl}/${id}/force-expire`, reason ? { reason } : {});
  }

  assign(id: string, userId: string): Observable<VoucherUserMapping> {
    return this.http.post<VoucherUserMapping>(`${this.baseUrl}/${id}/assign`, { userId });
  }

  revoke(id: string, userId: string): Observable<VoucherUserMapping> {
    return this.http.post<VoucherUserMapping>(`${this.baseUrl}/${id}/revoke`, { userId });
  }

  listAssignments(id: string): Observable<VoucherUserMapping[]> {
    return this.http.get<VoucherUserMapping[]>(`${this.baseUrl}/${id}/assignments`);
  }
}
