import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {RegistrationDTO} from '../../../../core/models/registration.interface';
import {AdminService} from '../../../../core/services/admin.service';
import {AccountDTO} from '../../../../core/models/account.interface';
import {AuthService} from '../../../../core/services/auth.service';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'app-admin-form',
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
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-form.component.html',
  styleUrls: ['./admin-form.component.css'],
})
export class AdminFormComponent implements OnInit {
  loading = false;
  mainLoading = false;
  signupForm: FormGroup;
  accountId: number | null = null;
  admin: AccountDTO | null = null;
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
          private adminService: AdminService,
          private route: ActivatedRoute,
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

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.accountId = +id;
      this.mainLoading = true;

      this.signupForm.removeControl('password');
      this.signupForm.removeControl('confirmPassword');

      this.adminService.getAccount(this.accountId).subscribe({
        next: (account) => {
          this.admin = account;
          this.resetForm();
          this.mainLoading = false;
        },
        error: (error: any) => {
          console.error('Error getting profile:', error);
          this.router.navigate(['/admin/admins']);
          this.mainLoading = false;
        }
      });
    }

  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      this.loading = true;

      let action;
      if (this.accountId == null) {
        const registration: RegistrationDTO = {
          email: this.signupForm.value.email,
          password: this.signupForm.value.password,
          name: this.signupForm.value.name,
          surname: this.signupForm.value.surname,
          phone: this.signupForm.value.phone,
        };
        action = this.adminService.createAccount(registration, "ADMIN");
      } else {
        const accountDTO: AccountDTO = {
          id: this.accountId,
          email: this.signupForm.value.email,
          name: this.signupForm.value.name,
          surname: this.signupForm.value.surname,
          phone: this.signupForm.value.phone,
          referenceType: "ADMIN",
        };
        action = this.adminService.updateAccount(accountDTO);
      }
      action.subscribe({
        next: (account) => {
          this.snackBar.open(this.accountId != null ? 'Admin created!' : 'Admin updated!', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });
          if ("authToken" in account && account.authToken) {
            this.authService.setToken(account.authToken as string, account.referenceType).subscribe({
              next: () => {
                if (this.accountId == null) {
                  this.router.navigate(['/admin/admins/' + account.id]);
                } else {
                  this.admin = account as AccountDTO;
                  this.resetForm();
                }

                this.loading = false;
              },
              error: (error: any) => {
                console.error('Error getting profile:', error);

                if (this.accountId == null) {
                  this.router.navigate(['/admin/admins/' + account.id]);
                } else {
                  this.admin = account as AccountDTO;
                  this.resetForm();
                }

                this.loading = false;
              }
            });
          }
        },
        error: (error) => {
          console.error(this.accountId != null ? 'Creation failed' : 'Update failed', error);
          this.formError = error?.message || ((this.accountId != null ? 'Creation failed' : 'Update failed') + '. Please try again.');
          this.loading = false;
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

  resetForm() {
    this.signupForm.patchValue({
      name: this.admin?.name ?? "",
      surname: this.admin?.surname ?? "",
      email: this.admin?.email ?? "",
      phone: this.admin?.phone ?? "",
    });
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