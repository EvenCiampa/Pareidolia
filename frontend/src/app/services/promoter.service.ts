import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserProfile } from '../models/user.interface';

@Injectable({
  providedIn: 'root'
})
export class PromoterService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Promoter Controller
  getPromoterData(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/promoter/data`);
  }

  updatePromoter(promoterData: FormData): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.apiUrl}/promoter/update`, promoterData);
  }

  // Public Controller
  getPromoterProfile(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/generic/service/promoter/${id}`);
  }

  getPromotersList(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/generic/service/promoter/list`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    });
  }

  // Admin Promoter Controller
  createPromoter(registrationData: any): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.apiUrl}/admin/promoter/create`, registrationData);
  }

  adminUpdatePromoter(promoterData: any): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.apiUrl}/admin/promoter/update`, promoterData);
  }

  deletePromoter(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/promoter/${id}`);
  }
}