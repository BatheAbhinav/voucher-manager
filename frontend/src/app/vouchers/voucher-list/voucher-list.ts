import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { VoucherService } from '../voucher.service';
import { Voucher } from '../voucher.model';
import { AuthService } from '../../auth/auth.service';
import { OrgContextService } from '../../orgs/org-context.service';

@Component({
  imports: [RouterLink],
  selector: 'app-voucher-list',
  styleUrl: './voucher-list.css',
  templateUrl: './voucher-list.html',
})
export class VoucherList {
  private readonly voucherService = inject(VoucherService);
  private readonly authService = inject(AuthService);
  private readonly orgContext = inject(OrgContextService);

  readonly vouchers = signal<Voucher[]>([]);
  readonly loading = signal(false);

  constructor() {
    effect(() => this.reload());
  }

  reload(): void {
    this.loading.set(true);
    const orgId = this.authService.role() === 'ADMIN' ? (this.orgContext.selectedOrgId() ?? undefined) : undefined;
    this.voucherService.list(orgId).subscribe({
      next: (vouchers) => {
        this.vouchers.set(vouchers);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
