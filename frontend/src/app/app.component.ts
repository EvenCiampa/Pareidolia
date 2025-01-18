import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
	selector: 'app-root',
	standalone: true, // Indica che è un componente standalone
	imports: [RouterOutlet], // Importa il router per la gestione delle rotte
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.css'] // Assicurati che il nome sia corretto
})
export class AppComponent {
	title = 'pareidolia';
}
