import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { Trip } from './models';

@Injectable({ providedIn: 'root' })
export class TripService {
  private readonly http = inject(HttpClient);

  listTrips(): Observable<Trip[]> {
    return this.http.get<Trip[]>(`${API_BASE_URL}/api/trips`);
  }

  getTrip(tripId: string): Observable<Trip> {
    return this.http.get<Trip>(`${API_BASE_URL}/api/trips/${tripId}`);
  }
}
