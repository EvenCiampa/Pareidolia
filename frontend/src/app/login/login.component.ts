import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	imports: [
		ReactiveFormsModule,
		MatCardModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		RouterModule
	],
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
	loginForm: FormGroup;
	hidePassword = true;
	logoUrl: string = 'assets/images/logo.png'; // Percorso al logo

	constructor(
			private formBuilder: FormBuilder,
			private authService: AuthService,
			private router: Router
	) {
		this.loginForm = this.formBuilder.group({
			email: ['', [Validators.required, Validators.email]],
			password: ['', [Validators.required, Validators.minLength(6)]]
		});
	}

	ngOnInit(): void {}

	onSubmit(): void {
		if (this.loginForm.valid) {
			const { email, password } = this.loginForm.value;

			this.authService.login(email, password).subscribe({
				next: (response) => {
					console.log('Login successful', response);
					localStorage.setItem('token', response.token); // Salva il token
					this.router.navigate(['/dashboard']); // Naviga alla dashboard
				},
				error: (error) => {
					console.error('Login failed', error);
					alert('Login failed. Please check your credentials.');
				}
			});
		}
	}

	getErrorMessage(controlName: string): string {
		const control = this.loginForm.get(controlName);
		if (control?.hasError('required')) {
			return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} is required`;
		}
		if (control?.hasError('email')) {
			return 'Invalid email format';
		}
		if (control?.hasError('minlength')) {
			return 'Password must be at least 6 characters long';
		}
		return '';
	}
}
