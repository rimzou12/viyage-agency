import { Routes } from '@angular/router';
import { TripList } from './trip-list/trip-list';
import { TripDetail } from './trip-detail/trip-detail';

export const routes: Routes = [
  { path: '', component: TripList },
  { path: 'trips/:id', component: TripDetail },
];
