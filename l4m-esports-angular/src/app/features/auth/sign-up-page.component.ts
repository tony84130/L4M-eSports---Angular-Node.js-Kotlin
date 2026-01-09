import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sign-up-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './sign-up-page.component.html',
  styleUrl: './sign-up-page.component.scss'
})
export class SignUpPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);
  hidePassword = signal(true);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    gamertag: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
    twitchUsername: [''],
    rememberMe: [true]
  });

  get emailControl() {
    return this.form.controls.email;
  }

  get passwordControl() {
    return this.form.controls.password;
  }

  get firstNameControl() {
    return this.form.controls.firstName;
  }

  get lastNameControl() {
    return this.form.controls.lastName;
  }

  get gamertagControl() {
    return this.form.controls.gamertag;
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update((v) => !v);
  }

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    const { rememberMe, ...payload } = this.form.getRawValue();

    this.error.set(null);
    this.success.set(null);
    this.loading.set(true);

    this.authService
      .signUp(payload, rememberMe)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.success.set('Compte créé, bienvenue sur L4M Esports !');
          this.router.navigateByUrl('/app');
        },
        error: (err) => {
          const message =
            err?.error?.message ??
            err?.message ??
            'Impossible de créer le compte pour le moment.';
          this.error.set(message);
        }
      });
  }
}
