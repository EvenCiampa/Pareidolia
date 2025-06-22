import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {LoginDTO} from '../models/login.interface';
import {RegistrationDTO} from '../models/registration.interface';
import {ErrorService} from './error.service';
import {AccountDTO, AccountLoginDTO} from '../models/account.interface';
import {ConsumerDTO} from '../models/consumer.interface';
import {PromoterDTO} from '../models/promoter.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  initialized = new BehaviorSubject<null>(null);
  private readonly TOKEN_KEY = 'token';
  private readonly REFERENCE_TYPE_KEY = 'referenceType';
  private currentUserSubject = new BehaviorSubject<ConsumerDTO | PromoterDTO | AccountDTO | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
          private http: HttpClient,
          private errorService: ErrorService
  ) {
    // Initialize user data if token exists
    if (this.isLoggedIn()) {
      this.refreshCurrentUser().subscribe({
        next: () => {
          if (!this.initialized.closed) {
            this.initialized.complete();
          }
        },
        error: () => {
          if (!this.initialized.closed) {
            this.initialized.complete();
          }
        }
      });
    } else {
      this.currentUserSubject.next(null);
      if (!this.initialized.closed) {
        this.initialized.complete();
      }
    }
  }

  getCurrentUserSubject(): ConsumerDTO | PromoterDTO | AccountDTO | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${environment.genericAccessEndpoint}/login`, credentials);
  }

  register(registrationDTO: RegistrationDTO): Observable<AccountLoginDTO> {
    return this.http.post<AccountLoginDTO>(`${environment.genericAccessEndpoint}/register`, registrationDTO)
            .pipe(catchError(err => this.errorService.handleError(err)));
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${environment.genericAccessEndpoint}/forgotPassword`, null, {
      params: {email}
    }).pipe(catchError(err => this.errorService.handleError(err)));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFERENCE_TYPE_KEY);
    // Clear current user data
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getReferenceType(): string | null {
    return localStorage.getItem(this.REFERENCE_TYPE_KEY);
  }

  setToken(token: string, referenceType: string) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.REFERENCE_TYPE_KEY, referenceType);
    // Refresh user data when token is set
    return this.refreshCurrentUser();
  }

  refreshCurrentUser(): Observable<ConsumerDTO | PromoterDTO | AccountDTO | null> {
    const token = this.getToken();
    if (!token) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFERENCE_TYPE_KEY);
      this.currentUserSubject.next(null);
      return of(null);
    }
    const referenceType = this.getReferenceType();
    if (!referenceType) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFERENCE_TYPE_KEY);
      this.currentUserSubject.next(null);
      return of(null);
    }

    let endpoint: string;
    switch (referenceType) {
      case "CONSUMER":
        endpoint = `${environment.consumerEndpoint}/data`;
        break;
      case "PROMOTER":
        endpoint = `${environment.promoterEndpoint}/data`;
        break;
      case "ADMIN":
        endpoint = `${environment.adminEndpoint}/data`;
        break;
      default:
        this.currentUserSubject.next(null);
        return of(null);
    }

    return this.http.get<ConsumerDTO | PromoterDTO | AccountDTO>(endpoint, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    }).pipe(
            tap(user => {
              this.currentUserSubject.next(user);
              console.info("User updated:", user)
            }),
            catchError(err => {
              this.errorService.handleError(err);
              this.currentUserSubject.next(null);
              return of(null);
            })
    );
  }
}
