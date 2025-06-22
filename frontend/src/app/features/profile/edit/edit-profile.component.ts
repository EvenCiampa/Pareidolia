import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {RouterModule} from '@angular/router';
import {ConsumerService} from '../../../core/services/consumer.service';
import {PromoterService} from '../../../core/services/promoter.service';
import {AuthService} from '../../../core/services/auth.service';
import {MatTabsModule} from '@angular/material/tabs';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {ConsumerDTO} from '../../../core/models/consumer.interface';
import {PromoterDTO} from '../../../core/models/promoter.interface';
import {AccountDTO, AccountLoginDTO} from '../../../core/models/account.interface';
import {AdminService} from '../../../core/services/admin.service';
import {Observable} from 'rxjs';
import {PhotoUploadComponent} from '../../../shared/components/photo-upload/photo-upload.component';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    PhotoUploadComponent,
  ],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  user: ConsumerDTO | PromoterDTO | AccountDTO | null | undefined = undefined;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  loading = false;
  imageLoading = false;
  mainLoading = false;
  imagePreview: string | null = null;
  originalImage: string | null = null;
  isPromoter = false;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;
  passwordValidations = {
    minLength: false,
    hasNumber: false,
    hasUpper: false,
    hasLower: false,
    hasSpecial: false,
    noSpaces: false
  };
  profileFormError: string | null = null;
  passwordFormError: string | null = null;

  constructor(
          private fb: FormBuilder,
          private consumerService: ConsumerService,
          private promoterService: PromoterService,
          private adminService: AdminService,
          private authService: AuthService,
          private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern("^[+]?[0-9 \\-().]{5,32}$")]],
      presentation: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$")]],
      confirmPassword: ['', [Validators.required, Validators.pattern("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=\\-!])(?=\\S+$).{8,}$"), this.passwordMatchValidator]],
    });

    // Add password validation tracking
    this.passwordForm.get('newPassword')?.valueChanges.subscribe(value => {
      this.updatePasswordValidations(value || '');
    });

    // Reset form errors on value changes
    this.profileForm.valueChanges.subscribe(() => {
      this.profileFormError = null;
    });

    this.passwordForm.valueChanges.subscribe(() => {
      this.passwordFormError = null;
    });
  }

  getProfileErrorMessage(controlName: string): string {
    const control = this.profileForm.get(controlName);
    if (control?.hasError('required')) {
      return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} is required`;
    }
    if (control?.hasError('email')) {
      return 'Invalid email format';
    }
    if (controlName === 'phone' && control?.hasError('pattern')) {
      return 'Please enter a valid phone number';
    }
    return '';
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      this.isPromoter = user?.referenceType === "PROMOTER";
      this.loadProfileData();
      this.loadPromoterImage();
    });
  }

  loadProfileData(): void {
    this.resetProfileForm();
    this.resetPasswordForm();

    if (this.isPromoter) {
      const promoterProfile = this.user as PromoterDTO;
      if (promoterProfile.photo) {
        this.setProfileImage(promoterProfile.photo);
      }
    }
  }

  onProfileSubmit(): void {
    if (this.profileForm.valid && this.user) {
      this.loading = true;
      this.profileFormError = null;

      let observable: Observable<AccountLoginDTO>;
      if (this.user?.referenceType === "CONSUMER") {
        observable = this.consumerService.update({
          id: this.user.id,
          email: this.profileForm.value.email,
          name: this.profileForm.value.name,
          surname: this.profileForm.value.surname,
          phone: this.profileForm.value.phone,
          referenceType: this.user?.referenceType,
        });
      } else if (this.user?.referenceType === "PROMOTER") {
        observable = this.promoterService.update({
          id: this.user.id,
          email: this.profileForm.value.email,
          name: this.profileForm.value.name,
          surname: this.profileForm.value.surname,
          phone: this.profileForm.value.phone,
          presentation: this.profileForm.value.presentation,
          referenceType: this.user?.referenceType,
        });
      } else if (this.user?.referenceType === "ADMIN") {
        observable = this.adminService.update({
          id: this.user.id,
          email: this.profileForm.value.email,
          name: this.profileForm.value.name,
          surname: this.profileForm.value.surname,
          phone: this.profileForm.value.phone,
          referenceType: this.user?.referenceType,
        });
      } else {
        return;
      }

      observable.subscribe({
        next: (account: AccountLoginDTO) => {
          this.snackBar.open('Profilo aggiornato con successo', 'Chiudi', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom'
          });
          if (account.authToken != null) {
            this.authService.setToken(account.authToken, account.referenceType).subscribe({
              next: () => {
                this.loading = false;
              },
              error: (error: any) => {
                console.error('Error getting profile:', error);
                this.loading = false;
              }
            });
          } else {
            this.authService.refreshCurrentUser().subscribe({
              next: () => {
                this.loading = false;
              },
              error: (error: any) => {
                console.error('Error getting profile:', error);
                this.loading = false;
              }
            });
          }
        },
        error: (error: any) => {
          console.error('Error updating profile:', error);
          this.profileFormError = error?.message || 'Errore nell\'aggiornamento del profilo';
          this.loading = false;
        }
      });
    }
  }

  onPasswordSubmit(): void {
    if (this.passwordForm.valid) {
      this.loading = true;
      this.passwordFormError = null;

      let service: ConsumerService | PromoterService | AdminService;
      if (this.user?.referenceType === "CONSUMER") {
        service = this.consumerService;
      } else if (this.user?.referenceType === "PROMOTER") {
        service = this.promoterService;
      } else if (this.user?.referenceType === "ADMIN") {
        service = this.adminService;
      } else {
        return;
      }

      service.updatePassword({
        currentPassword: this.passwordForm.value.currentPassword,
        newPassword: this.passwordForm.value.newPassword,
      }).subscribe({
        next: (account) => {
          this.snackBar.open('Password aggiornata con successo', 'Chiudi', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom'
          });
          this.passwordForm.reset();
          if (account.authToken != null) {
            this.authService.setToken(account.authToken, account.referenceType).subscribe({
              next: () => {
                this.loading = false;
              },
              error: (error: any) => {
                console.error('Error getting profile:', error);
                this.loading = false;
              }
            });
          } else {
            this.authService.refreshCurrentUser().subscribe({
              next: () => {
                this.loading = false;
              },
              error: (error: any) => {
                console.error('Error getting profile:', error);
                this.loading = false;
              }
            });
          }
        },
        error: (error) => {
          console.error('Error updating password:', error);
          this.passwordFormError = error?.message || 'Errore nell\'aggiornamento della password';
          this.loading = false;
        }
      });
    }
  }

  resetProfileForm() {
    this.profileForm.patchValue({
      name: this.user?.name ?? "",
      surname: this.user?.surname ?? "",
      email: this.user?.email ?? "",
      phone: this.user?.phone ?? "",
    });

    if (this.isPromoter) {
      const promoterProfile = this.user as PromoterDTO;
      this.profileForm.patchValue({
        presentation: promoterProfile.presentation ?? "",
      });
    }
  }

  resetPasswordForm() {
    this.passwordForm.reset({
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    });
  }

  onImageSelected(file: File): void {
    if (file) {
      this.mainLoading = true;
      this.promoterService.updateImage(file).subscribe({
        next: (event) => {
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
          (this.user as PromoterDTO)!.photo = event.photo;

          this.loadPromoterImage();
        },
        error: (error) => {
          console.error('Update failed', error);
          this.mainLoading = false;
        }
      });
    }
  }

  removeImage(): void {
    this.mainLoading = true;
    this.imageLoading = false;
    this.promoterService.deleteImage().subscribe({
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
        this.mainLoading = false;
      }
    });
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.parent?.get('password');
    const confirmPassword = control.parent?.get('confirmPassword');
    return password?.value == confirmPassword?.value ? null : {'mismatch': true};
  }

  private setProfileImage(photoUrl: string) {
    // Handle thumbnail and original image loading
    const isThumbnail = photoUrl.includes('thumbnail-');
    this.imagePreview = photoUrl;

    if (isThumbnail) {
      const originalUrl = photoUrl.replace('thumbnail-', '');
      this.loadOriginalImage(originalUrl);
    } else {
      this.originalImage = photoUrl;
    }
  }

  private loadOriginalImage(url: string) {
    this.imageLoading = true;
    const img = new Image();
    img.onload = () => {
      this.originalImage = url;
      this.imageLoading = false;
    };
    img.src = url;
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

    const photoUrl = (this.user as PromoterDTO)?.photo;

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