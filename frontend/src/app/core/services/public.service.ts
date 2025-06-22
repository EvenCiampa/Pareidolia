import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {PromoterDTO} from '../models/promoter.interface';
import {Page} from '../models/page.interface';
import {catchError} from 'rxjs/operators';
import {ErrorService} from './error.service';
import {EventDTO} from '../models/event.interface';

@Injectable({
  providedIn: 'root'
})
export class PublicService {
  private apiUrl = environment.apiUrl + '/generic/service';

  constructor(private http: HttpClient, private errorService: ErrorService) {
  }

  getPromoter(id: number): Observable<PromoterDTO> {
    return this.http.get<PromoterDTO>(`${this.apiUrl}/promoter/${id}`)
            .pipe(catchError(err => this.errorService.handleError(err)));
  }

  getPromoters(page: number = 0, size: number = 10): Observable<Page<PromoterDTO>> {
    return this.http.get<Page<PromoterDTO>>(`${this.apiUrl}/promoter/list`, {
      params: {page: page.toString(), size: size.toString()}
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getEvent(id: number): Observable<EventDTO> {
    return this.http.get<EventDTO>(`${this.apiUrl}/event/${id}`)
            .pipe(catchError(err => this.errorService.handleError(err)));
  }

  getPublicEvents(page: number = 0, size: number = 10): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/event/list`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  getPromoterEvents(promoterId: number, page: number = 0, size: number = 10): Observable<Page<EventDTO>> {
    return this.http.get<Page<EventDTO>>(`${this.apiUrl}/promoter/${promoterId}/events`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }
}