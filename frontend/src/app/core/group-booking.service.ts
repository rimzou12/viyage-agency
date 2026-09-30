import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { GroupBooking } from './models';

@Injectable({ providedIn: 'root' })
export class GroupBookingService {
  private readonly http = inject(HttpClient);

  createGroupBooking(tripId: string, customerName: string): Observable<GroupBooking> {
    return this.http.post<GroupBooking>(`${API_BASE_URL}/api/trips/${tripId}/group-bookings`, {
      customerName,
    });
  }

  joinGroupBooking(bookingId: string, customerName: string): Observable<GroupBooking> {
    return this.http.post<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}/participants`, {
      customerName,
    });
  }

  getGroupBooking(bookingId: string): Observable<GroupBooking> {
    return this.http.get<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}`);
  }
}

export function apiErrorMessage(error: HttpErrorResponse, fallback: string): string {
  const message = (error.error as { message?: string } | null)?.message;
  return message ?? fallback;
}
