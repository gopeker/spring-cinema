import { Component, inject, ChangeDetectorRef, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChatbotService } from '../../core/services/chatbot.service';
import { MovieService } from '../../core/services/movie.service';

interface Message {
  role: 'user' | 'bot';
  content: string;
  movies?: { id: number; title: string }[];
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css'
})
export class ChatbotComponent implements AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  private cdr = inject(ChangeDetectorRef);
  messages: Message[] = [];
  userInput = '';
  loading = false;
  private allMovies: { id: number; title: string }[] = [];
  private shouldScroll = false;

  constructor(
    private chatbotService: ChatbotService,
    private movieService: MovieService
  ) {
    this.messages.push({
      role: 'bot',
      content: 'Hi! I\'m your movie recommendation assistant. Tell me what kind of movies you like, and I\'ll suggest some great films for you to watch at Spring Cinema!'
    });
    this.shouldScroll = true;
    
    this.movieService.getMovies().subscribe({
      next: (movies) => {
        this.allMovies = movies.map(m => ({ id: m.id, title: m.title }));
      }
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) {}
  }

  sendMessage(): void {
    if (!this.userInput.trim() || this.loading) return;

    const userMessage = this.userInput.trim();
    this.messages.push({ role: 'user', content: userMessage });
    this.shouldScroll = true;
    this.userInput = '';
    this.loading = true;

    this.chatbotService.chat(userMessage).subscribe({
      next: (res: any) => {
        const responseText = res?.response || res?.message || 'No response';
        const movies = this.extractMovies(responseText);
        this.messages.push({ role: 'bot', content: responseText, movies });
        this.shouldScroll = true;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Chat error:', err);
        this.messages.push({ role: 'bot', content: 'Sorry, I\'m having trouble connecting. Make sure LM Studio is running with Gemma 4 loaded.' });
        this.shouldScroll = true;
        this.loading = false;
        this.cdr.detectChanges();
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