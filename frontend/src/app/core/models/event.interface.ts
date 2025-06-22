import {PromoterDTO} from './promoter.interface';

export interface EventDTO {
  id?: number;
  title: string;
  description: string;
  image?: string;
  place: string;
  date: string;
  time: string;
  duration: number;
  maxNumberOfParticipants: number;
  currentParticipants?: number;
  score?: number;
  booked?: boolean;
  promoters: PromoterDTO[];
  state: 'DRAFT' | 'REVIEW' | 'PUBLISHED';
  creationTime?: Date;
}

export interface EventUpdateDTO {
  id?: number;
  title: string;
  description: string;
  image?: string;
  place: string;
  date: string;
  time: string;
  duration: number;
  maxNumberOfParticipants: number;
  currentParticipants?: number;
  score?: number;
  promoterEmails: string[];
  state: 'DRAFT' | 'REVIEW' | 'PUBLISHED';
}