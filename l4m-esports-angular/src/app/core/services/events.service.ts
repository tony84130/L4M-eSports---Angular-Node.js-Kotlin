import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Event } from '../models';

@Injectable({ providedIn: 'root' })
export class EventsService {
  private readonly baseUrl = 'http://localhost:3000/api';
  private readonly noCacheHeaders = new HttpHeaders({
    'Cache-Control': 'no-cache',
    Pragma: 'no-cache',
    'If-Modified-Since': '0',
    'If-None-Match': '"disable-cache"'
  });

  constructor(private http: HttpClient) {}

  getEvents(params?: Record<string, string | number | boolean>): Observable<ApiResponse<Event[]>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        httpParams = httpParams.set(key, String(value));
      });
    }
    // Ajout d'un cache-buster pour éviter les 304 sans corps
    httpParams = httpParams.set('_', Date.now().toString());

    return this.http.get<ApiResponse<Event[]>>(`${this.baseUrl}/events`, {
      params: httpParams,
      headers: this.noCacheHeaders
    });
  }

  createEvent(payload: {
    name: string;
    game: string;
    format: string;
    startDate: string;
    endDate: string;
    registrationStartDate: string;
    registrationEndDate: string;
    description?: string;
    rules?: string;
    maxTeams?: number;
    location?: {
      type: 'online' | 'physical';
      address?: string;
      coordinates?: {
        latitude: number;
        longitude: number;
      };
    };
  }): Observable<ApiResponse<Event>> {
    return this.http.post<ApiResponse<Event>>(`${this.baseUrl}/events`, payload);
  }

  updateEvent(id: string, payload: {
    name?: string;
    game?: string;
    format?: string;
    startDate?: string;
    endDate?: string;
    registrationStartDate?: string;
    registrationEndDate?: string;
    description?: string;
    rules?: string;
    maxTeams?: number;
    status?: string;
    location?: {
      type: 'online' | 'physical';
      address?: string;
      coordinates?: {
        latitude: number;
        longitude: number;
      };
    };
  }): Observable<ApiResponse<Event>> {
    return this.http.put<ApiResponse<Event>>(`${this.baseUrl}/events/${id}`, payload);
  }

  deleteEvent(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/events/${id}`);
  }

  generateBracket(id: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/events/${id}/generate-bracket`, {});
  }

  getEventBracket(id: string): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/events/${id}/bracket`);
  }
}
