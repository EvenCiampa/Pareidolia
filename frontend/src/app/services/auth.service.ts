import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

interface LoginResponse {
	token: string;
}

interface RegistrationData {
	email: string;
	password: string;
	name: string;
	surname: string;
	phone: string;
}

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = environment.authEndpoint;

	constructor(private http: HttpClient) {}

	login(email: string, password: string): Observable<any> {
		return this.http.post(`${this.apiUrl}/login`, { email, password });
	}

	logout(): void {
		localStorage.removeItem('token');
	}

	isLoggedIn(): boolean {
		return !!localStorage.getItem('token');
	}

	signup(registrationData: RegistrationData): Observable<any> {
		return this.http.post(`${this.apiUrl}/register`, registrationData);
	}

	forgotPassword(email: string): Observable<void> {
		return this.http.post<void>(`${this.apiUrl}/forgotPassword`, null, {
			params: {email}
		});
	}
}
