import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, CreateUserRequest } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/users';

  create(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }

  get(id: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  list(orgId?: string): Observable<User[]> {
    const params = orgId ? new HttpParams().set('orgId', orgId) : undefined;
    return this.http.get<User[]>(this.baseUrl, { params });
  }
}
