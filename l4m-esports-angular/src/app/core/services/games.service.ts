import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Game } from '../models';

@Injectable({ providedIn: 'root' })
export class GamesService {
  private readonly baseUrl = 'http://localhost:3000/api';

  constructor(private http: HttpClient) {}

  getGames(): Observable<ApiResponse<Game[]>> {
    return this.http.get<ApiResponse<Game[]>>(`${this.baseUrl}/games`);
  }

  createGame(payload: { name: string; description?: string; logo?: string; rules?: string; formats?: string[] }): Observable<ApiResponse<Game>> {
    return this.http.post<ApiResponse<Game>>(`${this.baseUrl}/games`, payload);
  }
}
