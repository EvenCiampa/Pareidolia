import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {SignupComponent} from './signup/signup.component';
import {ForgotPasswordComponent} from './forgot-pass/forgot-pass.component';
import {SharedModule} from '../../shared/shared.module';
import {NoAuthGuard} from '../../core/guards/no-auth.guard';

const routes: Routes = [
  {path: 'login', component: LoginComponent, canActivate: [NoAuthGuard]},
  {path: 'signup', component: SignupComponent, canActivate: [NoAuthGuard]},
  {path: 'forgot', component: ForgotPasswordComponent, canActivate: [NoAuthGuard]}
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule.forChild(routes),
    LoginComponent,
    SignupComponent,
    ForgotPasswordComponent
  ]
})
export class AuthModule {
}