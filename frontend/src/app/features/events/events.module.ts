import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../shared/shared.module';
import {EventDetailsComponent} from './component/event-details.component';

const routes: Routes = [
  {path: '', component: EventDetailsComponent},
  {path: ':id', component: EventDetailsComponent},
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule.forChild(routes),
    EventDetailsComponent
  ]
})
export class EventsModule {
}