import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { Role } from '../auth.model';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-login-page',
  styleUrl: './login-page.css',
  templateUrl: './login-page.html',
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly role = signal<Role>('ORG');
  errorMessage: string | null = null;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  selectRole(role: Role): void {
    this.role.set(role);
    this.errorMessage = null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    const { email, password } = this.form.getRawValue();
    const login$ = this.role() === 'ADMIN'
      ? this.authService.loginAdmin({ email, password })
      : this.authService.loginOrg({ email, password });

    login$.subscribe({
      next: () => this.router.navigateByUrl('/users'),
      error: (err) => {
        this.errorMessage = err.error?.detail ?? 'Login failed';
      },
    });
  }
}
