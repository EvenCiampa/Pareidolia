export interface Account {
    id: number;
    email: string;
    name: string;
    surname: string;
    phoneNumber: string;
    imageUrl?: string;
    role: 'USER' | 'ADMIN' | 'PROMOTER';
}

export interface PromoterInfo {
	id: number;
	photo?: string; // Aggiunto per il supporto di immagini
	presentation?: string; // Aggiunto per la descrizione del promoter
	website?: string;
	socialLinks?: {
		linkedin?: string;
		facebook?: string;
		instagram?: string;
		twitter?: string;
	};
}

export interface UserProfile {
    account: Account;
    promoterInfo?: PromoterInfo; // opzionale, presente solo per i promoter
}