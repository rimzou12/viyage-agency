import { Routes } from '@angular/router';
import { TripList } from './trip-list/trip-list';
import { TripDetail } from './trip-detail/trip-detail';
import { GroupBookingDetail } from './group-booking-detail/group-booking-detail';
import { Login } from './login/login';
import { Register } from './register/register';
import { ContactAdmin } from './contact-admin/contact-admin';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';

export const routes: Routes = [
  { path: '', component: TripList },
  { path: 'trips/:id', component: TripDetail },
  { path: 'group-bookings/:id', component: GroupBookingDetail },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'contact', component: ContactAdmin },
  { path: 'admin', component: AdminDashboard },
];
