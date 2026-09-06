import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrgService } from '../org.service';
import { OrgContextService } from '../org-context.service';
import { Org } from '../org.model';

@Component({
  imports: [FormsModule],
  selector: 'app-org-context-selector',
  styleUrl: './org-context-selector.css',
  templateUrl: './org-context-selector.html',
})
export class OrgContextSelector implements OnInit {
  private readonly orgService = inject(OrgService);
  readonly orgContext = inject(OrgContextService);

  readonly orgs = signal<Org[]>([]);

  ngOnInit(): void {
    this.orgService.list().subscribe((orgs) => this.orgs.set(orgs));
  }
}
