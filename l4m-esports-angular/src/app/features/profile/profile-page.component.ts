import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsersService } from '../../core/services/users.service';
import { AuthUser } from '../../core/models';
import { TwitchService, TwitchUser } from '../../core/services/twitch.service';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss'
})
export class ProfilePageComponent implements OnInit {
  user = signal<AuthUser | null>(null);
  loading = signal(true);
  saving = signal(false);
  twitchPreview = signal<TwitchUser | null>(null);
  twitchMessage = signal<string | null>(null);
  twitchInput = '';

  constructor(private usersService: UsersService, private twitchService: TwitchService) {}

  ngOnInit(): void {
    this.usersService.getMe().subscribe({
      next: (res) => this.user.set(res.data),
      complete: () => {
        this.twitchInput = this.user()?.twitchUsername ?? '';
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  fetchTwitch(): void {
    const username = (this.twitchInput || '').trim();
    if (!username) {
      this.twitchPreview.set(null);
      this.twitchMessage.set('Renseigne un username Twitch.');
      return;
    }
    this.twitchMessage.set('Recherche Twitch…');
    this.twitchService.getUser(username).subscribe({
      next: (res) => {
        this.twitchPreview.set(res.data);
        this.twitchMessage.set('Utilisateur trouvé. Sauvegarde pour lier.');
      },
      error: () => {
        this.twitchPreview.set(null);
        this.twitchMessage.set('Utilisateur Twitch introuvable.');
      }
    });
  }

  saveTwitch(): void {
    const username = (this.twitchInput || '').trim();
    this.saving.set(true);
    this.usersService.updateMe({ twitchUsername: username || undefined }).subscribe({
      next: (res) => {
        this.user.set(res.data);
        this.twitchMessage.set(username ? 'Compte Twitch lié.' : 'Compte Twitch retiré.');
      },
      error: () => this.twitchMessage.set('Impossible de sauvegarder pour le moment.'),
      complete: () => this.saving.set(false)
    });
  }
}
