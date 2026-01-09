import { Injectable, inject } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { AuthService } from './auth.service';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SocketService {
  private socket: Socket | null = null;
  private connected = new BehaviorSubject<boolean>(false);
  private readonly authService = inject(AuthService);

  connect(): void {
    if (this.socket?.connected) {
      return;
    }

    const token = this.authService.token;
    this.socket = io('http://localhost:3000', {
      auth: { token },
      transports: ['websocket', 'polling']
    });

    this.socket.on('connect', () => {
      console.log('🔌 Socket.io connected', this.socket?.id);
      this.connected.next(true);
    });

    this.socket.on('disconnect', (reason) => {
      console.log('🔌 Socket.io disconnected', reason);
      this.connected.next(false);
    });

    this.socket.on('connect_error', (error) => {
      console.error('🔌 Socket.io connection error:', error);
    });
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.connected.next(false);
    }
  }

  on(event: string): Observable<any> {
    return new Observable((observer) => {
      if (!this.socket) {
        this.connect();
      }

      const handler = (data: any) => {
        console.log(`🔔 Socket.io event received: ${event}`, data);
        observer.next(data);
      };

      this.socket!.on(event, handler);
      console.log(`👂 Listening to Socket.io event: ${event}`);

      return () => {
        if (this.socket) {
          this.socket.off(event, handler);
        }
      };
    });
  }

  isConnected(): Observable<boolean> {
    return this.connected.asObservable();
  }

  reconnect(): void {
    this.disconnect();
    this.connect();
  }
}

