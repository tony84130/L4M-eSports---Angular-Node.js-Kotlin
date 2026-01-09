import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Notification } from '../models';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<ApiResponse<Notification[]>> {
    return this.http.get<ApiResponse<Notification[]>>(`${this.baseUrl}/notifications`);
  }

  markAllRead(): Observable<ApiResponse<{ updatedCount: number }>> {
    return this.http.put<ApiResponse<{ updatedCount: number }>>(
      `${this.baseUrl}/notifications/read-all`,
      {}
    );
  }

  getUnreadCount(): Observable<ApiResponse<{ count: number }>> {
    return this.http.get<ApiResponse<{ count: number }>>(
      `${this.baseUrl}/notifications/unread-count`
    );
  }
}
