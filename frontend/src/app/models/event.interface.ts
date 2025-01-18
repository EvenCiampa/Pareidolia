export interface Event {
    id: number;
    title: string;
    description: string;
    place: string;
    date: string;
    time: string;
    duration: number;
    maxNumberOfParticipants: number;
    currentParticipants?: number;
    promoterId: number;
    imageUrl?: string;
    state: 'DRAFT' | 'REVIEW' | 'PUBLISHED';
    price?: number;
    category?: string;
} 