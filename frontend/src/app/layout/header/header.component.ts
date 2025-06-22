import {Component, OnInit} from '@angular/core';
import {Router, RouterModule} from '@angular/router';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {AuthService} from '../../core/services/auth.service';
import {MatDividerModule} from '@angular/material/divider';
import {ConsumerDTO} from '../../core/models/consumer.interface';
import {PromoterDTO} from '../../core/models/promoter.interface';
import {AccountDTO} from '../../core/models/account.interface';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    NgOptimizedImage,
    MatDividerModule
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  user: ConsumerDTO | PromoterDTO | AccountDTO | null | undefined = undefined;
  isMobile = false;

  constructor(
          private authService: AuthService,
          private router: Router,
          private breakpointObserver: BreakpointObserver
  ) {
  }

  get isAdmin(): boolean {
    return this.user?.referenceType === 'ADMIN';
  }

  get isPromoter(): boolean {
    return this.user?.referenceType === 'PROMOTER';
  }

  get displayName(): string {
    if (!this.user) return '';
    if (this.isMobile) return this.user.name.charAt(0) + this.user.surname.charAt(0);
    return this.user.name.length > 15 ? this.user.name.substring(0, 12) + '...' : this.user.name;
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        this.user = user;
      },
      error: (error: any) => {
        console.error('Error getting profile:', error);
      }
    });

    this.breakpointObserver.observe([Breakpoints.Handset])
            .subscribe(result => {
              this.isMobile = result.matches;
            });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}