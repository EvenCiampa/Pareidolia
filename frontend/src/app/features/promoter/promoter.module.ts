import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../shared/shared.module';
import {AuthGuard} from '../../core/guards/auth.guard';
import {PromoterGuard} from '../../core/guards/promoter.guard';
import {PromoterDashboardComponent} from './dashboard/promoter-dashboard.component';
import {PromoterEventFormComponent} from './events/form/promoter-event-form.component';
import {PromoterEventManagementComponent} from './events/promoter-event-management.component';

const routes: Routes = [
  {
    path: '',
    component: PromoterDashboardComponent,
    canActivate: [AuthGuard, PromoterGuard],
    children: [
      {path: 'events', component: PromoterEventManagementComponent},
      {path: 'events/new', component: PromoterEventFormComponent},
      {path: 'events/:id', component: PromoterEventFormComponent},
      {path: '', redirectTo: 'events', pathMatch: 'full'}
    ]
  }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule.forChild(routes),
    PromoterDashboardComponent,
    PromoterEventFormComponent
  ]
})
export class PromoterModule {
}