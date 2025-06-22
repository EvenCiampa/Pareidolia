import {EventDTO} from './event.interface';
import {AccountDTO} from './account.interface';

export interface BookingDTO {
  id: number;
  event: EventDTO;
  consumer: AccountDTO;
  creationTime: Date;
}