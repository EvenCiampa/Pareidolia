import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

@Component({
	selector: 'app-forgot-password',
	templateUrl: './forgot-pass.component.html',
	styleUrls: ['./forgot-pass.component.css'],
	standalone: true,
	imports: [
		ReactiveFormsModule,
		MatCardModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		RouterModule
	]
})
export class ForgotPasswordComponent {
	forgotPasswordForm: FormGroup;
	logoUrl: string = 'assets/images/logo.png';

	constructor(
			private formBuilder: FormBuilder,
			private authService: AuthService,
			private router: Router
	) {
		this.forgotPasswordForm = this.formBuilder.group({
			email: ['', [Validators.required, Validators.email]]
		});
	}

	onSubmit(): void {
		if (this.forgotPasswordForm.valid) {
			const { email } = this.forgotPasswordForm.value;

			this.authService.forgotPassword(email).subscribe({
				next: () => {
					console.log('Password reset email sent');
					alert('If the email exists, a password reset link has been sent.');
					this.router.navigate(['/login']);
				},
				error: (error) => {
					console.error('Password reset failed', error);
					alert('Password reset failed. Please try again.');
				}
			});
		}
	}

	getErrorMessage(controlName: string): string {
		const control = this.forgotPasswordForm.get(controlName);
		if (control?.hasError('required')) {
			return 'Email is required';
		}
		if (control?.hasError('email')) {
			return 'Invalid email format';
		}
		return '';
	}
}
