import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GamesService } from '../../core/services/games.service';
import { TeamsService } from '../../core/services/teams.service';
import { AuthService } from '../../core/services/auth.service';
import { SocketService } from '../../core/services/socket.service';
import { Game, Team, AuthUser } from '../../core/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-teams-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teams-page.component.html',
  styleUrl: './teams-page.component.scss'
})
export class TeamsPageComponent implements OnInit, OnDestroy {
  teams = signal<Team[]>([]);
  games = signal<Game[]>([]);
  loading = signal(true);
  creating = signal(false);
  showCreateDialog = signal(false);
  form = signal<{ name: string; description: string; game: string; logo: string; maxMembers?: number }>({
    name: '',
    description: '',
    game: '',
    logo: ''
  });
  message = signal<string | null>(null);
  error = signal<string | null>(null);
  selectedTeam = signal<Team | null>(null);
  showTransferDialog = signal(false);
  selectedNewCaptainId = signal<string | null>(null);
  transferring = signal(false);
  private readonly authService = inject(AuthService);
  private readonly socketService = inject(SocketService);
  private socketSubscriptions: Subscription[] = [];

  constructor(private teamsService: TeamsService, private gamesService: GamesService) {}

  ngOnInit(): void {
    this.refresh();
    this.loadGames();
    this.setupSocketListeners();
  }

  ngOnDestroy(): void {
    this.socketSubscriptions.forEach(sub => sub.unsubscribe());
  }

  setupSocketListeners(): void {
    this.socketService.connect();

    // Listen for team updates
    const teamUpdatedSub = this.socketService.on('team:updated').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(teamUpdatedSub);

    // Listen for team creations
    const teamCreatedSub = this.socketService.on('team:created').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(teamCreatedSub);

    // Listen for team deletions
    const teamDeletedSub = this.socketService.on('team:deleted').subscribe((data: any) => {
      this.teams.update(teams => teams.filter(t => t._id !== data.teamId));
    });
    this.socketSubscriptions.push(teamDeletedSub);

    // Listen for captain transfers
    const captainTransferredSub = this.socketService.on('team:captainTransferred').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(captainTransferredSub);
  }

  loadGames(): void {
    this.gamesService.getGames().subscribe({
      next: (res) => this.games.set(res.data || [])
    });
  }

  refresh(): void {
    this.loading.set(true);
    this.teamsService.getTeams({ status: 'active' }).subscribe({
      next: (res) => {
        this.teams.set(res.data || []);
      },
      error: () => this.loading.set(false),
      complete: () => this.loading.set(false)
    });
  }

  openCreateDialog(): void {
    this.showCreateDialog.set(true);
    this.error.set(null);
    this.message.set(null);
    this.form.set({ name: '', description: '', game: '', logo: '', maxMembers: undefined });
  }

  closeCreateDialog(): void {
    this.showCreateDialog.set(false);
    this.form.set({ name: '', description: '', game: '', logo: '', maxMembers: undefined });
    this.error.set(null);
    this.message.set(null);
  }

  createTeam(): void {
    const payload = this.form();
    if (!payload.name || !payload.game) {
      this.error.set('Le nom et le jeu sont obligatoires');
      return;
    }
    this.creating.set(true);
    this.error.set(null);
    this.message.set(null);
    
    const teamPayload: any = {
      name: payload.name,
      game: payload.game,
      description: payload.description || undefined,
      logo: payload.logo || undefined,
      maxMembers: payload.maxMembers || undefined
    };
    
    this.teamsService.createTeam(teamPayload).subscribe({
      next: (res) => {
        this.message.set('Équipe créée avec succès');
        this.closeCreateDialog();
        // Recharger les équipes et les jeux pour mettre à jour la liste des jeux disponibles
        this.refresh();
        this.loadGames();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de créer l\'équipe');
      },
      complete: () => this.creating.set(false)
    });
  }

  updateForm(patch: Partial<{ name: string; description: string; game: string; logo: string; maxMembers?: number }>): void {
    const next = { ...this.form(), ...patch };
    if (typeof next.maxMembers === 'string') {
      const parsed = Number(next.maxMembers);
      next.maxMembers = Number.isNaN(parsed) ? undefined : parsed;
    }
    this.form.set(next);
  }

  getAvailableGames(): Game[] {
    const user = this.getCurrentUser();
    if (!user) return [];
    
    const userTeams = this.teams();
    // Jeux où l'utilisateur est déjà capitaine d'une équipe active
    const gamesWhereUserIsCaptain = userTeams
      .filter(team => {
        const captainId = typeof team.captain === 'string' ? team.captain : team.captain._id;
        return captainId === user._id && team.status === 'active';
      })
      .map(team => typeof team.game === 'string' ? team.game : team.game._id);
    
    // Jeux où l'utilisateur est déjà membre (mais pas capitaine) d'une équipe active
    const gamesWhereUserIsMember = userTeams
      .filter(team => {
        const captainId = typeof team.captain === 'string' ? team.captain : team.captain._id;
        const isMember = team.members.some(m => {
          const memberId = typeof m === 'string' ? m : m._id;
          return memberId === user._id;
        });
        return isMember && captainId !== user._id && team.status === 'active';
      })
      .map(team => typeof team.game === 'string' ? team.game : team.game._id);
    
    // Filtrer les jeux disponibles
    return this.games().filter(game => {
      const gameId = game._id;
      return gameId && !gamesWhereUserIsCaptain.includes(gameId) && !gamesWhereUserIsMember.includes(gameId);
    });
  }

  displayGameName(game: string | Game): string {
    if (typeof game === 'string') {
      return game;
    }
    return game?.name ?? '';
  }

  getCurrentUser(): AuthUser | null {
    return this.authService.currentUser;
  }

  canCreateTeam(): boolean {
    const user = this.getCurrentUser();
    // Seuls les members et captains peuvent créer des équipes (pas les admins)
    return user?.role === 'member' || user?.role === 'captain';
  }

  isCaptain(team: Team): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    const captainId = typeof team.captain === 'string' ? team.captain : team.captain._id;
    return captainId === user._id;
  }

  getCaptainName(team: Team): string {
    if (typeof team.captain === 'string') {
      return team.captain;
    }
    return team.captain?.gamertag || team.captain?.firstName + ' ' + team.captain?.lastName || 'Capitaine';
  }

  getMemberName(member: string | AuthUser): string {
    if (typeof member === 'string') {
      return member;
    }
    return member?.gamertag || member?.firstName + ' ' + member?.lastName || 'Membre';
  }

  getMemberId(member: string | AuthUser): string {
    if (typeof member === 'string') {
      return member;
    }
    return member?._id || '';
  }

  getOtherMembers(team: Team): (string | AuthUser)[] {
    const captainId = typeof team.captain === 'string' ? team.captain : team.captain._id;
    return team.members.filter(m => {
      const memberId = typeof m === 'string' ? m : m._id;
      return memberId !== captainId;
    });
  }

  openTransferDialog(team: Team): void {
    this.selectedTeam.set(team);
    this.selectedNewCaptainId.set(null);
    this.showTransferDialog.set(true);
  }

  closeTransferDialog(): void {
    this.showTransferDialog.set(false);
    this.selectedTeam.set(null);
    this.selectedNewCaptainId.set(null);
  }

  transferCaptain(): void {
    const team = this.selectedTeam();
    const newCaptainId = this.selectedNewCaptainId();
    if (!team || !newCaptainId) return;

    this.transferring.set(true);
    this.error.set(null);
    this.message.set(null);

    this.teamsService.transferCaptain(team._id, newCaptainId).subscribe({
      next: () => {
        this.message.set('Capitaine transféré avec succès');
        this.closeTransferDialog();
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de transférer le capitaine');
      },
      complete: () => this.transferring.set(false)
    });
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role === 'admin';
  }

  canDeleteTeam(team: Team): boolean {
    return this.isAdmin() || this.isCaptain(team);
  }

  deleteTeam(team: Team): void {
    if (!confirm(`Êtes-vous sûr de vouloir dissoudre l'équipe "${team.name}" ? Cette action est irréversible.`)) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.message.set(null);

    this.teamsService.deleteTeam(team._id).subscribe({
      next: () => {
        this.message.set('Équipe dissoute avec succès');
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de dissoudre l\'équipe');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
