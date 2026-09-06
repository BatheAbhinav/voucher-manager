import { Component, OnInit, inject, signal } from '@angular/core';
import { OrgService } from '../org.service';
import { Org } from '../org.model';

@Component({
  imports: [],
  selector: 'app-org-list',
  styleUrl: './org-list.css',
  templateUrl: './org-list.html',
})
export class OrgList implements OnInit {
  private readonly orgService = inject(OrgService);

  readonly orgs = signal<Org[]>([]);
  readonly loading = signal(false);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.orgService.list().subscribe({
      next: (orgs) => {
        this.orgs.set(orgs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
