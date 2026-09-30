import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { AuthResponse, User } from './models';

const STORAGE_KEY = 'agency-voyage:auth';

interface StoredAuth {
  token: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly stored = signal<StoredAuth | null>(readStoredAuth());

  readonly currentUser = computed(() => this.stored()?.user ?? null);
  readonly isLoggedIn = computed(() => this.stored() !== null);

  get token(): string | null {
    return this.stored()?.token ?? null;
  }

  register(email: string, password: string, displayName: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_BASE_URL}/api/auth/register`, { email, password, displayName })
      .pipe(tap((response) => this.storeAuth(response)));
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_BASE_URL}/api/auth/login`, { email, password })
      .pipe(tap((response) => this.storeAuth(response)));
  }

  logout(): void {
    this.stored.set(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore - localStorage unavailable
    }
  }

  private storeAuth(response: AuthResponse): void {
    const auth: StoredAuth = { token: response.token, user: response.user };
    this.stored.set(auth);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } catch {
      // ignore - localStorage unavailable
    }
  }
}

function readStoredAuth(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as StoredAuth) : null;
  } catch {
    return null;
  }
}
