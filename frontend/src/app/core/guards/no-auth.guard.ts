import {Injectable} from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {lastValueFrom} from 'rxjs';
import {AuthService} from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class NoAuthGuard implements CanActivate {
  constructor(
          private authService: AuthService,
          private router: Router
  ) {
  }

  async canActivate(): Promise<boolean | UrlTree> {
    try {
      await lastValueFrom(this.authService.initialized);
    } catch (_) {
      // ignore
    }
    if (!this.authService.getCurrentUserSubject()) {
      return true;
    } else {
      return this.router.createUrlTree(['/']);
    }
  }
}