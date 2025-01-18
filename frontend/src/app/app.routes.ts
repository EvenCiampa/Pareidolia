import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './home/home.component';
import { PromoterProfileComponent } from './promoter/promoter-profile.component';

export const routes: Routes = [
	{
		path: 'login',
		component: LoginComponent
	},
	{
		path: 'signup',
		component: SignupComponent
	},
	{
		path: 'home',
		component: HomeComponent
	},
	{
		path: 'promoter/:id',
		component: PromoterProfileComponent
	},
	{
		path: '',
		redirectTo: '/home',
		pathMatch: 'full'
	}
];
