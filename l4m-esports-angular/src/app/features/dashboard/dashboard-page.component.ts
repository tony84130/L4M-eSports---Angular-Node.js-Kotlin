import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { GamesService } from '../../core/services/games.service';
import { EventsService } from '../../core/services/events.service';
import { Game, Event } from '../../core/models';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit {
  games = signal<Game[]>([]);
  events = signal<Event[]>([]);
  loading = signal(true);

  constructor(private gamesService: GamesService, private eventsService: EventsService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.gamesService.getGames().subscribe({
      next: (res) => this.games.set(res.data || []),
      error: () => this.loading.set(false),
      complete: () => this.loading.set(false)
    });

    this.eventsService.getEvents({ upcoming: true }).subscribe({
      next: (res) => this.events.set(res.data || []),
      error: () => {},
      complete: () => {}
    });
  }
}
