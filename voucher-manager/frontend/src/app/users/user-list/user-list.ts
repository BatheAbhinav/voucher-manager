import { Component, effect, inject, signal } from '@angular/core';
import { UserService } from '../user.service';
import { User } from '../user.model';
import { AuthService } from '../../auth/auth.service';
import { OrgContextService } from '../../orgs/org-context.service';

@Component({
  imports: [],
  selector: 'app-user-list',
  styleUrl: './user-list.css',
  templateUrl: './user-list.html',
})
export class UserList {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly orgContext = inject(OrgContextService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(false);

  constructor() {
    effect(() => this.reload());
  }

  reload(): void {
    this.loading.set(true);
    const orgId = this.authService.role() === 'ADMIN' ? (this.orgContext.selectedOrgId() ?? undefined) : undefined;
    this.userService.list(orgId).subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
