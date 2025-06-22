import {NgModule, Optional, SkipSelf} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from './interceptors/error.interceptor';
import {AuthGuard} from './guards/auth.guard';
import {ErrorService} from './services/error.service';
import {AuthService} from './services/auth.service';

@NgModule({
  imports: [
    CommonModule
  ],
  providers: [
    AuthGuard,
    ErrorService,
    AuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only.');
    }
  }
}