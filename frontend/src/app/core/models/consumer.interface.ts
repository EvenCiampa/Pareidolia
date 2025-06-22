export interface ConsumerDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  referenceType: 'CONSUMER';
  creationTime?: Date;
}