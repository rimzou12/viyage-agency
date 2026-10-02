import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { Hotel } from './models';

@Injectable({ providedIn: 'root' })
export class HotelService {
  private readonly http = inject(HttpClient);

  listHotels(tripId: string): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(`${API_BASE_URL}/api/trips/${tripId}/hotels`);
  }

  /** Requires the caller to be an admin - the auth interceptor attaches the token. */
  addHotel(tripId: string, name: string, description: string, photoUrls: string[]): Observable<Hotel> {
    return this.http.post<Hotel>(`${API_BASE_URL}/api/trips/${tripId}/hotels`, { name, description, photoUrls });
  }

  /** Requires the caller to be an admin - the auth interceptor attaches the token. */
  updateHotel(
    tripId: string,
    hotelId: string,
    name: string,
    description: string,
    photoUrls: string[],
  ): Observable<Hotel> {
    return this.http.put<Hotel>(`${API_BASE_URL}/api/trips/${tripId}/hotels/${hotelId}`, {
      name,
      description,
      photoUrls,
    });
  }
}
