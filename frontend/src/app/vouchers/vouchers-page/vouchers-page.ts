import { Component, signal } from '@angular/core';
import { VoucherForm } from '../voucher-form/voucher-form';
import { VoucherList } from '../voucher-list/voucher-list';

@Component({
  imports: [VoucherForm, VoucherList],
  selector: 'app-vouchers-page',
  styleUrl: './vouchers-page.css',
  templateUrl: './vouchers-page.html',
})
export class VouchersPage {
  readonly showForm = signal(false);

  toggleForm(): void {
    this.showForm.update((v) => !v);
  }
}
