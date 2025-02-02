import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {RouterModule} from '@angular/router';
import {AdminService} from '../../../core/services/admin.service';
import {EventDTO} from '../../../core/models/event.interface';
import {Page} from '../../../core/models/page.interface';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {EmptyTableComponent} from '../../../shared/dashboard/empty-table.component';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-event-management',
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
  templateUrl: './event-management.component.html',
  styleUrls: ['./event-management.component.css']
})
export class EventManagementComponent implements OnInit {
  events: EventDTO[] = [];
  loading = true;
  totalEvents = 0;
  pageSize = 25;
  pageIndex = 0;
  displayedColumns: string[] = ['title', 'date', 'status', 'actions'];

  constructor(
          private adminService: AdminService,
          private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.loadEvents();
  }

  loadEvents() {
    this.loading = true;
    this.adminService.getEvents(this.pageIndex, this.pageSize)
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

  deleteEvent(id: number) {
    this.adminService.deleteEvent(id).subscribe({
      next: () => {
        this.loadEvents();
      },
      error: (error) => {
        console.error('Error deleting event:', error);
        this.snackBar.open(
                error?.message || 'Error deleting event. Please try again.',
                'Close',
                {
                  duration: 5000,
                  horizontalPosition: 'center',
                  verticalPosition: 'bottom',
                  panelClass: ['error-snackbar']
                }
        );
      }
    });
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