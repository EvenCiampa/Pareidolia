import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';
import {EventDTO} from '../../../core/models/event.interface';
import {Page} from '../../../core/models/page.interface';
import {EventImageComponent} from '../../../shared/components/event-image/event-image.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {PublicService} from '../../../core/services/public.service';
import {ConsumerService} from '../../../core/services/consumer.service';
import {PromoterService} from '../../../core/services/promoter.service';
import {AdminService} from '../../../core/services/admin.service';
import {AuthService} from '../../../core/services/auth.service';
import {ConsumerDTO} from '../../../core/models/consumer.interface';
import {PromoterDTO} from '../../../core/models/promoter.interface';
import {AccountDTO} from '../../../core/models/account.interface';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatIconModule,
    EventImageComponent,
    MatButtonToggleModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  user: ConsumerDTO | PromoterDTO | AccountDTO | null = null;
  events: EventDTO[] = [];
  loading = true;
  totalEvents = 0;
  pageSize = 12;
  pageIndex = 0;
  viewMode: 'grid' | 'list' = 'grid';
  protected readonly Math = Math;

  constructor(
          private authService: AuthService,
          private publicService: PublicService,
          private consumerService: ConsumerService,
          private promoterService: PromoterService,
          private adminService: AdminService
  ) {
  }

  get canBookEvents(): boolean {
    return this.user?.referenceType === 'CONSUMER';
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      this.loadEvents();
    });
  }

  loadEvents(): void {
    this.loading = true;

    let service;
    if (this.user?.referenceType === "CONSUMER") {
      service = this.consumerService.getPublicEvents(this.pageIndex, this.pageSize);
    } else {
      service = this.publicService.getPublicEvents(this.pageIndex, this.pageSize);
    }

    service.subscribe({
      next: (response: Page<EventDTO>) => {
        this.events = response.content;
        this.totalEvents = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading events:', error);
        this.loading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEvents();
  }

  formatDate(event: EventDTO): string {
    return new Date(event.date + "T" + event.time).toLocaleDateString();
  }

  formatTime(event: EventDTO): string {
    return new Date(event.date + "T" + event.time).toLocaleTimeString(undefined, {hour: "numeric", minute: "numeric", hour12: false});
  }

  toggleView(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  isEventStarted(event: EventDTO): boolean {
    const eventDateTime = new Date(`${event.date}T${event.time}`);
    return eventDateTime < new Date();
  }

  canBookOrCancelBook(event: EventDTO): boolean {
    return !this.isEventStarted(event) &&
            this.canBookEvents &&
            this.Math.floor(Math.abs((event.maxNumberOfParticipants) - (event.currentParticipants ?? 0))) > 0;
  }

  bookEvent(event: EventDTO, e: Event): void {
    e.preventDefault(); // Prevent navigation
    e.stopPropagation(); // Stop event bubbling

    if (!event.id || !this.canBookOrCancelBook(event)) return;

    if (event.booked) {
      this.consumerService.deleteBookingByEventId(event.id).subscribe({
        next: () => {
          this.loadEvents();
        },
        error: (error) => {
          console.error('Error booking event:', error);
        }
      });
    } else {
      this.consumerService.createBooking(event.id).subscribe({
        next: () => {
          this.loadEvents();
        },
        error: (error) => {
          console.error('Error booking event:', error);
        }
      });
    }
  }
}
