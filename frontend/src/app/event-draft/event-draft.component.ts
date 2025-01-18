import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EventService } from '../services/event.service';
import { PromoterService } from '../services/promoter.service';
import { UserProfile } from '../models/user.interface'; // Importa l'interfaccia UserProfile

@Component({
	selector: 'app-event-draft',
	standalone: true,
	imports: [
		CommonModule,
		ReactiveFormsModule,
		RouterModule,
		MatCardModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatDatepickerModule,
		MatNativeDateModule,
		MatSnackBarModule,
		MatSelectModule,
		MatExpansionModule,
		MatProgressSpinnerModule,
	],
	templateUrl: './event-draft.component.html',
	styleUrls: ['./event-draft.component.css'],
})
export class EventDraftComponent implements OnInit {
	eventDraftForm: FormGroup;
	loading = false;
	userRole = 'PROMOTER';
	availablePromoters: UserProfile[] = []; // Lista di promoter per aggiungere alla bozza dell'evento

	constructor(
			private fb: FormBuilder,
			private eventService: EventService,
			private promoterService: PromoterService,
			private route: ActivatedRoute,
			private router: Router,
			private snackBar: MatSnackBar
	) {
		this.eventDraftForm = this.fb.group({
			title: ['', [Validators.required, Validators.minLength(3)]],
			description: ['', [Validators.required, Validators.minLength(20)]],
			place: ['', Validators.required],
			date: ['', Validators.required],
			time: ['', Validators.required],
			duration: ['', [Validators.required, Validators.min(15)]],
			maxNumberOfParticipants: ['', [Validators.required, Validators.min(1)]],
		});
	}

	ngOnInit(): void {
		this.getPromotersList();
	}

	// Funzione per ottenere la lista dei promoter
	getPromotersList(): void {
		this.promoterService.getPromotersList().subscribe({
			next: (promoters: UserProfile[]) => {
				this.availablePromoters = promoters;
			},
			error: (err) => {
				console.error('Errore durante il caricamento della lista dei promoter:', err);
			},
		});
	}

	// Funzione per inviare la bozza dell'evento
	onSubmit(): void {
		if (this.eventDraftForm.valid) {
			this.loading = true;
			const formData = this.eventDraftForm.value;

			this.eventService.createEventDraft(formData).subscribe({
				next: (event) => {
					this.snackBar.open('Bozza evento creata con successo', 'Chiudi', { duration: 3000 });
					this.router.navigate(['/events/drafts', event.id]);
				},
				error: (error) => {
					console.error('Errore durante la creazione della bozza:', error);
					this.snackBar.open('Errore durante la creazione della bozza', 'Chiudi', { duration: 3000 });
					this.loading = false;
				},
			});
		}
	}

	// Funzione per aggiungere un promoter alla bozza dell'evento
	addPromoterToDraft(promoterId: number): void {
		const draftId = this.route.snapshot.params['id'];
		if (!draftId) return;

		this.eventService.addPromoterToEventDraft(draftId, promoterId).subscribe({
			next: () => {
				this.snackBar.open('Promoter aggiunto con successo', 'Chiudi', { duration: 3000 });
			},
			error: (err) => {
				console.error('Errore durante l\'aggiunta del promoter:', err);
			},
		});
	}

	// Funzione per inviare la bozza per la revisione
	submitForReview(): void {
		const draftId = this.route.snapshot.params['id'];
		if (!draftId) return;

		this.eventService.submitForReview(draftId).subscribe({
			next: () => {
				this.snackBar.open('Bozza inviata per revisione', 'Chiudi', { duration: 3000 });
				this.router.navigate(['/events/drafts']);
			},
			error: (err) => {
				console.error('Errore durante l\'invio per revisione:', err);
			},
		});
	}
}
