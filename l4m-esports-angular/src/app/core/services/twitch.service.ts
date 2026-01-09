import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models';

export interface TwitchUser {
  display_name: string;
  description?: string;
  profile_image_url?: string;
  view_count?: number;
  followers?: number;
  is_live?: boolean;
}

@Injectable({ providedIn: 'root' })
export class TwitchService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getUser(username: string): Observable<ApiResponse<TwitchUser>> {
    return this.http.get<ApiResponse<TwitchUser>>(`${this.baseUrl}/twitch/user/${username}`);
  }
}
