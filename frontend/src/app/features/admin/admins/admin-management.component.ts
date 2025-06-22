import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {RouterModule} from '@angular/router';
import {AdminService} from '../../../core/services/admin.service';
import {Page} from '../../../core/models/page.interface';
import {EmptyTableComponent} from '../../../shared/dashboard/empty-table.component';
import {AccountDTO} from '../../../core/models/account.interface';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../../core/services/auth.service';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'app-admin-management',
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
  templateUrl: './admin-management.component.html',
  styleUrls: ['./admin-management.component.css']
})
export class AdminManagementComponent implements OnInit {
  admins: AccountDTO[] = [];
  loading = true;
  totalAdmins = 0;
  pageSize = 25;
  pageIndex = 0;
  displayedColumns: string[] = ['name', 'email', 'actions'];

  constructor(
          private adminService: AdminService,
          public authService: AuthService,
          private snackBar: MatSnackBar
  ) {
  }

  ngOnInit() {
    this.loadAdmins();
  }

  loadAdmins() {
    this.loading = true;
    this.adminService.getAdmins(this.pageIndex, this.pageSize)
            .subscribe({
              next: (response: Page<AccountDTO>) => {
                this.admins = response.content;
                this.totalAdmins = response.totalElements;
                this.loading = false;
              },
              error: (error) => {
                console.error('Error loading admins:', error);
                this.loading = false;
              }
            });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAdmins();
  }

  deleteAdmin(id: number) {
    this.adminService.deleteAccount(id).subscribe({
      next: () => {
        this.loadAdmins();
      },
      error: (error) => {
        console.error('Error deleting admin:', error);
        this.snackBar.open(
                error?.message || 'Error deleting admin. Please try again.',
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