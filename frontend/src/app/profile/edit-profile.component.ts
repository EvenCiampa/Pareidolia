import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../services/user.service';
import { UserProfile } from '../models/user.interface';

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
    MatSnackBarModule
  ],
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  profileForm: FormGroup;
  promoterForm: FormGroup | null = null;
  loading = false;
  imagePreview: string | null = null;
  isPromoter = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: [''],
      profileImage: [null]
    });
  }

  ngOnInit(): void {
    this.loadProfileData();
  }

  private initPromoterForm(): void {
    this.promoterForm = this.fb.group({
      organization: ['', Validators.required],
      position: ['', Validators.required],
      description: ['', Validators.required],
      website: [''],
      linkedin: [''],
      facebook: [''],
      instagram: [''],
      twitter: ['']
    });
  }

  loadProfileData(): void {
    const userId = 1; // TODO: Get from auth service
    this.userService.getUserProfile(userId).subscribe({
      next: (profile) => {
        this.profileForm.patchValue({
          name: profile.account.name,
          surname: profile.account.surname,
          email: profile.account.email,
          phoneNumber: profile.account.phoneNumber
        });
        
        this.imagePreview = profile.account.imageUrl || null;
        this.isPromoter = profile.account.role === 'PROMOTER';

        if (this.isPromoter && profile.promoterInfo) {
          this.initPromoterForm();
          this.promoterForm?.patchValue({
            organization: profile.promoterInfo.organization,
            position: profile.promoterInfo.position,
            description: profile.promoterInfo.description,
            website: profile.promoterInfo.website,
            ...profile.promoterInfo.socialLinks
          });
        }
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.snackBar.open('Errore nel caricamento del profilo', 'Chiudi', {
          duration: 3000
        });
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.valid && (!this.isPromoter || this.promoterForm?.valid)) {
      this.loading = true;
      const formData = new FormData();
      
      // Aggiungi i dati base del profilo
      Object.keys(this.profileForm.value).forEach(key => {
        if (this.profileForm.value[key] !== null) {
          formData.append(key, this.profileForm.value[key]);
        }
      });

      // Se è un promoter, aggiungi anche i dati del promoter
      if (this.isPromoter && this.promoterForm) {
        const promoterValue = this.promoterForm.value;
        Object.keys(promoterValue).forEach(key => {
          if (promoterValue[key] !== null) {
            formData.append(`promoterInfo.${key}`, promoterValue[key]);
          }
        });
      }

      this.userService.updateProfile(formData).subscribe({
        next: () => {
          this.snackBar.open('Profilo aggiornato con successo', 'Chiudi', {
            duration: 3000
          });
          this.router.navigate(['/profile']);
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.snackBar.open('Errore nell\'aggiornamento del profilo', 'Chiudi', {
            duration: 3000
          });
        },
        complete: () => {
          this.loading = false;
        }
      });
    }
  }

  onImageSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
        this.profileForm.patchValue({ profileImage: file });
      };
      reader.readAsDataURL(file);
    }
  }
} 