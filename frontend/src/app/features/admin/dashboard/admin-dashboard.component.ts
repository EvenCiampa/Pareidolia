import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatListModule} from '@angular/material/list';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {BreakpointObserver, Breakpoints, BreakpointState} from '@angular/cdk/layout';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  isExpanded = true;
  isMobile = false;
  private destroy$ = new Subject<void>();

  constructor(private breakpointObserver: BreakpointObserver) {
  }

  ngOnInit() {
    this.breakpointObserver
            .observe([
              Breakpoints.XSmall,
              Breakpoints.Small,
              '(max-width: 960px)'
            ])
            .pipe(takeUntil(this.destroy$))
            .subscribe((state: BreakpointState) => {
              this.isMobile = state.matches;
              if (this.isMobile) {
                this.isExpanded = false;
              }
            });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleMenu() {
    this.isExpanded = !this.isExpanded;
  }

  closeMobileMenu() {
    if (this.isMobile) {
      this.isExpanded = false;
    }
  }

  onSidenavClosed() {
    this.isExpanded = false;
  }
}