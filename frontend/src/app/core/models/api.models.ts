// ─── Auth ────────────────────────────────────────────────────────────────────

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

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

// ─── Admin DTOs ──────────────────────────────────────────────────────────────

export interface MovieDto {
  id: number;
  title: string;
  description?: string;
  duration: number;
  posterUrl?: string;
}

export interface MovieCreateRequest {
  title: string;
  description?: string;
  duration: number;
  posterUrl?: string;
}

export interface ShowroomDto {
  id: number;
  name: string;
  rows: number;
  seatsPerRow: number;
  totalSeats: number;
}

export interface ScreeningDto {
  id: number;
  movie: MovieDto;
  showroom: ShowroomDto;
  startTime: string;
  basePrice: number;
}

export interface ScreeningCreateRequest {
  movieId: number;
  showroomId: number;
  startTime: string;
  basePrice: number;
}

export interface AddressDto {
  street?: string;
  city?: string;
  postalCode?: string;
  country?: string;
}

export interface PaymentDetailsDto {
  cardHolderName?: string;
  cardLastFour?: string;
  cardExpiry?: string;
}

export interface UserDto {
  id: number;
  name: string;
  email: string;
  role: string;
  address?: AddressDto;
  paymentDetails?: PaymentDetailsDto;
}

export interface UserCreateRequest {
  name: string;
  email: string;
  password: string;
  role?: string;
  address?: AddressDto;
  paymentDetails?: PaymentDetailsDto;
}

export interface UserUpdateRequest {
  name: string;
  email: string;
  password?: string;
  role?: string;
  address?: AddressDto;
  paymentDetails?: PaymentDetailsDto;
}

export interface UserSearchRequest {
  name?: string;
  email?: string;
  street?: string;
  city?: string;
  postalCode?: string;
  country?: string;
}

export interface MovieSearchParams {
  title?: string;
  description?: string;
  minDuration?: number;
  maxDuration?: number;
}

export interface ScreeningSearchParams {
  movieId?: number;
  from?: string;
  to?: string;
  minPrice?: number;
  maxPrice?: number;
}
