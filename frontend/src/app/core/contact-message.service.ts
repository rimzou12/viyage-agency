import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { ContactMessage } from './models';

@Injectable({ providedIn: 'root' })
export class ContactMessageService {
  private readonly http = inject(HttpClient);

  /** Requires the caller to be logged in - the auth interceptor attaches the token. */
  sendMessage(subject: string, message: string): Observable<ContactMessage> {
    return this.http.post<ContactMessage>(`${API_BASE_URL}/api/contact-messages`, { subject, message });
  }

  /** Newest first. No dedicated admin role yet, so any logged-in user can list messages. */
  listMessages(): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(`${API_BASE_URL}/api/contact-messages`);
  }
}
