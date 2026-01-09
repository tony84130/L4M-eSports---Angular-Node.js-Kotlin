import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, AuthUser } from '../models';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getMe(): Observable<ApiResponse<AuthUser>> {
    return this.http.get<ApiResponse<AuthUser>>(`${this.baseUrl}/users/me`);
  }

  updateMe(payload: Partial<AuthUser>): Observable<ApiResponse<AuthUser>> {
    return this.http.put<ApiResponse<AuthUser>>(`${this.baseUrl}/users/me`, payload);
  }

  getAllUsers(): Observable<ApiResponse<AuthUser[]>> {
    // Passer une limite élevée pour récupérer tous les utilisateurs
    const params = new HttpParams().set('limit', 1000);
    return this.http.get<ApiResponse<AuthUser[]>>(`${this.baseUrl}/users`, { params });
  }

  deleteUser(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/users/${id}`);
  }
}
