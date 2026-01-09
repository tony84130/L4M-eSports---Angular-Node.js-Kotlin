import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatchesService } from '../../core/services/matches.service';
import { AuthService } from '../../core/services/auth.service';
import { SocketService } from '../../core/services/socket.service';
import { Match } from '../../core/models';
import { Subscription } from 'rxjs';

type MatchStatusFilter =
  | 'all'
  | 'upcoming'
  | 'in_progress'
  | 'finished'
  | 'pending_validation'
  | 'cancelled';

@Component({
  selector: 'app-matches-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './matches-page.component.html',
  styleUrl: './matches-page.component.scss'
})
export class MatchesPageComponent implements OnInit, OnDestroy {
  matches = signal<Match[]>([]);
  loading = signal(true);
  selectedStatus = signal<MatchStatusFilter>('all');
  selectedMatch = signal<Match | null>(null);
  showStatusDialog = signal(false);
  showScoreDialog = signal(false);
  showValidateDialog = signal(false);
  editing = signal(false);
  validating = signal(false);
  newStatus = signal<string>('');
  scoreTeam1 = signal<string>('0');
  scoreTeam2 = signal<string>('0');
  message = signal<string | null>(null);
  error = signal<string | null>(null);
  private readonly authService = inject(AuthService);
  private readonly socketService = inject(SocketService);
  private socketSubscriptions: Subscription[] = [];

  statusFilters: { key: MatchStatusFilter; label: string }[] = [
    { key: 'all', label: 'Tous' },
    { key: 'upcoming', label: 'À venir' },
    { key: 'in_progress', label: 'En cours' },
    { key: 'pending_validation', label: 'À valider' },
    { key: 'finished', label: 'Terminé' },
    { key: 'cancelled', label: 'Annulé' }
  ];

  constructor(private matchesService: MatchesService) {}

  ngOnInit(): void {
    this.refresh();
    this.setupSocketListeners();
  }

  ngOnDestroy(): void {
    this.socketSubscriptions.forEach(sub => sub.unsubscribe());
  }

  setupSocketListeners(): void {
    this.socketService.connect();
    // Écouter les mises à jour de matchs (si implémenté côté serveur)
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role === 'admin';
  }

  openStatusDialog(match: Match): void {
    this.selectedMatch.set(match);
    this.newStatus.set(match.status);
    this.showStatusDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeStatusDialog(): void {
    this.showStatusDialog.set(false);
    this.selectedMatch.set(null);
    this.newStatus.set('');
    this.error.set(null);
    this.message.set(null);
  }

  openScoreDialog(match: Match): void {
    this.selectedMatch.set(match);
    this.scoreTeam1.set(match.score?.team1?.toString() || '0');
    this.scoreTeam2.set(match.score?.team2?.toString() || '0');
    this.showScoreDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeScoreDialog(): void {
    this.showScoreDialog.set(false);
    this.selectedMatch.set(null);
    this.scoreTeam1.set('0');
    this.scoreTeam2.set('0');
    this.error.set(null);
    this.message.set(null);
  }

  updateMatchStatus(): void {
    const match = this.selectedMatch();
    if (!match) return;

    this.editing.set(true);
    this.error.set(null);
    this.message.set(null);

    this.matchesService.updateMatchStatus(match._id, this.newStatus()).subscribe({
      next: () => {
        this.message.set('Statut du match mis à jour avec succès');
        this.refresh();
        this.closeStatusDialog();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de mettre à jour le statut');
      },
      complete: () => this.editing.set(false)
    });
  }

  updateMatchScore(): void {
    const match = this.selectedMatch();
    if (!match) return;

    const score = {
      team1: parseInt(this.scoreTeam1()) || 0,
      team2: parseInt(this.scoreTeam2()) || 0
    };

    this.editing.set(true);
    this.error.set(null);
    this.message.set(null);

    this.matchesService.updateMatchScore(match._id, score).subscribe({
      next: () => {
        this.message.set('Score du match mis à jour avec succès');
        this.refresh();
        this.closeScoreDialog();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de mettre à jour le score');
      },
      complete: () => this.editing.set(false)
    });
  }

  refresh(): void {
    this.loading.set(true);
    const params: Record<string, string | number | boolean> = {};
    if (this.selectedStatus() !== 'all') {
      params['status'] = this.selectedStatus();
    }

    this.matchesService.getMatches(params).subscribe({
      next: (res) => {
        const data = (res?.data as any)?.matches ?? res?.data ?? [];
        this.matches.set(data);
      },
      error: () => {
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  selectStatus(status: MatchStatusFilter): void {
    this.selectedStatus.set(status);
    this.refresh();
  }

  eventLabel(match: Match): string {
    if (!match.event) return 'Événement inconnu';
    if (typeof match.event === 'string') return match.event;
    return match.event.name ?? 'Événement';
  }

  teamsLabel(match: Match): string {
    const teamNames =
      match.teams?.map((t) => (typeof t === 'string' ? t : t.name)).filter(Boolean) || [];
    return teamNames.length ? teamNames.join(' vs ') : 'Match sans équipes';
  }

  statusLabel(status: Match['status']): string {
    const labels: Record<Match['status'], string> = {
      upcoming: 'À venir',
      in_progress: 'En cours',
      finished: 'Terminé',
      pending_validation: 'À valider',
      cancelled: 'Annulé'
    };
    return labels[status] ?? status;
  }

  getAvailableStatuses(): string[] {
    return ['upcoming', 'in_progress', 'finished', 'pending_validation', 'cancelled'];
  }

  getStatusLabel(status: string): string {
    return this.statusLabel(status as Match['status']);
  }

  openValidateDialog(match: Match): void {
    this.selectedMatch.set(match);
    this.showValidateDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeValidateDialog(): void {
    this.showValidateDialog.set(false);
    this.selectedMatch.set(null);
    this.error.set(null);
    this.message.set(null);
  }

  validateMatch(): void {
    const match = this.selectedMatch();
    if (!match) return;

    this.validating.set(true);
    this.error.set(null);
    this.message.set(null);

    this.matchesService.validateMatchResult(match._id).subscribe({
      next: () => {
        this.message.set('Match validé avec succès');
        this.refresh();
        this.closeValidateDialog();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de valider le match');
      },
      complete: () => this.validating.set(false)
    });
  }
}
