import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AiService } from '../../core/services/ai.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-ai-help-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-help-widget.component.html',
  styleUrl: './ai-help-widget.component.scss'
})
export class AiHelpWidgetComponent {
  isOpen = false;
  loading = false;
  questionText = '';
  answer = '';
  error = '';

  constructor(
    private ai: AiService,
    private auth: AuthService,
    private router: Router
  ) {}

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (!this.isOpen) {
      this.loading = false;
      this.answer = '';
      this.error = '';
    }
  }

  ask(): void {
    const q = this.questionText.trim();
    if (!q) {
      this.error = 'Pose une question.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.answer = '';

    const context = {
      page: this.router.url,
      role: this.auth.currentUser?.role ?? 'anonymous'
    };

    this.ai.ask(q, context).subscribe({
      next: (res) => {
        this.answer = res.data?.answer ?? 'Pas de réponse pour le moment.';
      },
      error: () => {
        this.error = "Impossible de joindre l'assistant.";
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
