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
import {PromoterDTO} from '../../../../core/models/promoter.interface';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {PublicService} from '../../../../core/services/public.service';
import {MatTabsModule} from '@angular/material/tabs';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {PhotoUploadComponent} from '../../../../shared/components/photo-upload/photo-upload.component';

@Component({
  selector: 'app-promoter-form',
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
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTabsModule,
    MatExpansionModule,
    PhotoUploadComponent
  ],
  templateUrl: './promoter-form.component.html',
  styleUrls: ['./promoter-form.component.css'],
})
export class PromoterFormComponent implements OnInit {
  loading = false;
  mainLoading = false;
  promoter: PromoterDTO | null = null;
  signupForm: FormGroup;
  accountId: number | null = null;
  hidePassword = true;
  imageLoading = false;
  imagePreview: string | null = null;
  originalImage: string | null = null;
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
          private publicService: PublicService,
          private route: ActivatedRoute,
          private router: Router,
          private snackBar: MatSnackBar
  ) {
    this.signupForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$")]],
      confirmPassword: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$"), this.passwordMatchValidator]],
      name: ['', Validators.required],
      surname: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern("^[+]?[0-9 \\-().]{5,32}$")]],
      presentation: [''],
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

      this.publicService.getPromoter(this.accountId).subscribe({
        next: (account) => {
          this.promoter = account;
          this.resetForm();
          this.loadPromoterImage();
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
        action = this.adminService.createAccount(registration, "PROMOTER");
      } else {
        const promoterDTO: PromoterDTO = {
          id: this.accountId,
          email: this.signupForm.value.email,
          name: this.signupForm.value.name,
          surname: this.signupForm.value.surname,
          phone: this.signupForm.value.phone,
          presentation: this.signupForm.value.presentation,
          referenceType: "PROMOTER",
        };
        action = this.adminService.updatePromoter(promoterDTO);
      }
      action.subscribe({
        next: (account) => {
          this.snackBar.open(this.accountId != null ? 'Promoter created!' : 'Promoter updated!', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });

          if (this.accountId == null) {
            this.router.navigate(['/admin/promoters/' + account.id]);
          } else {
            this.promoter = account as PromoterDTO;
            this.resetForm();
            this.loadPromoterImage();
          }

          this.loading = false;
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
      name: this.promoter?.name ?? "",
      surname: this.promoter?.surname ?? "",
      email: this.promoter?.email ?? "",
      phone: this.promoter?.phone ?? "",
      presentation: this.promoter?.presentation ?? "",
    });
  }

  onImageSelected(file: File): void {
    if (file) {
      this.mainLoading = true;
      this.adminService.updatePromoterImage(this.accountId!, file).subscribe({
        next: (promoter) => {
          this.snackBar.open('Photo updated!', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });
          this.imageLoading = false;
          this.imagePreview = null;
          this.originalImage = null;
          this.mainLoading = false;
          this.promoter!.photo = promoter.photo;

          this.loadPromoterImage();
        },
        error: (error) => {
          console.error('Update failed', error);
          this.formError = error?.message || 'Update failed. Please try again.';
          this.mainLoading = false;
        }
      });
    }
  }

  removeImage(): void {
    this.mainLoading = true;
    this.imageLoading = false;
    this.adminService.deletePromoterImage(this.accountId!).subscribe({
      next: () => {
        this.snackBar.open('Photo removed!', 'Close', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['success-snackbar']
        });
        this.imagePreview = null;
        this.originalImage = null;
        this.mainLoading = false;
      },
      error: (error) => {
        console.error('Update failed', error);
        this.formError = error?.message || 'Update failed. Please try again.';
        this.mainLoading = false;
      }
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

  private loadPromoterImage() {
    this.imagePreview = null;
    this.originalImage = null;

    const photoUrl = this.promoter?.photo;

    if (!photoUrl) {
      return;
    }

    this.imageLoading = true;
    const isThumbnail = photoUrl.includes('thumbnail-');

    if (isThumbnail) {
      let previewImgSrc = photoUrl;

      const previewImg = new Image();
      previewImg.onload = () => {
        this.imageLoading = false;
        this.imagePreview = previewImgSrc;
      };
      previewImg.onerror = () => {
        this.imageLoading = false;
      };
      previewImg.src = previewImgSrc;
    }

    let originalImgSrc = photoUrl;
    if (isThumbnail) {
      originalImgSrc = photoUrl.replace('thumbnail-', '');
    }

    const originalImg = new Image();
    originalImg.onload = () => {
      this.imageLoading = false;
      this.originalImage = originalImg.src;
    };
    originalImg.onerror = () => {
      this.imageLoading = false;
    };
    if (isThumbnail) {
      originalImg.src = originalImgSrc;
    }
  }
}