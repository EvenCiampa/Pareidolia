import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-empty-table',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, RouterModule],
  templateUrl: './empty-table.component.html',
  styles: [`
    .empty-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 20px;
      text-align: center;
    }

    .empty-icon {
      font-size: 64px;
      height: 64px;
      width: 64px;
      color: #666;
      margin-bottom: 16px;
    }

    h2 {
      color: #333;
      margin-bottom: 8px;
    }

    p {
      color: #666;
      margin: 0 0 16px;
    }
  `]
})
export class EmptyTableComponent {
  @Input() icon = 'info';
  @Input() title = 'No data available';
  @Input() message = 'There are no items to display.';
  @Input() createRoute?: string;
  @Input() createButtonText = 'Create New';
}