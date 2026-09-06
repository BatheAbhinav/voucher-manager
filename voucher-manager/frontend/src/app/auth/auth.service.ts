import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, Role } from './auth.model';

const STORAGE_KEY = 'voucher-manager-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly session = signal<LoginResponse | null>(this.readStoredSession());

  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly role = computed<Role | null>(() => this.session()?.role ?? null);
  readonly name = computed(() => this.session()?.name ?? null);
  readonly id = computed(() => this.session()?.id ?? null);

  get token(): string | null {
    return this.session()?.token ?? null;
  }

  loginAdmin(request: LoginRequest): Observable<LoginResponse> {
    return this.login('/auth/admin/login', request);
  }

  loginOrg(request: LoginRequest): Observable<LoginResponse> {
    return this.login('/auth/org/login', request);
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  private login(url: string, request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(url, request).pipe(
      tap((response) => {
        this.session.set(response);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
      }),
    );
  }

  private readStoredSession(): LoginResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as LoginResponse) : null;
  }
}
