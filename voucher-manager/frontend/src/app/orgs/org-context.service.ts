import { Injectable, signal } from '@angular/core';

/**
 * Admins aren't scoped to a single org the way org logins are, so admin-facing
 * Users/Vouchers views need an explicit org to operate against. This holds that
 * choice; org-role sessions never read it since the backend scopes them by token.
 */
@Injectable({ providedIn: 'root' })
export class OrgContextService {
  readonly selectedOrgId = signal<string | null>(null);

  select(orgId: string | null): void {
    this.selectedOrgId.set(orgId);
  }
}
