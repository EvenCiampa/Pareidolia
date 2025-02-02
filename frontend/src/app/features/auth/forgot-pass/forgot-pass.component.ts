import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../../../core/services/auth.service';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {MatSnackBar} from '@angular/material/snack-bar';

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
    RouterModule,
    CommonModule,
    NgOptimizedImage
  ]
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  isSubmitted = false;
  formError: string | null = null;

  constructor(
          private formBuilder: FormBuilder,
          private authService: AuthService,
          private router: Router,
          private snackBar: MatSnackBar
  ) {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });

    // Clear form error when form changes
    this.forgotPasswordForm.valueChanges.subscribe(() => {
      this.formError = null;
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      const {email} = this.forgotPasswordForm.value;
      this.isSubmitted = true;

      this.authService.forgotPassword(email).subscribe({
        next: () => {
          this.snackBar.open('Password reset instructions have been sent to your email.', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/auth/login']);
        },
        error: (error) => {
          console.error('Password reset request failed', error);
          this.formError = 'Unable to process password reset request. Please try again.';
          this.isSubmitted = false;
        }
      });
    }
  }

  getErrorMessage(controlName: string, controlLabel: string): string {
    const control = this.forgotPasswordForm.get(controlName);
    if (control?.hasError('required')) {
      return `${controlLabel} is required`;
    }
    if (control?.hasError('email')) {
      return 'Invalid email format';
    }
    return '';
  }
}
