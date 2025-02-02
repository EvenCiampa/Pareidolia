import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {RouterModule} from '@angular/router';
import {EventDTO} from '../../../core/models/event.interface';
import {Page} from '../../../core/models/page.interface';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {EmptyTableComponent} from '../../../shared/dashboard/empty-table.component';
import {PromoterService} from '../../../core/services/promoter.service';

@Component({
  selector: 'app-promoter-event-management',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    RouterModule,
    MatProgressSpinnerModule,
    EmptyTableComponent
  ],
  templateUrl: './promoter-event-management.component.html',
  styleUrls: ['./promoter-event-management.component.css']
})
export class PromoterEventManagementComponent implements OnInit {
  events: EventDTO[] = [];
  loading = true;
  totalEvents = 0;
  pageSize = 25;
  pageIndex = 0;
  displayedColumns: string[] = ['title', 'date', 'status', 'actions'];

  constructor(private promoterService: PromoterService) {
  }

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.promoterService.getEvents(this.pageIndex, this.pageSize)
            .subscribe({
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

  eventStateToString(state?: string): string | undefined {
    switch (state) {
      case "DRAFT":
        return "Draft";
      case "REVIEW":
        return "Under Review";
      case "PUBLISHED":
        return "Published";
      default:
        return undefined;
    }
  }
}