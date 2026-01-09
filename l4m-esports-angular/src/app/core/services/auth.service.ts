import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, AuthSession, AuthUser, SignInPayload, SignUpPayload } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  /**
   * Relative base path to work with a local dev proxy (Angular `ng serve` -> `http://localhost:4200`)
   * against the Node API (`http://localhost:3000`). Adjust if you host the API elsewhere.
   */
  private readonly baseUrl =
    typeof window !== 'undefined' && window.location.origin.includes('localhost:4200')
      ? 'http://localhost:3000/api'
      : '/api';
  private readonly tokenKey = 'l4m_token';
  private readonly userKey = 'l4m_user';

  constructor(private http: HttpClient) {}

  login(payload: SignInPayload, rememberMe = true): Observable<ApiResponse<AuthSession>> {
    return this.http
      .post<ApiResponse<AuthSession>>(`${this.baseUrl}/auth/sign-in`, payload)
      .pipe(tap((response) => this.persistSession(response.data, rememberMe)));
  }

  signUp(payload: SignUpPayload, rememberMe = true): Observable<ApiResponse<AuthSession>> {
    return this.http
      .post<ApiResponse<AuthSession>>(`${this.baseUrl}/auth/sign-up`, payload)
      .pipe(tap((response) => this.persistSession(response.data, rememberMe)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.userKey);
  }

  get token(): string | null {
    return localStorage.getItem(this.tokenKey) ?? sessionStorage.getItem(this.tokenKey);
  }

  get currentUser(): AuthUser | null {
    const rawUser = localStorage.getItem(this.userKey) ?? sessionStorage.getItem(this.userKey);
    return rawUser ? (JSON.parse(rawUser) as AuthUser) : null;
  }

  private persistSession(data: AuthSession, remember: boolean): void {
    const storage = remember ? localStorage : sessionStorage;
    storage.setItem(this.tokenKey, data.token);
    storage.setItem(this.userKey, JSON.stringify(data.user));
  }
}
