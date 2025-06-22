import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {ConsumerDTO} from '../models/consumer.interface';
import {BookingDTO} from '../models/booking.interface';
import {ReviewDTO} from '../models/review.interface';
import {Page} from '../models/page.interface';
import {ErrorService} from './error.service';
import {PasswordUpdateDTO} from '../models/login.interface';
import {AccountLoginDTO} from '../models/account.interface';
import {AuthService} from './auth.service';
import {EventDTO} from '../models/event.interface';

@Injectable({
  providedIn: 'root'
})
export class ConsumerService {
  private apiUrl = environment.apiUrl + '/consumer';

  constructor(private http: HttpClient,
              private authService: AuthService,
              private errorService: ErrorService) {
  }

  update(consumerDTO: ConsumerDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${this.apiUrl}/update`, consumerDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updatePassword(passwordUpdateDTO: PasswordUpdateDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${this.apiUrl}/update/password`, passwordUpdateDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Booking related endpoints
  getBooking(id: number): Observable<BookingDTO> {
    return this.http.get<BookingDTO>(`${this.apiUrl}/booking/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getBookings(page: number = 0, size: number = 20): Observable<Page<BookingDTO>> {
    return this.http.get<Page<BookingDTO>>(`${this.apiUrl}/booking/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createBooking(eventId: number): Observable<BookingDTO> {
    return this.http.post<BookingDTO>(`${this.apiUrl}/booking/${eventId}`, {}, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteBooking(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/booking/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteBookingByEventId(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/booking/event/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Event related endpoints
  getEvent(id: number): Observable<EventDTO> {
    return this.http.get<EventDTO>(`${this.apiUrl}/event/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getPublicEvents(page: number = 0, size: number = 10): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/event/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Review related endpoints
  getEventReviews(eventId: number, page: number = 0, size: number = 10): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(`${this.apiUrl}/review/${eventId}/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createReview(reviewDTO: ReviewDTO): Observable<ReviewDTO> {
    return this.http.post<ReviewDTO>(`${this.apiUrl}/review/create`, reviewDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }
}