import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Match } from '../models';

@Injectable({ providedIn: 'root' })
export class MatchesService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getMatches(params?: Record<string, string | number | boolean>): Observable<ApiResponse<Match[]>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        httpParams = httpParams.set(key, String(value));
      });
    }
    return this.http.get<ApiResponse<Match[]>>(`${this.baseUrl}/matches`, { params: httpParams });
  }

  updateMatchStatus(matchId: string, status: string): Observable<ApiResponse<Match>> {
    return this.http.put<ApiResponse<Match>>(`${this.baseUrl}/matches/${matchId}/status`, { status });
  }

  updateMatchScore(matchId: string, score: { team1: number; team2: number }): Observable<ApiResponse<Match>> {
    return this.http.put<ApiResponse<Match>>(`${this.baseUrl}/matches/${matchId}/score`, { score });
  }

  updateMatch(matchId: string, payload: Partial<Match>): Observable<ApiResponse<Match>> {
    return this.http.put<ApiResponse<Match>>(`${this.baseUrl}/matches/${matchId}`, payload);
  }

  validateMatchResult(id: string): Observable<ApiResponse<Match>> {
    return this.http.post<ApiResponse<Match>>(`${this.baseUrl}/matches/${id}/validate`, {});
  }
}
