import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { GroupBooking } from './models';

const LIVE_EVENT_NAMES = ['participant-joined', 'participant-left', 'finalized'];

@Injectable({ providedIn: 'root' })
export class GroupBookingService {
  private readonly http = inject(HttpClient);

  /** Requires the caller to be logged in - the auth interceptor attaches the token. */
  createGroupBooking(tripId: string): Observable<GroupBooking> {
    return this.http.post<GroupBooking>(`${API_BASE_URL}/api/trips/${tripId}/group-bookings`, {});
  }

  joinGroupBooking(bookingId: string): Observable<GroupBooking> {
    return this.http.post<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}/participants`, {});
  }

  leaveGroupBooking(bookingId: string): Observable<GroupBooking> {
    return this.http.delete<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}/participants/me`);
  }

  getGroupBooking(bookingId: string): Observable<GroupBooking> {
    return this.http.get<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}`);
  }

  /**
   * Emits (with no payload) whenever the backend pushes a live update for this booking
   * over SSE - a signal to re-fetch, not a copy of the booking itself. Never errors or
   * completes on its own; closes the underlying EventSource on unsubscribe.
   */
  streamEvents(bookingId: string): Observable<void> {
    return new Observable<void>((subscriber) => {
      const source = new EventSource(`${API_BASE_URL}/api/group-bookings/${bookingId}/events`);
      const onEvent = () => subscriber.next();
      for (const eventName of LIVE_EVENT_NAMES) {
        source.addEventListener(eventName, onEvent);
      }
      return () => source.close();
    });
  }
}

export function apiErrorMessage(error: HttpErrorResponse, fallback: string): string {
  const message = (error.error as { message?: string } | null)?.message;
  return message ?? fallback;
}
