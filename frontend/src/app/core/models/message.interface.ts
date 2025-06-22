export interface MessageDTO {
  id: number;
  idEvent: number;
  idAccount: number;
  message: string;
  accountName: string;
  accountReferenceType: 'ADMIN' | 'PROMOTER';
  creationTime?: Date;
}