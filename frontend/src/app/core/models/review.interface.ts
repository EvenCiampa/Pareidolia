export interface ReviewDTO {
  id?: number;
  title: string;
  description: string;
  score: number;
  idEvent: number;
  idConsumer?: number;
  accountName?: string;
  referenceType?: 'CONSUMER' | 'PROMOTER' | 'ADMIN';
  creationTime?: Date;
  tag?: string;
  isAnonymous?: boolean;
}