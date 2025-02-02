import {Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {throwError} from 'rxjs';
import {ErrorResponse} from '../models/error.interface';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  handleError(error: HttpErrorResponse) {
    const errorResponse: ErrorResponse = error.error;
    return throwError(() => errorResponse);
  }
}