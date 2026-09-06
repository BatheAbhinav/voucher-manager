import { Component, signal } from '@angular/core';
import { OrgForm } from '../org-form/org-form';
import { OrgList } from '../org-list/org-list';

@Component({
  imports: [OrgForm, OrgList],
  selector: 'app-orgs-page',
  styleUrl: './orgs-page.css',
  templateUrl: './orgs-page.html',
})
export class OrgsPage {
  readonly showForm = signal(false);

  toggleForm(): void {
    this.showForm.update((v) => !v);
  }
}
