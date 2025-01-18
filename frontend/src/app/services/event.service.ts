import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Event } from '../models/event.interface';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createEvent(eventData: FormData): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/admin/event/create`, eventData);
  }

  createEventDraft(eventData: FormData): Observable<Event> {
	  return this.http.post<Event>(`${this.apiUrl}/promoter/event/draft/create`, eventData);
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/generic/service/event/${id}`);
  }

  getEventDraft(id: number): Observable<Event> {
	  return this.http.get<Event>(`${this.apiUrl}/promoter/event/draft/${id}`);
  }


  updateEvent(id: number, eventData: FormData): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/admin/event/update`, eventData);
  }

  updateEventDraft(id: number, eventData: FormData): Observable<Event> {
	  return this.http.put<Event>(`${this.apiUrl}/promoter/event/draft/update`, eventData);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/event/${id}`);
  }

  getPublicEvents(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/generic/service/event/list`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    });
  }

  //per aggiungere un promoter a un evento esistente
  addPromoterToEventDraft(eventDraftId: number, promoterId: number): Observable<Event> {
    return this.http.post<Event>(
      `${this.apiUrl}/promoter/event/draft/${eventDraftId}/add-promoter/${promoterId}`,
      {}
    );
  }
  // per inviare un evento in revisione
  submitForReview(eventId: number): Observable<Event> {
    return this.http.post<Event>(
      `${this.apiUrl}/promoter/event/draft/${eventId}/review`,
      {}
    );
  }
  //per ottenere gli eventi di un promoter con paginazione
  getPromoterEvents(promoterId: number, page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/generic/service/promoter/${promoterId}/events`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    });
  }
}

