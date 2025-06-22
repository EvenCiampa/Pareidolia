import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDialog} from '@angular/material/dialog';
import {ImagePreviewDialogComponent} from '../image-preview-dialog/image-preview-dialog.component';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-event-image',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './event-image.component.html',
  styleUrls: ['./event-image.component.css']
})
export class EventImageComponent implements OnChanges {
  @Input() imageUrl: string | null = null;
  @Input() loading = false;

  displayUrl: string | null = null;
  originalUrl: string | null = null;

  constructor(private dialog: MatDialog) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['imageUrl']) {
      this.handleImageUrl();
    }
  }

  openImagePreview(): void {
    const imageUrl = this.originalUrl || this.displayUrl;
    if (!imageUrl) return;

    this.dialog.open(ImagePreviewDialogComponent, {
      data: {imageUrl},
      maxWidth: '90vw',
      maxHeight: '90vh',
      panelClass: 'image-preview-dialog'
    });
  }

  private handleImageUrl() {
    this.displayUrl = null;
    this.originalUrl = null;

    const photoUrl = this.imageUrl;

    if (!photoUrl) {
      return;
    }

    this.loading = true;
    const isThumbnail = photoUrl.includes('thumbnail-');

    if (isThumbnail) {
      let previewImgSrc = photoUrl;

      const previewImg = new Image();
      previewImg.onload = () => {
        this.loading = false;
        this.displayUrl = previewImgSrc;
      };
      previewImg.onerror = () => {
        this.loading = false;
      };
      previewImg.src = previewImgSrc;
    }

    let originalImgSrc = photoUrl;
    if (isThumbnail) {
      originalImgSrc = photoUrl.replace('thumbnail-', '');
    }

    const originalImg = new Image();
    originalImg.onload = () => {
      this.loading = false;
      this.originalUrl = originalImg.src;
    };
    originalImg.onerror = () => {
      this.loading = false;
    };
    if (isThumbnail) {
      originalImg.src = originalImgSrc;
    }
  }
}