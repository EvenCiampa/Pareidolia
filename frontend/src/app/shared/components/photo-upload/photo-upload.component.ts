import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {ImagePreviewDialogComponent} from '../image-preview-dialog/image-preview-dialog.component';

@Component({
  selector: 'app-photo-upload',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatDialogModule
  ],
  templateUrl: './photo-upload.component.html',
  styleUrls: ['./photo-upload.component.css']
})
export class PhotoUploadComponent {
  @Input() imagePreview: string | null = null;
  @Input() originalImage: string | null = null;
  @Input() loading = false;
  @Input() editable = true;
  @Output() fileSelected = new EventEmitter<File>();
  @Output() removeImage = new EventEmitter<void>();

  constructor(private dialog: MatDialog) {
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.fileSelected.emit(file);
    }
  }

  onRemoveImage(): void {
    this.removeImage.emit();
  }

  openImagePreview(): void {
    const imageUrl = this.originalImage || this.imagePreview;
    if (!imageUrl) return;

    this.dialog.open(ImagePreviewDialogComponent, {
      data: {imageUrl},
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'image-preview-dialog'
    });
  }
}