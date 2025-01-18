import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserProfile, PromoterInfo } from '../models/user.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getUserProfile(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/users/${id}`);
  }

  updateProfile(formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/profile`, formData);
  }

  updatePromoterInfo(id: number, promoterInfo: Partial<PromoterInfo>): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${id}/promoter-info`, promoterInfo);
  }
} 