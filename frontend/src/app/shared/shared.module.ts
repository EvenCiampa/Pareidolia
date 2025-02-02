import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from './material.module';

@NgModule({
  declarations: [
    // Shared components, directives, and pipes
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  exports: [
    ReactiveFormsModule,
    MaterialModule,
    // Export shared components, directives, and pipes
  ]
})
export class SharedModule {
}