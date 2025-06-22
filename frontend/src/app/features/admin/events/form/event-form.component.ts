import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {MatSelectModule} from '@angular/material/select';
import {MatTimepickerModule} from '@angular/material/timepicker';
import {AdminService} from '../../../../core/services/admin.service';
import {EventDTO, EventUpdateDTO} from '../../../../core/models/event.interface';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTabsModule} from '@angular/material/tabs';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {PhotoUploadComponent} from '../../../../shared/components/photo-upload/photo-upload.component';
import {COMMA, ENTER, SPACE} from '@angular/cdk/keycodes';
import {MatChipInputEvent, MatChipsModule} from '@angular/material/chips';
import {AuthService} from '../../../../core/services/auth.service';
import {MessageDTO} from '../../../../core/models/message.interface';
import {PromoterDTO} from '../../../../core/models/promoter.interface';
import {AccountDTO} from '../../../../core/models/account.interface';

@Component({
  selector: 'app-event-form',
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
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTimepickerModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    PhotoUploadComponent,
    MatChipsModule,
    FormsModule
  ],
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.css'],
})
export class EventFormComponent implements OnInit {
  admin: AccountDTO | null = null;
  eventForm: FormGroup;
  event: EventDTO | null = null;
  eventId: number | null = null;
  loading = false;
  mainLoading = false;
  promoter: PromoterDTO | null = null;
  formError: string | null = null;
  imageLoading = false;
  imagePreview: string | null = null;
  originalImage: string | null = null;
  readonly separatorKeysCodes = [ENTER, COMMA, SPACE] as const;
  promoterEmails: string[] = [];
  messages: MessageDTO[] = [];
  newMessage = '';
  messagesPage = 0;
  messagesSize = 20;
  hasMoreMessages = true;
  loadingMessages = false;

  constructor(
          private fb: FormBuilder,
          private router: Router,
          private route: ActivatedRoute,
          private snackBar: MatSnackBar,
          private adminService: AdminService,
          private authService: AuthService
  ) {
    this.eventForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      place: ['', Validators.required],
      date: ['', Validators.required],
      time: ['', Validators.required],
      duration: ['', [Validators.required, Validators.min(15)]],
      maxNumberOfParticipants: ['', [Validators.required, Validators.min(1)]],
      promoterEmails: [[], Validators.required],
    });

    this.eventForm.valueChanges.subscribe(() => {
      this.formError = null;
    });
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.eventId = +id;
      this.mainLoading = true;

      this.adminService.getEvent(this.eventId).subscribe({
        next: (event) => {
          this.event = event;
          this.resetForm();
          this.loadEventImage();
          this.mainLoading = false;
          if (this.eventId) {
            this.loadMessages();
          }
        },
        error: (error: any) => {
          console.error('Error getting event:', error);
          this.router.navigate(['/admin/events']);
          this.mainLoading = false;
        }
      });
    }

    this.authService.currentUser$.subscribe(user => {
      this.admin = user;
    });
  }

  onSubmit() {
    if (this.eventForm.valid) {
      const dateTime = new Date(
              this.eventForm.value.date.getFullYear(), this.eventForm.value.date.getMonth(), this.eventForm.value.date.getDate(),
              this.eventForm.value.time.getHours(), this.eventForm.value.time.getMinutes()
      );
      const isoDateTimeString = dateTime.toISOString().split("T");

      this.loading = true;
      const event: EventUpdateDTO = {
        title: this.eventForm.value.title,
        description: this.eventForm.value.description,
        //image: this.eventForm.value.image,
        place: this.eventForm.value.place,
        date: isoDateTimeString[0],
        time: isoDateTimeString[1],
        duration: this.eventForm.value.duration,
        maxNumberOfParticipants: this.eventForm.value.maxNumberOfParticipants,
        state: this.event?.state ?? "DRAFT",
        promoterEmails: this.eventForm.value.promoterEmails,
      };

      if (this.eventId == null) {
        this.adminService.createEvent(event).subscribe({
          next: (event) => {
            this.snackBar.open(this.eventId != null ? 'Event created!' : 'Event updated!', 'Close', {
              duration: 5000,
              horizontalPosition: 'center',
              verticalPosition: 'bottom',
              panelClass: ['success-snackbar']
            });
            this.router.navigate(['/admin/events/' + event.id]);
            this.loading = false;
          },
          error: (error) => {
            console.error('Creation failed', error);
            this.formError = error?.message || 'Creation failed';
            this.loading = false;
          }
        });
      } else {
        event.id = this.eventId;
        this.adminService.updateEvent(event).subscribe({
          next: (event) => {
            this.snackBar.open('Event updated!', 'Close', {
              duration: 5000,
              horizontalPosition: 'center',
              verticalPosition: 'bottom',
              panelClass: ['success-snackbar']
            });

            this.event = event;
            this.resetForm();
            this.loadEventImage();

            this.loading = false;
          },
          error: (error) => {
            console.error('Update failed', error);
            this.formError = error?.message || 'Update failed';
            this.loading = false;
          }
        });
      }
    }
  }

  getErrorMessage(controlName: string, controlLabel: string): string {
    const control = this.eventForm.get(controlName);
    if (control?.hasError('required')) {
      return `${controlLabel} is required`;
    }
    if (controlName == "title" && control?.hasError('minLength')) {
      return 'Title must be at least 3 characters long';
    }
    if (controlName == "description" && control?.hasError('minLength')) {
      return 'Description must be at least 20 characters long';
    }
    if (controlName == "duration" && control?.hasError('min')) {
      return 'Duration must be at least 15 minutes';
    }
    if (controlName == "maxNumberOfParticipants" && control?.hasError('min')) {
      return 'Must be at least 1';
    }
    return '';
  }

  resetForm() {
    const promoterEmails = this.event?.promoters?.map(it => it.email) ?? [];
    this.promoterEmails = [...promoterEmails];
    const eventDate = this.event?.time && this.event?.date ? new Date(this.event?.date + "T" + this.event.time) : new Date()

    this.eventForm.patchValue({
      title: this.event?.title ?? "",
      description: this.event?.description ?? "",
      place: this.event?.place ?? "",
      date: new Date(eventDate),
      time: new Date(eventDate),
      duration: this.event?.duration ?? "",
      maxNumberOfParticipants: this.event?.maxNumberOfParticipants ?? "",
      promoterEmails: promoterEmails,
    });

    this.eventForm.get('promoterEmails')?.setValue(this.promoterEmails);
    this.eventForm.get('promoterEmails')?.markAsTouched();
  }

  onImageSelected(file: File): void {
    if (file) {
      this.imageLoading = true;
      this.adminService.updateEventImage(this.eventId!, file).subscribe({
        next: (event) => {
          this.snackBar.open('Image updated successfully!', 'Close', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'bottom',
            panelClass: ['success-snackbar']
          });
          this.event = event;
          this.loadEventImage();
        },
        error: (error) => {
          console.error('Error updating image:', error);
          this.snackBar.open(
                  error?.message || 'Error updating image. Please try again.',
                  'Close',
                  {
                    duration: 5000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom',
                    panelClass: ['error-snackbar']
                  }
          );
          this.imageLoading = false;
        }
      });
    }
  }

  removeImage(): void {
    this.imageLoading = true;
    this.adminService.deleteEventImage(this.eventId!).subscribe({
      next: (event) => {
        this.snackBar.open('Image removed successfully!', 'Close', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom',
          panelClass: ['success-snackbar']
        });
        this.event = event;
        this.imagePreview = null;
        this.originalImage = null;
        this.imageLoading = false;
      },
      error: (error) => {
        console.error('Error removing image:', error);
        this.snackBar.open(
                error?.message || 'Error removing image. Please try again.',
                'Close',
                {
                  duration: 5000,
                  horizontalPosition: 'center',
                  verticalPosition: 'bottom',
                  panelClass: ['error-snackbar']
                }
        );
        this.imageLoading = false;
      }
    });
  }

  addEmail(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    // Add email if valid
    if (value && emailRegex.test(value)) {
      this.promoterEmails.push(value);
      this.eventForm.get('promoterEmails')?.setValue(this.promoterEmails);
      this.eventForm.get('promoterEmails')?.markAsTouched();
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  removeEmail(email: string): void {
    const index = this.promoterEmails.indexOf(email);

    if (index >= 0) {
      this.promoterEmails.splice(index, 1);
      this.eventForm.get('promoterEmails')?.setValue(this.promoterEmails);
      this.eventForm.get('promoterEmails')?.markAsTouched();
    }
  }

  loadMessages(loadMore = false) {
    if (this.loadingMessages || (!loadMore && !this.hasMoreMessages)) return;

    this.loadingMessages = true;
    const page = loadMore ? this.messagesPage + 1 : 0;

    this.adminService.getMessages(this.eventId!, page, this.messagesSize).subscribe({
      next: (response) => {
        if (loadMore) {
          this.messages = [...this.messages, ...response.content];
          this.messagesPage = page;
        } else {
          this.messages = response.content;
          this.messagesPage = 0;
          setTimeout(() => this.scrollToBottom());
        }
        this.hasMoreMessages = response.content.length === this.messagesSize;
        this.loadingMessages = false;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.loadingMessages = false;
      }
    });
  }

  onScroll(event: any) {
    const element = event.target;
    if (element.scrollTop === 0 && this.hasMoreMessages && !this.loadingMessages) {
      this.loadMessages(true);
    }
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    this.adminService.createMessage(this.eventId!, this.newMessage).subscribe({
      next: (message) => {
        this.messages = [message, ...this.messages];
        this.newMessage = '';
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.snackBar.open('Error sending message', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  deleteMessage(messageId: number) {
    this.adminService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter(m => m.id !== messageId);
      },
      error: (error) => {
        console.error('Error deleting message:', error);
        this.snackBar.open('Error deleting message', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  moveBackwards() {
    this.mainLoading = true;
    this.adminService.moveBackwards(this.eventId!).subscribe({
      next: (updatedEvent) => {
        this.event = updatedEvent;
        this.snackBar.open('Status updated successfully', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
        this.mainLoading = false;
      },
      error: (error) => {
        console.error('Error updating status:', error);
        this.snackBar.open('Error updating status', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.mainLoading = false;
      }
    });
  }

  moveForward() {
    this.mainLoading = true;
    this.adminService.moveForward(this.eventId!).subscribe({
      next: (updatedEvent) => {
        this.event = updatedEvent;
        this.snackBar.open('Status updated successfully', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
        this.mainLoading = false;
      },
      error: (error) => {
        console.error('Error updating status:', error);
        this.snackBar.open('Error updating status', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.mainLoading = false;
      }
    });
  }

  isOwnMessage(message: MessageDTO): boolean {
    return message.idAccount === this.admin?.id;
  }

  private loadEventImage() {
    this.imagePreview = null;
    this.originalImage = null;

    const photoUrl = this.event?.image;

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

  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    });
  }
}