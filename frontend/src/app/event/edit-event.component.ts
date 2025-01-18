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
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EventService } from '../services/event.service';
import { Event } from '../models/event.interface';
import { MatExpansionModule } from '@angular/material/expansion';
import { Message } from '../models/message.interface';
import {MatChip} from '@angular/material/chips';

@Component({
  selector: 'app-edit-event',
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
		MatSelectModule,
		MatSnackBarModule,
		MatExpansionModule,
		MatChip
	],
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.css']
})
export class EditEventComponent implements OnInit {
  eventForm: FormGroup;
  loading = false;
  isEdit = false;
  imagePreview: string | null = null;
  messages: Message[] = [];
  userRole = 'PROMOTER';
  userId = 1;

  categories = [
    'Musica',
    'Sport',
    'Arte',
    'Teatro',
    'Cinema',
    'Food & Drink',
    'Networking',
    'Altro'
  ];

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.eventForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      place: ['', Validators.required],
      date: ['', Validators.required],
      time: ['', Validators.required],
      duration: ['', [Validators.required, Validators.min(15)]],
      maxNumberOfParticipants: ['', [Validators.required, Validators.min(1)]],
      price: [''],
      category: [''],
      eventImage: [null],
      state: ['DRAFT']
    });

    this.eventForm.get('state')?.valueChanges.subscribe(state => {
      if (state === 'REVIEW') {
        this.addMessage('Ho completato le modifiche e richiedo una revisione.');
      }
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.loadEvent(+id);
    }
  }

  private loadEvent(id: number): void {
    this.loading = true;
    this.eventService.getEvent(id).subscribe({
      next: (event) => {
        this.eventForm.patchValue({
          title: event.title,
          description: event.description,
          place: event.place,
          date: new Date(event.date),
          time: event.time,
          duration: event.duration,
          maxNumberOfParticipants: event.maxNumberOfParticipants,
          price: event.price,
          category: event.category,
          state: event.state
        });
        this.imagePreview = event.imageUrl || null;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading event:', error);
        this.snackBar.open('Errore nel caricamento dell\'evento', 'Chiudi', {
          duration: 3000
        });
        this.loading = false;
      }
    });
  }

  onImageSelected(event: Event & { target: HTMLInputElement }): void {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
        this.eventForm.patchValue({ eventImage: file });
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.eventForm.valid) {
      this.loading = true;
      const formData = new FormData();

      Object.keys(this.eventForm.value).forEach(key => {
        if (this.eventForm.value[key] !== null) {
          if (key === 'date') {
            formData.append(key, this.eventForm.value[key].toISOString().split('T')[0]);
          } else {
            formData.append(key, this.eventForm.value[key]);
          }
        }
      });

      const request = this.isEdit ?
        this.eventService.updateEvent(+this.route.snapshot.paramMap.get('id')!, formData) :
        this.eventService.createEvent(formData);

      request.subscribe({
        next: (event) => {
          this.snackBar.open(
            `Evento ${this.isEdit ? 'aggiornato' : 'creato'} con successo`,
            'Chiudi',
            { duration: 3000 }
          );
          this.router.navigate(['/events', event.id]);
        },
        error: (error) => {
          console.error('Error saving event:', error);
          this.snackBar.open(
            `Errore durante il ${this.isEdit ? 'aggiornamento' : 'salvataggio'} dell'evento`,
            'Chiudi',
            { duration: 3000 }
          );
          this.loading = false;
        }
      });
    }
  }

  private addMessage(message: string): void {
    const newMessage: Message = {
      id: this.messages.length + 1,
      eventId: +this.route.snapshot.paramMap.get('id')!,
      userId: this.userId,
      userName: this.userRole === 'ADMIN' ? 'Admin' : 'Promoter',
      userRole: this.userRole as 'ADMIN' | 'PROMOTER',
      message,
      timestamp: new Date()
    };
    this.messages = [...this.messages, newMessage];
  }

  onMessageSubmit(messageText: string): void {
    if (messageText.trim()) {
      this.addMessage(messageText);
    }
  }

  onStateChange(newState: 'DRAFT' | 'REVIEW' | 'PUBLISHED'): void {
    const currentState = this.eventForm.get('state')?.value;

    // Verifica permessi promoter
    if (this.userRole === 'PROMOTER') {
      if (currentState !== 'DRAFT' || newState !== 'REVIEW') {
        this.snackBar.open('Come promoter puoi solo richiedere la revisione di una bozza', 'Chiudi', {
          duration: 3000
        });
        return;
      }
    }

    // Verifica permessi admin
    if (this.userRole !== 'ADMIN' && (newState === 'DRAFT' || newState === 'PUBLISHED')) {
      this.snackBar.open('Solo gli admin possono pubblicare o riportare in bozza gli eventi', 'Chiudi', {
        duration: 3000
      });
      return;
    }

    if (currentState !== newState) {
      this.eventForm.patchValue({ state: newState });

      if (newState === 'DRAFT') {
        this.addMessage('L\'evento è stato riportato in bozza per ulteriori modifiche.');
      } else if (newState === 'PUBLISHED') {
        this.addMessage('L\'evento è stato approvato e pubblicato.');
      } else if (newState === 'REVIEW') {
        this.addMessage('Ho completato le modifiche e richiedo una revisione.');
      }
    }
  }
}