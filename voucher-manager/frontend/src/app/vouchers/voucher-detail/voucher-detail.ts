import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VoucherService } from '../voucher.service';
import { Voucher, VoucherUserMapping } from '../voucher.model';
import { UserService } from '../../users/user.service';
import { User } from '../../users/user.model';

@Component({
  imports: [FormsModule],
  selector: 'app-voucher-detail',
  styleUrl: './voucher-detail.css',
  templateUrl: './voucher-detail.html',
})
export class VoucherDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly voucherService = inject(VoucherService);
  private readonly userService = inject(UserService);

  readonly voucher = signal<Voucher | null>(null);
  readonly users = signal<User[]>([]);
  readonly assignments = signal<VoucherUserMapping[]>([]);
  readonly loading = signal(false);
  selectedUserId = '';
  linkUserId = '';
  actionMessage: string | null = null;
  actionError: string | null = null;

  ngOnInit(): void {
    this.reload();
  }

  userEmail(userId: string): string {
    return this.users().find((user) => user.id === userId)?.email ?? userId;
  }

  private reload(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.loading.set(true);
    this.voucherService.get(id).subscribe({
      next: (voucher) => {
        this.voucher.set(voucher);
        this.loading.set(false);
        this.userService.list(voucher.orgId).subscribe((users) => this.users.set(users));
        this.loadAssignments(id);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadAssignments(id: string): void {
    this.voucherService.listAssignments(id).subscribe((assignments) => this.assignments.set(assignments));
  }

  redeem(): void {
    const voucher = this.voucher();
    if (!voucher || !this.selectedUserId) {
      return;
    }

    this.actionMessage = null;
    this.actionError = null;

    this.voucherService.redeem(voucher.code, this.selectedUserId).subscribe({
      next: () => {
        this.actionMessage = 'Voucher redeemed.';
        this.reload();
      },
      error: (err) => {
        this.actionError = err.error?.detail ?? 'Failed to redeem voucher';
      },
    });
  }

  forceExpire(): void {
    const voucher = this.voucher();
    if (!voucher) {
      return;
    }

    this.actionMessage = null;
    this.actionError = null;

    this.voucherService.forceExpire(voucher.id).subscribe({
      next: () => {
        this.actionMessage = 'Voucher force-expired.';
        this.reload();
      },
      error: (err) => {
        this.actionError = err.error?.detail ?? 'Failed to force-expire voucher';
      },
    });
  }

  linkUser(): void {
    const voucher = this.voucher();
    if (!voucher || !this.linkUserId) {
      return;
    }

    this.actionMessage = null;
    this.actionError = null;

    this.voucherService.assign(voucher.id, this.linkUserId).subscribe({
      next: () => {
        this.actionMessage = 'Voucher linked to user.';
        this.linkUserId = '';
        this.reload();
      },
      error: (err) => {
        this.actionError = err.error?.detail ?? 'Failed to link voucher to user';
      },
    });
  }

  revokeAssignment(userId: string): void {
    const voucher = this.voucher();
    if (!voucher) {
      return;
    }

    this.actionMessage = null;
    this.actionError = null;

    this.voucherService.revoke(voucher.id, userId).subscribe({
      next: () => {
        this.actionMessage = 'Assignment revoked.';
        this.reload();
      },
      error: (err) => {
        this.actionError = err.error?.detail ?? 'Failed to revoke assignment';
      },
    });
  }
}
