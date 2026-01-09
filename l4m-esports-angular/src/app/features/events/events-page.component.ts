import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EventsService } from '../../core/services/events.service';
import { GamesService } from '../../core/services/games.service';
import { AuthService } from '../../core/services/auth.service';
import { SocketService } from '../../core/services/socket.service';
import { Event, Game } from '../../core/models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-events-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './events-page.component.html',
  styleUrl: './events-page.component.scss'
})
export class EventsPageComponent implements OnInit, OnDestroy {
  events = signal<Event[]>([]);
  games = signal<Game[]>([]);
  loading = signal(true);
  upcoming = signal(true);
  creating = signal(false);
  updating = signal(false);
  generatingBracket = signal<string | null>(null);
  showCreateDialog = signal(false);
  showEditDialog = signal(false);
  selectedEvent = signal<Event | null>(null);
  deleteConfirmId = signal<string | null>(null);
  form = signal<{
    name: string;
    game: string;
    format: string;
    description: string;
    rules: string;
    maxTeams: string;
    locationType: 'online' | 'physical';
    address: string;
    latitude: string;
    longitude: string;
    startDate: string;
    endDate: string;
    registrationStartDate: string;
    registrationEndDate: string;
  }>({
    name: '',
    game: '',
    format: '',
    description: '',
    rules: '',
    maxTeams: '',
    locationType: 'online',
    address: '',
    latitude: '',
    longitude: '',
    startDate: '',
    endDate: '',
    registrationStartDate: '',
    registrationEndDate: ''
  });
  message = signal<string | null>(null);
  error = signal<string | null>(null);
  private readonly authService = inject(AuthService);
  private readonly socketService = inject(SocketService);
  private readonly gamesService = inject(GamesService);
  private socketSubscriptions: Subscription[] = [];

  constructor(private eventsService: EventsService) {}

  ngOnInit(): void {
    // Les admins voient tous les événements par défaut, les autres voient seulement les événements à venir
    const isAdmin = this.authService.currentUser?.role === 'admin';
    this.upcoming.set(!isAdmin);
    this.refresh();
    this.loadGames();
    this.setupSocketListeners();
  }

  loadGames(): void {
    this.gamesService.getGames().subscribe({
      next: (res) => this.games.set(res.data || [])
    });
  }

  ngOnDestroy(): void {
    this.socketSubscriptions.forEach(sub => sub.unsubscribe());
  }

  setupSocketListeners(): void {
    this.socketService.connect();

    // Listen for event updates
    const eventUpdatedSub = this.socketService.on('event:updated').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(eventUpdatedSub);

    // Listen for event creations
    const eventCreatedSub = this.socketService.on('event:created').subscribe(() => {
      this.refresh();
    });
    this.socketSubscriptions.push(eventCreatedSub);

    // Listen for event deletions
    const eventDeletedSub = this.socketService.on('event:deleted').subscribe((data: any) => {
      this.events.update(events => events.filter(e => e._id !== data.eventId));
    });
    this.socketSubscriptions.push(eventDeletedSub);
  }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.message.set(null);
    const isAdmin = this.authService.currentUser?.role === 'admin';
    // Les admins peuvent voir tous les événements, les autres voient seulement les événements à venir
    const params: Record<string, string | number | boolean> | undefined = isAdmin 
      ? (this.upcoming() ? { upcoming: true } : undefined)
      : { upcoming: true };
    // Forcer le rafraîchissement en ajoutant un timestamp unique
    const refreshParams = { ...params, _refresh: Date.now() };
    this.eventsService.getEvents(refreshParams).subscribe({
      next: (res) => {
        const data = (res?.data as any)?.events ?? res?.data ?? [];
        this.events.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erreur lors du chargement des événements');
        this.events.set([]);
        this.loading.set(false);
      }
    });
  }

  toggleUpcoming(value: boolean): void {
    const isAdmin = this.authService.currentUser?.role === 'admin';
    // Seuls les admins peuvent voir tous les événements
    if (!isAdmin && !value) {
      return; // Empêcher les non-admins de voir tous les événements
    }
    this.upcoming.set(value);
    this.refresh();
  }

  isAdmin(): boolean {
    return this.authService.currentUser?.role === 'admin';
  }

  locationLabel(event: Event): string {
    const loc = event.location;
    if (!loc) return 'Lieu non renseigné';
    if (loc.type === 'online') return 'En ligne';
    const parts = [loc.address].filter(Boolean);
    if (loc.coordinates?.coordinates?.length === 2) {
      const [lng, lat] = loc.coordinates.coordinates;
      parts.push(`(${lat.toFixed(4)}, ${lng.toFixed(4)})`);
    }
    return parts.join(' ');
  }

  mapsLink(event: Event): string | null {
    const coords = event.location?.coordinates?.coordinates;
    if (coords && coords.length === 2) {
      const [lng, lat] = coords;
      return `https://www.google.com/maps?q=${lat},${lng}`;
    }
    if (event.location?.address) {
      return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(event.location.address)}`;
    }
    return null;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'draft': 'Brouillon',
      'open': 'Inscriptions ouvertes',
      'registration_closed': 'Inscriptions fermées',
      'in_progress': 'En cours',
      'completed': 'Terminé',
      'cancelled': 'Annulé'
    };
    return labels[status] || status;
  }

  openCreateDialog(): void {
    this.showCreateDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeCreateDialog(): void {
    this.showCreateDialog.set(false);
    this.form.set({
      name: '',
      game: '',
      format: '',
      description: '',
      rules: '',
      maxTeams: '',
      locationType: 'online',
      address: '',
      latitude: '',
      longitude: '',
      startDate: '',
      endDate: '',
      registrationStartDate: '',
      registrationEndDate: ''
    });
    this.error.set(null);
    this.message.set(null);
  }

  createEvent(): void {
    const payload = this.form();
    if (!payload.name || !payload.game || !payload.format || !payload.startDate || !payload.endDate || 
        !payload.registrationStartDate || !payload.registrationEndDate) {
      this.error.set('Tous les champs obligatoires doivent être remplis');
      return;
    }

    this.creating.set(true);
    this.error.set(null);
    this.message.set(null);

    const eventPayload: any = {
      name: payload.name,
      game: payload.game,
      format: payload.format,
      startDate: payload.startDate,
      endDate: payload.endDate,
      registrationStartDate: payload.registrationStartDate,
      registrationEndDate: payload.registrationEndDate,
      description: payload.description || undefined,
      rules: payload.rules || undefined,
      maxTeams: payload.maxTeams ? parseInt(payload.maxTeams) : undefined,
      location: {
        type: payload.locationType
      }
    };

    if (payload.locationType === 'physical') {
      if (payload.address) {
        eventPayload.location.address = payload.address;
      }
      if (payload.latitude && payload.longitude) {
        eventPayload.location.coordinates = {
          latitude: parseFloat(payload.latitude),
          longitude: parseFloat(payload.longitude)
        };
      }
    }

    this.eventsService.createEvent(eventPayload).subscribe({
      next: () => {
        this.message.set('Événement créé avec succès');
        this.closeCreateDialog();
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de créer l\'événement');
      },
      complete: () => this.creating.set(false)
    });
  }

  updateForm(patch: Partial<{
    name: string;
    game: string;
    format: string;
    description: string;
    rules: string;
    maxTeams: string;
    locationType: 'online' | 'physical';
    address: string;
    latitude: string;
    longitude: string;
    startDate: string;
    endDate: string;
    registrationStartDate: string;
    registrationEndDate: string;
  }>): void {
    this.form.set({ ...this.form(), ...patch });
  }

  getSelectedGameFormats(): string[] {
    const gameId = this.form().game;
    const game = this.games().find(g => g._id === gameId);
    return game?.formats || [];
  }

  openEditDialog(event: Event): void {
    this.selectedEvent.set(event);
    const gameId = typeof event.game === 'string' ? event.game : event.game._id;
    this.form.set({
      name: event.name,
      game: gameId,
      format: event.format,
      description: event.description || '',
      rules: event.rules || '',
      maxTeams: event.maxTeams?.toString() || '',
      locationType: event.location?.type || 'online',
      address: event.location?.address || '',
      latitude: event.location?.coordinates?.coordinates?.[1]?.toString() || '', // GeoJSON: [lng, lat] -> lat is index 1
      longitude: event.location?.coordinates?.coordinates?.[0]?.toString() || '', // GeoJSON: [lng, lat] -> lng is index 0
      startDate: new Date(event.startDate).toISOString().slice(0, 16),
      endDate: new Date(event.endDate).toISOString().slice(0, 16),
      registrationStartDate: new Date(event.registrationStartDate).toISOString().slice(0, 16),
      registrationEndDate: new Date(event.registrationEndDate).toISOString().slice(0, 16)
    });
    this.showEditDialog.set(true);
    this.error.set(null);
    this.message.set(null);
  }

  closeEditDialog(): void {
    this.showEditDialog.set(false);
    this.selectedEvent.set(null);
    this.form.set({
      name: '',
      game: '',
      format: '',
      description: '',
      rules: '',
      maxTeams: '',
      locationType: 'online',
      address: '',
      latitude: '',
      longitude: '',
      startDate: '',
      endDate: '',
      registrationStartDate: '',
      registrationEndDate: ''
    });
    this.error.set(null);
    this.message.set(null);
  }

  updateEvent(): void {
    const event = this.selectedEvent();
    if (!event) return;

    const payload = this.form();
    if (!payload.name || !payload.game || !payload.format || !payload.startDate || !payload.endDate || 
        !payload.registrationStartDate || !payload.registrationEndDate) {
      this.error.set('Tous les champs obligatoires doivent être remplis');
      return;
    }

    this.updating.set(true);
    this.error.set(null);
    this.message.set(null);

    const eventPayload: any = {
      name: payload.name,
      game: payload.game,
      format: payload.format,
      startDate: payload.startDate,
      endDate: payload.endDate,
      registrationStartDate: payload.registrationStartDate,
      registrationEndDate: payload.registrationEndDate,
      description: payload.description || undefined,
      rules: payload.rules || undefined,
      maxTeams: payload.maxTeams ? parseInt(payload.maxTeams) : undefined,
      location: {
        type: payload.locationType
      }
    };

    if (payload.locationType === 'physical') {
      if (payload.address) {
        eventPayload.location.address = payload.address;
      }
      if (payload.latitude && payload.longitude) {
        eventPayload.location.coordinates = {
          latitude: parseFloat(payload.latitude),
          longitude: parseFloat(payload.longitude)
        };
      }
    }

    this.eventsService.updateEvent(event._id, eventPayload).subscribe({
      next: () => {
        this.message.set('Événement mis à jour avec succès');
        this.closeEditDialog();
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de mettre à jour l\'événement');
      },
      complete: () => this.updating.set(false)
    });
  }

  deleteEvent(id: string): void {
    this.deleteConfirmId.set(id);
  }

  confirmDelete(): void {
    const id = this.deleteConfirmId();
    if (!id) return;

    this.loading.set(true);
    this.error.set(null);
    this.message.set(null);

    this.eventsService.deleteEvent(id).subscribe({
      next: () => {
        this.message.set('Événement supprimé avec succès');
        this.deleteConfirmId.set(null);
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de supprimer l\'événement');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  cancelDelete(): void {
    this.deleteConfirmId.set(null);
  }

  generateBracket(eventId: string): void {
    this.generatingBracket.set(eventId);
    this.error.set(null);
    this.message.set(null);

    this.eventsService.generateBracket(eventId).subscribe({
      next: () => {
        this.message.set('Bracket généré avec succès');
        this.generatingBracket.set(null);
        this.refresh();
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible de générer le bracket');
        this.generatingBracket.set(null);
      }
    });
  }
}
