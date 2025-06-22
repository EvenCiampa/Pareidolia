import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../shared/shared.module';
import {AuthGuard} from '../../core/guards/auth.guard';
import {AdminGuard} from '../../core/guards/admin.guard';
import {AdminDashboardComponent} from './dashboard/admin-dashboard.component';
import {EventManagementComponent} from './events/event-management.component';
import {PromoterManagementComponent} from './promoters/promoter-management.component';
import {ConsumerManagementComponent} from './consumer/consumer-management.component';
import {AdminManagementComponent} from './admins/admin-management.component';
import {ConsumerFormComponent} from './consumer/form/consumer-form.component';
import {AdminFormComponent} from './admins/form/admin-form.component';
import {PromoterFormComponent} from './promoters/form/promoter-form.component';
import {EventFormComponent} from './events/form/event-form.component';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, AdminGuard],
    children: [
      {path: 'events', component: EventManagementComponent},
      {path: 'events/new', component: EventFormComponent},
      {path: 'events/:id', component: EventFormComponent},
      {path: 'promoters', component: PromoterManagementComponent},
      {path: 'promoters/new', component: PromoterFormComponent},
      {path: 'promoters/:id', component: PromoterFormComponent},
      {path: 'consumers', component: ConsumerManagementComponent},
      {path: 'consumers/new', component: ConsumerFormComponent},
      {path: 'consumers/:id', component: ConsumerFormComponent},
      {path: 'admins', component: AdminManagementComponent},
      {path: 'admins/new', component: AdminFormComponent},
      {path: 'admins/:id', component: AdminFormComponent},
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
    AdminDashboardComponent,
    EventManagementComponent,
    PromoterManagementComponent,
    ConsumerManagementComponent,
    AdminManagementComponent,
    ConsumerFormComponent,
    AdminFormComponent,
    PromoterFormComponent,
    EventFormComponent
  ]
})
export class AdminModule {
}