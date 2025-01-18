import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import {provideHttpClient} from '@angular/common/http'; // Rotte importate

bootstrapApplication(AppComponent, {
	providers: [
		provideRouter(routes), // // Configurazione delle rotte
		provideHttpClient() // Configurazione del modulo HTTP
	]
}).catch(err => console.error(err));