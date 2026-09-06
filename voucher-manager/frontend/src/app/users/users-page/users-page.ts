import { Component, signal } from '@angular/core';
import { UserForm } from '../user-form/user-form';
import { UserList } from '../user-list/user-list';

@Component({
  imports: [UserForm, UserList],
  selector: 'app-users-page',
  styleUrl: './users-page.css',
  templateUrl: './users-page.html',
})
export class UsersPage {
  readonly showForm = signal(false);

  toggleForm(): void {
    this.showForm.update((v) => !v);
  }
}
