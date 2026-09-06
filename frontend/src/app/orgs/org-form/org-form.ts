import { Component, inject, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { OrgService } from '../org.service';
import { Org } from '../org.model';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-org-form',
  styleUrl: './org-form.css',
  templateUrl: './org-form.html',
})
export class OrgForm {
  private readonly fb = inject(FormBuilder);
  private readonly orgService = inject(OrgService);

  readonly created = output<Org>();
  errorMessage: string | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    this.orgService.create(this.form.getRawValue()).subscribe({
      next: (org) => {
        this.created.emit(org);
        this.form.reset();
      },
      error: (err) => {
        this.errorMessage = err.error?.detail ?? 'Failed to create org';
      },
    });
  }
}
