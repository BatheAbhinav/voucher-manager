import { Component, computed, inject, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../user.service';
import { User } from '../user.model';
import { AuthService } from '../../auth/auth.service';
import { OrgContextService } from '../../orgs/org-context.service';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-user-form',
  styleUrl: './user-form.css',
  templateUrl: './user-form.html',
})
export class UserForm {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly orgContext = inject(OrgContextService);

  readonly created = output<User>();
  errorMessage: string | null = null;

  readonly orgSelectionRequired = computed(
    () => this.authService.role() === 'ADMIN' && !this.orgContext.selectedOrgId(),
  );

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    name: [''],
  });

  submit(): void {
    if (this.form.invalid || this.orgSelectionRequired()) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    const { email, name } = this.form.getRawValue();
    const orgId = this.authService.role() === 'ADMIN' ? (this.orgContext.selectedOrgId() ?? undefined) : undefined;

    this.userService.create({ email, name: name || undefined, orgId }).subscribe({
      next: (user) => {
        this.created.emit(user);
        this.form.reset();
      },
      error: (err) => {
        this.errorMessage = err.error?.detail ?? 'Failed to create user';
      },
    });
  }
}
