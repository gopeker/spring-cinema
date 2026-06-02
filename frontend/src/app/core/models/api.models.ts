// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

/** The user portion stored in AuthService after login. */
export type User = Omit<AuthResponse, 'token'>;

// ─── Movies ──────────────────────────────────────────────────────────────────

export interface Movie {
  id: number;
  title: string;
  description: string;
  duration: number;
  posterUrl: string;
  genre?: string;
}

// ─── Screenings ───────────────────────────────────────────────────────────────

export interface Showroom {
  id: number;
  name: string;
  rows: number;
  seatsPerRow: number;
  totalSeats: number;
}

export interface Screening {
  id: number;
  movie: Movie;
  showroom: Showroom;
  startTime: string;
  basePrice: number;
}

export interface Seat {
  seatRow: string;
  seatNumber: number;
  available: boolean;
  price: number;
  tier: string;
}

// ─── Tickets ─────────────────────────────────────────────────────────────────

export interface Ticket {
  id: number;
  screening: Screening;
  seatRow: string;
  seatNumber: number;
  price: number;
  status: string;
  purchaseTime: string;
}

// ─── Grouped Screenings (UI model) ───────────────────────────────────────────

export interface ScreeningGroup {
  date: string;
  screenings: Screening[];
}
