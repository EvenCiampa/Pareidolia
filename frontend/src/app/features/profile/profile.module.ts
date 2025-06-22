import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {EditProfileComponent} from './edit/edit-profile.component';
import {SharedModule} from '../../shared/shared.module';
import {AuthGuard} from '../../core/guards/auth.guard';

const routes: Routes = [
  {path: '', component: EditProfileComponent, canActivate: [AuthGuard]}
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule.forChild(routes),
    EditProfileComponent
  ]
})
export class ProfileModule {
}