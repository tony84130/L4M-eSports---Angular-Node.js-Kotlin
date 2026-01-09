import { CommonModule } from '@angular/common';
import { Component, OnInit, signal, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NotificationsService } from '../../core/services/notifications.service';
import { AuthService } from '../../core/services/auth.service';
import { AiHelpWidgetComponent } from '../support/ai-help-widget.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, AiHelpWidgetComponent],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.scss'
})
export class AppLayoutComponent implements OnInit {
  unreadCount = signal(0);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  constructor(
    private notificationsService: NotificationsService
  ) {}

  ngOnInit(): void {
    this.refreshUnread();
  }

  refreshUnread(): void {
    this.notificationsService.getUnreadCount().subscribe({
      next: (res) => this.unreadCount.set(res.data?.count ?? 0)
    });
  }

  goNotifications(): void {
    const user = this.auth.currentUser;
    if (user?.role === 'admin') {
      this.router.navigateByUrl('/app/users');
    } else {
      this.router.navigateByUrl('/app/notifications').then(() => {
        this.unreadCount.set(0);
      });
    }
  }

  isAdmin(): boolean {
    return this.auth.currentUser?.role === 'admin';
  }

  goProfile(): void {
    this.router.navigateByUrl('/app/profile');
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  avatarLetter(): string {
    const user = this.auth.currentUser;
    const base = user?.gamertag || user?.firstName || user?.email || '?';
    return base.slice(0, 1).toUpperCase();
  }
}
