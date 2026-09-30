import { Routes } from '@angular/router';
import { TripList } from './trip-list/trip-list';
import { TripDetail } from './trip-detail/trip-detail';
import { GroupBookingDetail } from './group-booking-detail/group-booking-detail';

export const routes: Routes = [
  { path: '', component: TripList },
  { path: 'trips/:id', component: TripDetail },
  { path: 'group-bookings/:id', component: GroupBookingDetail },
];
