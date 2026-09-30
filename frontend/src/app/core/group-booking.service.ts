import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { GroupBooking } from './models';

const LIVE_EVENT_NAMES = ['participant-joined', 'participant-left', 'finalized'];
const MY_PARTICIPANT_STORAGE_PREFIX = 'agency-voyage:my-participant:';

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

  leaveGroupBooking(bookingId: string, participantId: string): Observable<GroupBooking> {
    return this.http.delete<GroupBooking>(
      `${API_BASE_URL}/api/group-bookings/${bookingId}/participants/${participantId}`,
    );
  }

  getGroupBooking(bookingId: string): Observable<GroupBooking> {
    return this.http.get<GroupBooking>(`${API_BASE_URL}/api/group-bookings/${bookingId}`);
  }

  /**
   * There's no auth, so "which participant is me" is remembered client-side, per
   * booking, the moment a create/join response tells us. Best-effort: a private
   * window, cleared storage, or a different browser just means no Leave button shows.
   */
  rememberMyParticipantId(bookingId: string, participantId: string): void {
    try {
      localStorage.setItem(MY_PARTICIPANT_STORAGE_PREFIX + bookingId, participantId);
    } catch {
      // ignore - localStorage unavailable
    }
  }

  myParticipantId(bookingId: string): string | null {
    try {
      return localStorage.getItem(MY_PARTICIPANT_STORAGE_PREFIX + bookingId);
    } catch {
      return null;
    }
  }

  forgetMyParticipantId(bookingId: string): void {
    try {
      localStorage.removeItem(MY_PARTICIPANT_STORAGE_PREFIX + bookingId);
    } catch {
      // ignore - localStorage unavailable
    }
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
