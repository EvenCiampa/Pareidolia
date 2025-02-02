import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConsumerService} from '../../../../core/services/consumer.service';
import {PromoterService} from '../../../../core/services/promoter.service';
import {AdminService} from '../../../../core/services/admin.service';
import {ReviewDTO} from '../../../../core/models/review.interface';

@Component({
  selector: 'app-add-review-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule
  ],
  templateUrl: './add-review-dialog.component.html',
  styleUrls: ['./add-review-dialog.component.css']
})
export class AddReviewDialogComponent {
  reviewForm: FormGroup;
  loading = false;

  constructor(
          private dialogRef: MatDialogRef<AddReviewDialogComponent>,
          private consumerService: ConsumerService,
          private promoterService: PromoterService,
          private adminService: AdminService,
          @Inject(MAT_DIALOG_DATA) private data: { eventId: number, referenceType: 'CONSUMER' | 'PROMOTER' | 'ADMIN' },
          private fb: FormBuilder,
          private snackBar: MatSnackBar
  ) {
    this.reviewForm = this.fb.group({
      title: ['', Validators.required],
      score: ['', [Validators.required, Validators.min(1), Validators.max(5)]],
      description: ['', Validators.required]
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.reviewForm.invalid || !this.data.referenceType) return;

    let data: ReviewDTO = {
      title: this.reviewForm.value.title,
      description: this.reviewForm.value.description,
      score: this.reviewForm.value.score,
      idEvent: this.data.eventId,
    };

    let service;
    if (this.data.referenceType === "CONSUMER") {
      service = this.consumerService.createReview(data)
    } else if (this.data.referenceType === "PROMOTER") {
      service = this.promoterService.createReview(data)
    } else if (this.data.referenceType === "ADMIN") {
      service = this.adminService.createReview(data)
    } else {
      return;
    }

    this.loading = true;
    service.subscribe({
      next: () => {
        this.snackBar.open('Recensione aggiunta con successo!', 'Chiudi', {
          duration: 3000
        });
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.snackBar.open(
                error?.message || 'Errore durante l\'invio della recensione',
                'Chiudi',
                {duration: 3000}
        );
        this.loading = false;
      }
    });
  }
}