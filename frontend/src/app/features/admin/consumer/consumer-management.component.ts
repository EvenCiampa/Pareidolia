import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {RouterModule} from '@angular/router';
import {AdminService} from '../../../core/services/admin.service';
import {ConsumerDTO} from '../../../core/models/consumer.interface';
import {Page} from '../../../core/models/page.interface';
import {EmptyTableComponent} from '../../../shared/dashboard/empty-table.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'app-consumer-management',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    RouterModule,
    EmptyTableComponent,
    MatProgressSpinnerModule,
  ],
  templateUrl: './consumer-management.component.html',
  styleUrls: ['./consumer-management.component.css']
})
export class ConsumerManagementComponent implements OnInit {
  consumers: ConsumerDTO[] = [];
  loading = true;
  totalConsumers = 0;
  pageSize = 25;
  pageIndex = 0;
  displayedColumns: string[] = ['name', 'email', 'phone', 'actions'];

  constructor(
          private adminService: AdminService,
          private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.loadConsumers();
  }

  loadConsumers() {
    this.loading = true;
    this.adminService.getConsumers(this.pageIndex, this.pageSize)
            .subscribe({
              next: (response: Page<ConsumerDTO>) => {
                this.consumers = response.content;
                this.totalConsumers = response.totalElements;
                this.loading = false;
              },
              error: (error) => {
                console.error('Error loading consumers:', error);
                this.loading = false;
              }
            });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadConsumers();
  }

  deleteConsumer(id: number) {
    this.adminService.deleteConsumer(id).subscribe({
      next: () => {
        this.loadConsumers();
      },
      error: (error) => {
        console.error('Error deleting consumer:', error);
        this.snackBar.open(
                error?.message || 'Error deleting consumer. Please try again.',
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
}