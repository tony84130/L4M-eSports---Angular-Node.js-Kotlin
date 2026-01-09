import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GamesService } from '../../core/services/games.service';
import { AuthService } from '../../core/services/auth.service';
import { SocketService } from '../../core/services/socket.service';
import { Game } from '../../core/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-games-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './games-page.component.html',
  styleUrl: './games-page.component.scss'
})
export class GamesPageComponent implements OnInit, OnDestroy {
  games = signal<Game[]>([]);
  loading = signal(true);
  creating = signal(false);
  showCreateDialog = signal(false);
  form = signal<{ name: string; description: string; logo: string; rules: string; formats: string }>({
    name: '',
    description: '',
    logo: '',
    rules: '',
    formats: ''
  });
  message = signal<string | null>(null);
  error = signal<string | null>(null);
  private readonly authService = inject(AuthService);
  private readonly socketService = inject(SocketService);
  private socketSubscriptions: Subscription[] = [];

  constructor(private gamesService: GamesService) {}

  ngOnInit(): void {
    this.refresh();
    this.setupSocketListeners();
  }

  ngOnDestroy(): void {
    this.socketSubscriptions.forEach(sub => sub.unsubscribe());
  }

  setupSocketListeners(): void {
    this.socketService.connect();

    // Listen for game updates
    const gameUpdatedSub = this.socketService.on('game:updated').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(gameUpdatedSub);

    // Listen for game creations
    const gameCreatedSub = this.socketService.on('game:created').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(gameCreatedSub);

    // Listen for game deletions
    const gameDeletedSub = this.socketService.on('game:deleted').subscribe((data: any) => {
      this.games.update(games => games.filter(g => g._id !== data.gameId));
    });
    this.socketSubscriptions.push(gameDeletedSub);
  }

  refresh(): void {
    this.loading.set(true);
    this.gamesService.getGames().subscribe({
      next: (res) => this.games.set(res.data || []),
      error: () => this.loading.set(false),
      complete: () => this.loading.set(false)
    });
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role === 'admin';
  }

  openCreateDialog(): void {
    this.showCreateDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeCreateDialog(): void {
    this.showCreateDialog.set(false);
    this.form.set({ name: '', description: '', logo: '', rules: '', formats: '' });
    this.error.set(null);
    this.message.set(null);
  }

  createGame(): void {
    const payload = this.form();
    if (!payload.name) {
      this.error.set('Le nom est obligatoire');
      return;
    }
    this.creating.set(true);
    this.error.set(null);
    this.message.set(null);
    
    const gamePayload: any = {
      name: payload.name,
      description: payload.description || undefined,
      logo: payload.logo || undefined,
      rules: payload.rules || undefined
    };
    
    // Parse formats (comma-separated string)
    if (payload.formats) {
      gamePayload.formats = payload.formats.split(',').map(f => f.trim()).filter(f => f.length > 0);
    }
    
    this.gamesService.createGame(gamePayload).subscribe({
      next: () => {
        this.message.set('Jeu créé avec succès');
        this.closeCreateDialog();
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de créer le jeu');
      },
      complete: () => this.creating.set(false)
    });
  }

  updateForm(patch: Partial<{ name: string; description: string; logo: string; rules: string; formats: string }>): void {
    this.form.set({ ...this.form(), ...patch });
  }
}
