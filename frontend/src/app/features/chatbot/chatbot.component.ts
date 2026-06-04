import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal, ViewChild, ElementRef, afterNextRender, Injector } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChatbotService, ChatHistoryEntry } from '../../core/services/chatbot.service';
import { MovieService } from '../../core/services/movie.service';

interface Message {
  role: 'user' | 'bot';
  content: string;
  movies?: { id: number; title: string }[];
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css'
})
export class ChatbotComponent {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  private chatbotService = inject(ChatbotService);
  private movieService = inject(MovieService);
  private injector = inject(Injector);
  private destroyRef = inject(DestroyRef);

  messages = signal<Message[]>([{
    role: 'bot',
    content: 'Hi! I\'m your movie recommendation assistant. Tell me what kind of movies you like, and I\'ll suggest some great films for you to watch at Spring Cinema!'
  }]);
  userInput = signal('');
  loading = signal(false);

  private allMovies: { id: number; title: string }[] = [];

  constructor() {
    this.movieService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (movies) => {
        this.allMovies = movies.map(m => ({ id: m.id, title: m.title }));
      }
    });
    this.scheduleScroll();
  }

  private scheduleScroll(): void {
    afterNextRender(() => {
      try {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      } catch { /* messagesContainer not yet available */ }
    }, { injector: this.injector });
  }

  /** Build the history payload for the backend from current messages.
   *  Maps frontend 'bot' role to 'assistant' as the API expects. */
  private buildHistory(): ChatHistoryEntry[] {
    return this.messages()
      .filter(m => m.content !== this.messages()[0].content) // exclude greeting
      .map(m => ({
        role: m.role === 'user' ? 'user' : 'assistant' as const,
        content: m.content
      }));
  }

  sendMessage(): void {
    if (!this.userInput().trim() || this.loading()) return;

    const userMessage = this.userInput().trim();
    const history = this.buildHistory();

    this.messages.update(msgs => {
      const updated: Message[] = [...msgs, { role: 'user', content: userMessage }];
      return updated.length > 100 ? updated.slice(-100) : updated;
    });
    this.scheduleScroll();
    this.userInput.set('');
    this.loading.set(true);

    this.chatbotService.chat(userMessage, history).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => {
        const responseText = res.response || 'No response';
        const movies = this.extractMovies(responseText);
        this.messages.update(msgs => {
          const updated: Message[] = [...msgs, { role: 'bot', content: responseText, movies }];
          return updated.length > 100 ? updated.slice(-100) : updated;
        });
        this.scheduleScroll();
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Chat error:', err);
        this.messages.update(msgs => {
          const updated: Message[] = [...msgs, {
            role: 'bot',
            content: 'Sorry, I\'m having trouble connecting. Make sure LM Studio is running with Gemma 4 loaded.'
          }];
          return updated.length > 100 ? updated.slice(-100) : updated;
        });
        this.scheduleScroll();
        this.loading.set(false);
      }
    });
  }

  private extractMovies(response: string): { id: number; title: string }[] {
    const movies: { id: number; title: string }[] = [];
    for (const movie of this.allMovies) {
      if (response.toLowerCase().includes(movie.title.toLowerCase())) {
        movies.push({ id: movie.id, title: movie.title });
      }
    }
    return movies;
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
