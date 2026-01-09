import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Team } from '../models';

@Injectable({ providedIn: 'root' })
export class TeamsService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getTeams(params?: Record<string, string | number | boolean>): Observable<ApiResponse<Team[]>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        httpParams = httpParams.set(key, String(value));
      });
    }
    return this.http.get<ApiResponse<Team[]>>(`${this.baseUrl}/teams`, { params: httpParams });
  }

  createTeam(payload: Partial<Team> & { game: string }): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.baseUrl}/teams`, payload);
  }

  getTeamById(id: string): Observable<ApiResponse<Team>> {
    return this.http.get<ApiResponse<Team>>(`${this.baseUrl}/teams/${id}`);
  }

  transferCaptain(teamId: string, newCaptainId: string): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.baseUrl}/teams/${teamId}/transfer-captain`, {
      newCaptainId
    });
  }

  deleteTeam(teamId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/teams/${teamId}`);
  }
}
