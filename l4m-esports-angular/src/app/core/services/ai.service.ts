import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, AiAnswer, AiRequestContext } from '../models';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly baseUrl =
    typeof window !== 'undefined' && window.location.origin.includes('localhost:4200')
      ? 'http://localhost:3000/api'
      : '/api';

  constructor(private http: HttpClient) {}

  ask(question: string, context: AiRequestContext = {}): Observable<ApiResponse<AiAnswer>> {
    return this.http.post<ApiResponse<AiAnswer>>(`${this.baseUrl}/ai/assist`, {
      question,
      context
    });
  }
}
