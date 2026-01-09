import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { NotificationsService } from '../../core/services/notifications.service';
import { Notification } from '../../core/models';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.scss'
})
export class NotificationsPageComponent implements OnInit {
  notifications = signal<Notification[]>([]);
  loading = signal(true);
  message = signal<string | null>(null);

  constructor(private notificationsService: NotificationsService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.notificationsService.getNotifications().subscribe({
      next: (res) => this.notifications.set(res.data || []),
      error: () => this.loading.set(false),
      complete: () => this.loading.set(false)
    });
  }

  markAll(): void {
    this.notificationsService.markAllRead().subscribe({
      next: () => {
        this.message.set('Toutes les notifications sont marquées comme lues.');
        this.refresh();
      }
    });
  }
}
