export interface AccountDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  referenceType: 'CONSUMER' | 'PROMOTER' | 'ADMIN';
  creationTime?: Date;
}

export interface AccountLoginDTO extends AccountDTO {
  authToken: string;
}