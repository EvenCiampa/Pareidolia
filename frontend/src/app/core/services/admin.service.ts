import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {PromoterDTO} from '../models/promoter.interface';
import {EventDTO, EventUpdateDTO} from '../models/event.interface';
import {BookingDTO} from '../models/booking.interface';
import {MessageDTO} from '../models/message.interface';
import {RegistrationDTO} from '../models/registration.interface';
import {Page} from '../models/page.interface';
import {ErrorService} from './error.service';
import {AccountDTO, AccountLoginDTO} from '../models/account.interface';
import {PasswordUpdateDTO} from '../models/login.interface';
import {AuthService} from './auth.service';
import {ConsumerDTO} from '../models/consumer.interface';
import {ReviewDTO} from '../models/review.interface';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl + '/admin';

  constructor(
          private http: HttpClient,
          private authService: AuthService,
          private errorService: ErrorService
  ) {
  }

  update(accountDTO: AccountDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${this.apiUrl}/update`, accountDTO, {
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

  // Admin management
  getAccount(id: number): Observable<AccountDTO> {
    return this.http.get<AccountLoginDTO>(`${this.apiUrl}/data/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getAdmins(page: number = 0, size: number = 20): Observable<Page<AccountDTO>> {
    return this.http.get<Page<AccountDTO>>(`${this.apiUrl}/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createAccount(registrationDTO: RegistrationDTO, referenceType: 'CONSUMER' | 'PROMOTER' | 'ADMIN'): Observable<AccountDTO> {
    return this.http.post<AccountDTO>(`${this.apiUrl}/create/${referenceType}`, registrationDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updateAccount(accountDTO: AccountDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${this.apiUrl}/update`, accountDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Consumer management
  updateConsumer(consumerDTO: ConsumerDTO): Observable<ConsumerDTO> {
    return this.http.post<ConsumerDTO>(`${this.apiUrl}/consumer/update`, consumerDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteConsumer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/consumer/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getConsumers(page: number = 0, size: number = 20): Observable<Page<ConsumerDTO>> {
    return this.http.get<Page<ConsumerDTO>>(`${this.apiUrl}/consumer/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getConsumer(id: number): Observable<ConsumerDTO> {
    return this.http.get<ConsumerDTO>(`${this.apiUrl}/consumer/${id}/data`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Promoter management
  createPromoter(registrationDTO: RegistrationDTO): Observable<PromoterDTO> {
    return this.http.post<PromoterDTO>(`${this.apiUrl}/promoter/create`, registrationDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updatePromoter(promoterDTO: PromoterDTO): Observable<PromoterDTO> {
    return this.http.post<PromoterDTO>(`${this.apiUrl}/promoter/update`, promoterDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deletePromoter(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/promoter/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updatePromoterImage(promoterId: number, file: File): Observable<PromoterDTO> {
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<PromoterDTO>(`${this.apiUrl}/promoter/${promoterId}/image`, formData, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deletePromoterImage(promoterId: number): Observable<PromoterDTO> {
    return this.http.delete<PromoterDTO>(`${this.apiUrl}/promoter/${promoterId}/image`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Event management
  getEvent(id: number): Observable<EventDTO> {
    return this.http.get<EventDTO>(`${this.apiUrl}/event/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getEvents(page: number = 0, size: number = 20, state?: 'DRAFT' | 'REVIEW' | 'PUBLISHED'): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/event/list`, {
      params: {page: page.toString(), size: size.toString(), ...(state != null ? {state: state} : {})},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getPublicEvents(page: number = 0, size: number = 10): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/event/list`, {
      params: {page: page.toString(), size: size.toString(), state: 'PUBLISHED'},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createEvent(eventDTO: EventUpdateDTO): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/create`, eventDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updateEvent(eventDTO: EventUpdateDTO): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/update`, eventDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/event/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  moveBackwards(eventId: number): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/${eventId}/backwards`, {}, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  moveForward(eventId: number): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/${eventId}/forward`, {}, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updateEventImage(eventId: number, file: File): Observable<EventDTO> {
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<EventDTO>(`${this.apiUrl}/event/${eventId}/image`, formData, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteEventImage(eventId: number): Observable<EventDTO> {
    return this.http.delete<EventDTO>(`${this.apiUrl}/event/${eventId}/image`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Review management
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

  // Message management
  getMessages(eventId: number, page: number = 0, size: number = 20): Observable<Page<MessageDTO>> {
    return this.http.get<Page<MessageDTO>>(`${this.apiUrl}/message/${eventId}/list`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createMessage(eventId: number, message: string): Observable<MessageDTO> {
    return this.http.post<MessageDTO>(`${this.apiUrl}/message/${eventId}/create`, message, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteMessage(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/message/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Booking management
  getBooking(id: number): Observable<BookingDTO> {
    return this.http.get<BookingDTO>(`${this.apiUrl}/booking/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getBookings(eventId: number, page: number = 0, size: number = 20): Observable<Page<BookingDTO>> {
    return this.http.get<Page<BookingDTO>>(`${this.apiUrl}/booking/event/${eventId}`, {
      params: {page: page.toString(), size: size.toString()},
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
}