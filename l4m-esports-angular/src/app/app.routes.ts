import { Routes } from '@angular/router';
import { LoginPageComponent } from './features/auth/login-page.component';
import { SignUpPageComponent } from './features/auth/sign-up-page.component';
import { AppLayoutComponent } from './features/layout/app-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { GamesPageComponent } from './features/games/games-page.component';
import { EventsPageComponent } from './features/events/events-page.component';
import { TeamsPageComponent } from './features/teams/teams-page.component';
import { MatchesPageComponent } from './features/matches/matches-page.component';
import { NotificationsPageComponent } from './features/notifications/notifications-page.component';
import { UsersPageComponent } from './features/users/users-page.component';
import { ProfilePageComponent } from './features/profile/profile-page.component';

export const routes: Routes = [
  { path: '', component: LoginPageComponent },
  { path: 'sign-up', component: SignUpPageComponent },
  {
    path: 'app',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardPageComponent },
      { path: 'games', component: GamesPageComponent },
      { path: 'events', component: EventsPageComponent },
      { path: 'teams', component: TeamsPageComponent },
      { path: 'matches', component: MatchesPageComponent },
      { path: 'notifications', component: NotificationsPageComponent },
      { path: 'users', component: UsersPageComponent },
      { path: 'profile', component: ProfilePageComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
