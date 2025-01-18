import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PromoterService } from '../services/promoter.service';
import { UserProfile } from '../models/user.interface';

@Component({
	selector: 'app-promoter-profile',
	standalone: true,
	imports: [
		CommonModule,
		MatCardModule,
		MatIconModule,
		MatButtonModule,
		MatDividerModule,
		MatProgressSpinnerModule,
	],
	templateUrl: './promoter-profile.component.html',
	styleUrls: ['./promoter-profile.component.css'],
})
export class PromoterProfileComponent implements OnInit {
	promoter?: UserProfile;
	loading = true;
	error = false;

	constructor(
			private promoterService: PromoterService,
			private route: ActivatedRoute
	) {}

	ngOnInit(): void {
		const id = this.route.snapshot.paramMap.get('id');
		if (id) {
			this.loadPromoterProfile(+id);
		}
	}

	private loadPromoterProfile(id: number): void {
		this.promoterService.getPromoterProfile(id).subscribe({
			next: (profile) => {
				this.promoter = profile;
				this.loading = false;
			},
			error: (error) => {
				console.error('Error loading promoter profile:', error);
				this.error = true;
				this.loading = false;
			},
		});
	}

	getSocialIcon(platform: string): string {
		const icons: { [key: string]: string } = {
			linkedin: 'linkedin',
			facebook: 'facebook',
			instagram: 'instagram',
			twitter: 'twitter',
		};
		return icons[platform] || 'link';
	}
}
