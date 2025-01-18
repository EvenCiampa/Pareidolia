export interface Message {
  id: number;
  eventId: number;
  userId: number;
  userName: string;
  userRole: 'ADMIN' | 'PROMOTER';
  message: string;
  timestamp: Date;
} 