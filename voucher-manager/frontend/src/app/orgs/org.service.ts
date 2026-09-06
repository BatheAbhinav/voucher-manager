import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateOrgRequest, Org } from './org.model';

@Injectable({ providedIn: 'root' })
export class OrgService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/orgs';

  create(request: CreateOrgRequest): Observable<Org> {
    return this.http.post<Org>(this.baseUrl, request);
  }

  list(): Observable<Org[]> {
    return this.http.get<Org[]>(this.baseUrl);
  }
}
