import { Component, computed, effect, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { VoucherService } from '../voucher.service';
import { Voucher, VoucherScope, VoucherStatus } from '../voucher.model';
import { AuthService } from '../../auth/auth.service';
import { OrgContextService } from '../../orgs/org-context.service';
import { UserService } from '../../users/user.service';
import { User } from '../../users/user.model';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-voucher-form',
  styleUrl: './voucher-form.css',
  templateUrl: './voucher-form.html',
})
export class VoucherForm {
  private readonly fb = inject(FormBuilder);
  private readonly voucherService = inject(VoucherService);
  private readonly authService = inject(AuthService);
  private readonly orgContext = inject(OrgContextService);
  private readonly userService = inject(UserService);

  readonly created = output<Voucher>();
  readonly statuses: VoucherStatus[] = ['DRAFT', 'ACTIVE', 'PAUSED', 'EXPIRED', 'EXHAUSTED'];
  readonly scopes: VoucherScope[] = ['FREE', 'USER_SPECIFIC'];
  errorMessage: string | null = null;

  readonly candidateUsers = signal<User[]>([]);
  readonly selectedUserIds = signal<ReadonlySet<string>>(new Set());

  readonly orgSelectionRequired = computed(
    () => this.authService.role() === 'ADMIN' && !this.orgContext.selectedOrgId(),
  );

  readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(64)]],
    title: [''],
    status: ['DRAFT' as VoucherStatus],
    scope: ['FREE' as VoucherScope],
    maxRedemptions: [null as number | null],
  });

  constructor() {
    effect(() => this.loadCandidateUsers());
  }

  private loadCandidateUsers(): void {
    const orgId = this.authService.role() === 'ADMIN' ? (this.orgContext.selectedOrgId() ?? undefined) : undefined;
    this.userService.list(orgId).subscribe((users) => this.candidateUsers.set(users));
  }

  toggleUser(userId: string, checked: boolean): void {
    const next = new Set(this.selectedUserIds());
    if (checked) {
      next.add(userId);
    } else {
      next.delete(userId);
    }
    this.selectedUserIds.set(next);
  }

  submit(): void {
    if (this.form.invalid || this.orgSelectionRequired()) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    const { code, title, status, scope, maxRedemptions } = this.form.getRawValue();
    const orgId = this.authService.role() === 'ADMIN' ? (this.orgContext.selectedOrgId() ?? undefined) : undefined;
    const assignedUserIds = scope === 'USER_SPECIFIC' ? Array.from(this.selectedUserIds()) : undefined;

    this.voucherService
      .create({
        code,
        title: title || undefined,
        status,
        scope,
        maxRedemptions: maxRedemptions ?? undefined,
        orgId,
        assignedUserIds,
      })
      .subscribe({
        next: (voucher) => {
          this.created.emit(voucher);
          this.form.reset({ status: 'DRAFT', scope: 'FREE', code: '', title: '', maxRedemptions: null });
          this.selectedUserIds.set(new Set());
        },
        error: (err) => {
          this.errorMessage = err.error?.detail ?? 'Failed to create voucher';
        },
      });
  }
}
