import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { ContactMessage } from './models';

@Injectable({ providedIn: 'root' })
export class ContactMessageService {
  private readonly http = inject(HttpClient);

  /** Sends a message as the caller, starting or continuing their own thread with the admin. */
  sendMessage(message: string): Observable<ContactMessage> {
    return this.http.post<ContactMessage>(`${API_BASE_URL}/api/contact-messages`, { message });
  }

  /** Admin only: replies into a given customer's thread. */
  reply(conversationUserId: string, message: string): Observable<ContactMessage> {
    return this.http.post<ContactMessage>(`${API_BASE_URL}/api/contact-messages/reply`, {
      conversationUserId,
      message,
    });
  }

  /** Oldest first. The caller's own thread, or any thread if the caller is an admin. */
  getConversation(userId: string): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(`${API_BASE_URL}/api/contact-messages/conversations/${userId}`);
  }

  /** Admin only: every message across every conversation, newest first. */
  listAllMessages(): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(`${API_BASE_URL}/api/contact-messages`);
  }
}
