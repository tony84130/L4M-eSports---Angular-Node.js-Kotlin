import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { UsersService } from '../../core/services/users.service';
import { AuthService } from '../../core/services/auth.service';
import { SocketService } from '../../core/services/socket.service';
import { AuthUser } from '../../core/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users-page.component.html',
  styleUrl: './users-page.component.scss'
})
export class UsersPageComponent implements OnInit, OnDestroy {
  users = signal<AuthUser[]>([]);
  loading = signal(true);
  message = signal<string | null>(null);
  deleteConfirmId = signal<string | null>(null);
  private readonly usersService = inject(UsersService);
  private readonly authService = inject(AuthService);
  private readonly socketService = inject(SocketService);
  private socketSubscriptions: Subscription[] = [];

  ngOnInit(): void {
    this.refresh();
    this.setupSocketListeners();
  }

  ngOnDestroy(): void {
    this.socketSubscriptions.forEach(sub => sub.unsubscribe());
  }

  setupSocketListeners(): void {
    console.log('🔧 Setting up Socket.io listeners...');
    this.socketService.connect();

    // Check connection status
    this.socketService.isConnected().subscribe(connected => {
      console.log('🔌 Socket.io connection status:', connected);
    });

    // Listen for user updates
    const userUpdatedSub = this.socketService.on('user:updated').subscribe((data: any) => {
      console.log('🔔 Socket event: user:updated', data);
      this.refresh();
    });
    this.socketSubscriptions.push(userUpdatedSub);

    // Listen for user deletions
    const userDeletedSub = this.socketService.on('user:deleted').subscribe((data: any) => {
      console.log('🔔 Socket event: user:deleted', data);
      if (data && data.userId) {
        const beforeCount = this.users().length;
        this.users.update(users => users.filter(u => u._id !== data.userId));
        const afterCount = this.users().length;
        console.log(`✅ User ${data.userId} removed from list (${beforeCount} -> ${afterCount})`);
      } else {
        // If data structure is different, refresh the whole list
        console.log('⚠️ Unexpected data structure, refreshing list...');
        this.refresh();
      }
    });
    this.socketSubscriptions.push(userDeletedSub);

    // Listen for role updates
    const roleUpdatedSub = this.socketService.on('user:roleUpdated').subscribe((data: any) => {
      console.log('🔔 Socket event: user:roleUpdated', data);
      this.refresh();
    });
    this.socketSubscriptions.push(roleUpdatedSub);
  }

  refresh(): void {
    this.loading.set(true);
    this.usersService.getAllUsers().subscribe({
      next: (res) => {
        this.users.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.message.set('Erreur lors du chargement des utilisateurs.');
        this.loading.set(false);
      }
    });
  }

  deleteUser(id: string): void {
    this.deleteConfirmId.set(id);
  }

  confirmDelete(): void {
    const id = this.deleteConfirmId();
    if (!id) return;

    this.loading.set(true);
    this.usersService.deleteUser(id).subscribe({
      next: () => {
        this.message.set('Utilisateur supprimé avec succès.');
        this.deleteConfirmId.set(null);
        this.refresh();
      },
      error: (err) => {
        this.message.set('Erreur lors de la suppression de l\'utilisateur.');
        this.loading.set(false);
      }
    });
  }

  cancelDelete(): void {
    this.deleteConfirmId.set(null);
  }

  isCurrentUser(user: AuthUser): boolean {
    return this.authService.currentUser?._id === user._id;
  }
}

