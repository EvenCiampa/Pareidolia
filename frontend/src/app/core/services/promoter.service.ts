import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {PromoterDTO} from '../models/promoter.interface';
import {EventDTO, EventUpdateDTO} from '../models/event.interface';
import {BookingDTO} from '../models/booking.interface';
import {MessageDTO} from '../models/message.interface';
import {Page} from '../models/page.interface';
import {catchError} from 'rxjs/operators';
import {ErrorService} from './error.service';
import {PasswordUpdateDTO} from '../models/login.interface';
import {AccountLoginDTO} from '../models/account.interface';
import {AuthService} from './auth.service';
import {ReviewDTO} from '../models/review.interface';

@Injectable({
  providedIn: 'root'
})
export class PromoterService {
  private apiUrl = environment.apiUrl + '/promoter';

  constructor(
          private http: HttpClient,
          private authService: AuthService,
          private errorService: ErrorService
  ) {
  }

  update(promoterDTO: PromoterDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${this.apiUrl}/update`, promoterDTO, {
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

  // Event related endpoints
  getEvents(page: number = 0, size: number = 10): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/event/list`, {
      params: {page: page.toString(), size: size.toString()},
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

  getEvent(id: number): Observable<EventDTO> {
    return this.http.get<EventDTO>(`${this.apiUrl}/event/${id}`, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  createEventDraft(eventDTO: EventUpdateDTO): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/create`, eventDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  updateEventDraft(eventDTO: EventUpdateDTO): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/update`, eventDTO, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  addPromoterToEventDraft(eventId: number, promoterId: number): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/${eventId}/add-promoter/${promoterId}`, {}, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

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

  submitForReview(eventId: number): Observable<EventDTO> {
    return this.http.post<EventDTO>(`${this.apiUrl}/event/${eventId}/review`, {}, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Booking related endpoints
  getEventBookings(eventId: number, page: number = 0, size: number = 20): Observable<Page<BookingDTO>> {
    return this.http.get<Page<BookingDTO>>(`${this.apiUrl}/booking/event/${eventId}`, {
      params: {page: page.toString(), size: size.toString()},
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  // Message related endpoints
  getMessages(eventId: number, page: number = 0, size: number = 10): Observable<Page<MessageDTO>> {
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

  updateImage(file: File): Observable<PromoterDTO> {
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<PromoterDTO>(`${this.apiUrl}/update/image`, formData, {
      headers: {
        "Authorization": `Bearer ${this.authService.getToken()}`
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  deleteImage(): Observable<PromoterDTO> {
    return this.http.delete<PromoterDTO>(`${this.apiUrl}/image`, {
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
}