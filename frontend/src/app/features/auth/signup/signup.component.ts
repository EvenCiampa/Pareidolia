import {Component} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {AuthService} from '../../../core/services/auth.service';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {MatSnackBar} from '@angular/material/snack-bar';
import {RegistrationDTO} from '../../../core/models/registration.interface';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
    NgOptimizedImage
  ]
})
export class SignupComponent {
  signupForm: FormGroup;
  hidePassword = true;
  formError: string | null = null;
  passwordValidations = {
    minLength: false,
    hasNumber: false,
    hasUpper: false,
    hasLower: false,
    hasSpecial: false,
    noSpaces: false
  };

  constructor(
          private formBuilder: FormBuilder,
          private authService: AuthService,
          private router: Router,
          private snackBar: MatSnackBar
  ) {
    this.signupForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$")]],
      confirmPassword: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$"), this.passwordMatchValidator]],
      name: ['', Validators.required],
      surname: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern("^[+]?[0-9 \\-().]{5,32}$")]]
    });

    this.signupForm.valueChanges.subscribe(() => {
      this.formError = null;
    });

    // Add password validation tracking
    this.signupForm.get('password')?.valueChanges.subscribe(value => {
      this.updatePasswordValidations(value);
    });
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      const registration: RegistrationDTO = {
        email: this.signupForm.value.email,
        password: this.signupForm.value.password,
        name: this.signupForm.value.name,
        surname: this.signupForm.value.surname,
        phone: this.signupForm.value.phone,
      };

      this.authService.register(registration).subscribe({
        next: () => {
          this.snackBar.open('Registration successful! Please login.', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/auth/login']);
        },
        error: (error) => {
          console.error('Registration failed', error);
          this.formError = error?.message || 'Registration failed. Please try again.';
        }
      });
    }
  }

  getErrorMessage(controlName: string, controlLabel: string): string {
    const control = this.signupForm.get(controlName);
    if (control?.hasError('required')) {
      return `${controlLabel} is required`;
    }
    if (control?.hasError('email')) {
      return 'Invalid email format';
    }
    if (controlName === 'password' || controlName === 'confirmPassword') {
      if (control?.hasError('pattern')) {
        return 'Please enter a valid password';
      }
    }
    if (controlName === 'phone' && control?.hasError('pattern')) {
      return 'Please enter a valid phone number';
    }
    if (controlName === 'confirmPassword' && control?.hasError('mismatch')) {
      return 'Passwords do not match';
    }
    return '';
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.parent?.get('password');
    const confirmPassword = control.parent?.get('confirmPassword');
    return password?.value == confirmPassword?.value ? null : {'mismatch': true};
  }

  private updatePasswordValidations(password: string) {
    this.passwordValidations = {
      minLength: password.length >= 8,
      hasNumber: /\d/.test(password),
      hasUpper: /[A-Z]/.test(password),
      hasLower: /[a-z]/.test(password),
      hasSpecial: /[@#$%^&+=\-!]/.test(password),
      noSpaces: !/\s/.test(password)
    };
  }
}