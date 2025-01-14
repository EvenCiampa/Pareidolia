/* import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

}

 */

import { Component , OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ImageLoaderService } from '../image-loader.service';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
	loginForm: FormGroup;
	hidePassword = true;
	logoUrl: string = 'assets/images/logo.png';

	constructor(private formBuilder: FormBuilder,private imageLoader: ImageLoaderService) {
		this.loginForm = this.formBuilder.group({
			email: ['', [Validators.required, Validators.email]],
			password: ['', [Validators.required, Validators.minLength(6)]]
		});
	}

	ngOnInit(): void {}

	onSubmit(): void {
		if (this.loginForm.valid) {
			const credentials = {
				email: this.loginForm.get('email')?.value,
				password: this.loginForm.get('password')?.value
			};

			this.authService.login(credentials.email, credentials.password)
				.subscribe({
					next: (response) => {
						console.log('Login successful', response);
						this.router.navigate(['/dashboard']);
					},
					error: (error) => {
						console.error('Login failed', error);
						// Gestisci l'errore (es. mostra un messaggio all'utente)
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
			return 'Password must be at least 6 characters';
		}
		return '';
	}
}
