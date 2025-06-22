import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDividerModule} from '@angular/material/divider';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {EventImageComponent} from '../../../shared/components/event-image/event-image.component';
import {ActivatedRoute, Router} from '@angular/router';
import {EventDTO} from '../../../core/models/event.interface';
import {AuthService} from '../../../core/services/auth.service';
import {ReviewDTO} from '../../../core/models/review.interface';
import {Page} from '../../../core/models/page.interface';
import {MatDialog} from '@angular/material/dialog';
import {AddReviewDialogComponent} from './add-review-dialog/add-review-dialog.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {PublicService} from '../../../core/services/public.service';
import {ConsumerService} from '../../../core/services/consumer.service';
import {AdminService} from '../../../core/services/admin.service';
import {PromoterService} from '../../../core/services/promoter.service';
import {ConsumerDTO} from '../../../core/models/consumer.interface';
import {PromoterDTO} from '../../../core/models/promoter.interface';
import {AccountDTO} from '../../../core/models/account.interface';

@Component({
  selector: 'app-event-details',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatPaginatorModule,
    EventImageComponent
  ],
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css']
})
export class EventDetailsComponent implements OnInit {
  user: ConsumerDTO | PromoterDTO | AccountDTO | null | undefined = undefined;

  event: EventDTO | null = null;
  reviews: ReviewDTO[] = [];
  loading = true;
  reviewsLoading = false;
  totalReviews = 0;
  pageSize = 10;
  pageIndex = 0;
  protected readonly Math = Math;

  constructor(
          private route: ActivatedRoute,
          private router: Router,
          private consumerService: ConsumerService,
          private promoterService: PromoterService,
          private publicService: PublicService,
          private adminService: AdminService,
          private authService: AuthService,
          private dialog: MatDialog,
          private snackBar: MatSnackBar
  ) {
  }

  get isEventStarted(): boolean {
    if (!this.event) return false;
    const eventDateTime = new Date(`${this.event.date}T${this.event.time}`);
    return eventDateTime < new Date();
  }

  get canBookOrCancelBook(): boolean {
    return !this.isEventStarted &&
            this.user?.referenceType === 'CONSUMER' &&
            this.Math.floor(Math.abs((this.event?.maxNumberOfParticipants ?? 0) - (this.event?.currentParticipants ?? 0))) > 0;
  }

  get canReview(): boolean {
    return this.isEventEnded &&
            this.user?.referenceType !== undefined &&
            ['CONSUMER', 'PROMOTER', 'ADMIN'].includes(this.user.referenceType) &&
            (this.event?.booked || this.user.referenceType !== 'CONSUMER');
  }

  get isEventEnded(): boolean {
    if (!this.event) return false;
    const eventDateTime = new Date(`${this.event.date}T${this.event.time}`);
    const eventEndTime = new Date(eventDateTime.getTime() + (this.event.duration * 60000));
    return eventEndTime < new Date();
  }

  ngOnInit(): void {
    const eventId = this.route.snapshot.paramMap.get('id');
    if (!eventId) {
      this.router.navigate(['/']);
      return;
    }

    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      this.loadEvent(+eventId);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.event) {
      this.loadReviews(this.event!.id!);
    }
  }

  formatDate(event: EventDTO): string {
    return new Date(event.date + "T" + event.time).toLocaleDateString();
  }

  formatTime(event: EventDTO): string {
    return new Date(event.date + "T" + event.time).toLocaleTimeString(undefined, {
      hour: "numeric",
      minute: "numeric",
      hour12: false
    });
  }

  bookEvent(): void {
    if (!this.event?.id || !this.canBookOrCancelBook) return;

    if (this.event.booked) {
      this.consumerService.deleteBookingByEventId(this.event.id).subscribe({
        next: () => {
          this.snackBar.open(
                  'Prenotazione cancellata con successo!',
                  'Chiudi',
                  {
                    duration: 3000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom'
                  }
          );
          this.loadEvent(this.event!.id!);
        },
        error: (error) => {
          this.snackBar.open(
                  error?.message || 'Errore durante la prenotazione',
                  'Chiudi',
                  {
                    duration: 3000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom'
                  }
          );
        }
      });
    } else {
      this.consumerService.createBooking(this.event.id).subscribe({
        next: () => {
          this.snackBar.open(
                  'Prenotazione effettuata con successo!',
                  'Chiudi',
                  {
                    duration: 3000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom'
                  }
          );
          this.loadEvent(this.event!.id!);
        },
        error: (error) => {
          this.snackBar.open(
                  error?.message || 'Errore durante la prenotazione',
                  'Chiudi',
                  {
                    duration: 3000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom'
                  }
          );
        }
      });
    }
  }

  openReviewDialog(): void {
    if (!this.event) return;

    const dialogRef = this.dialog.open(AddReviewDialogComponent, {
      width: '500px',
      data: {
        eventId: this.event.id,
        referenceType: this.user?.referenceType,
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadReviews(this.event!.id!);
      }
    });
  }

  getStarsArray(score: number): number[] {
    return Array(score).fill(0);
  }

  getEmptyStarsArray(score: number): number[] {
    return Array(5 - score).fill(0);
  }

  private loadEvent(eventId: number): void {
    this.loading = true;

    let service;
    if (this.user?.referenceType === "CONSUMER") {
      service = this.consumerService.getEvent(eventId);
    } else if (this.user?.referenceType === "ADMIN") {
      service = this.adminService.getEvent(eventId);
    } else {
      service = this.publicService.getEvent(eventId);
    }

    service.subscribe({
      next: (event) => {
        this.event = event;
        this.loading = false;
        if (this.isEventEnded) {
          this.loadReviews(eventId);
        }
      },
      error: () => {
        this.router.navigate(['/']);
      }
    });
  }

  private loadReviews(eventId: number): void {
    if (!this.isEventEnded) return;

    this.reviewsLoading = true;

    let service;
    if (this.user?.referenceType === "CONSUMER") {
      service = this.consumerService.getEventReviews(eventId, this.pageIndex, this.pageSize);
    } else if (this.user?.referenceType === "PROMOTER") {
      service = this.promoterService.getEventReviews(eventId, this.pageIndex, this.pageSize);
    } else if (this.user?.referenceType === "ADMIN") {
      service = this.adminService.getEventReviews(eventId, this.pageIndex, this.pageSize);
    } else {
      this.reviewsLoading = false;
      return;
    }

    service.subscribe({
      next: (response: Page<ReviewDTO>) => {
        this.reviews = response.content;
        this.totalReviews = response.totalElements;
        this.reviewsLoading = false;
      },
      error: () => {
        this.reviewsLoading = false;
      }
    });
  }
}