import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EventService } from '../services/event.service';
import { Event } from '../models/event.interface';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatPaginatorModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  events: Event[] = [];
  loading = true;
  totalEvents = 0;
  pageSize = 10;
  pageIndex = 0;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getPublicEvents(this.pageIndex, this.pageSize)
      .subscribe({
        next: (response) => {
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

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString();
  }
}
