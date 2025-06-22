import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {RouterModule} from '@angular/router';
import {PublicService} from '../../../core/services/public.service';
import {PromoterDTO} from '../../../core/models/promoter.interface';
import {Page} from '../../../core/models/page.interface';
import {EmptyTableComponent} from '../../../shared/dashboard/empty-table.component';
import {AdminService} from '../../../core/services/admin.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'app-promoter-management',
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
  templateUrl: './promoter-management.component.html',
  styleUrls: ['./promoter-management.component.css']
})
export class PromoterManagementComponent implements OnInit {
  promoters: PromoterDTO[] = [];
  loading = true;
  totalPromoters = 0;
  pageSize = 25;
  pageIndex = 0;
  displayedColumns: string[] = ['name', 'email', 'phone', 'actions'];

  constructor(
          private publicService: PublicService,
          private adminService: AdminService,
          private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.loadPromoters();
  }

  loadPromoters() {
    this.loading = true;
    this.publicService.getPromoters(this.pageIndex, this.pageSize)
            .subscribe({
              next: (response: Page<PromoterDTO>) => {
                this.promoters = response.content;
                this.totalPromoters = response.totalElements;
                this.loading = false;
              },
              error: (error) => {
                console.error('Error loading promoters:', error);
                this.loading = false;
              }
            });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPromoters();
  }

  deletePromoter(id: number) {
    this.adminService.deletePromoter(id).subscribe({
      next: () => {
        this.loadPromoters();
      },
      error: (error) => {
        console.error('Error deleting promoter:', error);
        this.snackBar.open(
                error?.message || 'Error deleting promoter. Please try again.',
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