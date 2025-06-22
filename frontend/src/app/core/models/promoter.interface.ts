export interface PromoterDTO {
  id: number;
  email: string;
  name: string;
  surname: string;
  phone: string;
  photo?: string;
  presentation?: string;
  referenceType: 'PROMOTER';
  creationTime?: Date;
}