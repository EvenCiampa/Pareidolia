import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {LayoutModule} from './layout/layout.module';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true, // Indica che è un componente standalone
  imports: [
    RouterOutlet,
    LayoutModule,
    CommonModule,
  ], // Importa il router per la gestione delle rotte
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  title = 'Pareidolia';
}
