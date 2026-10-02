import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../core/auth.service';
import { ContactMessageService } from '../core/contact-message.service';
import { apiErrorMessage } from '../core/group-booking.service';
import { ContactMessage } from '../core/models';

interface ConversationSummary {
  conversationUserId: string;
  customerName: string;
  lastMessage: string;
  lastSentAt: string;
}

const POLL_INTERVAL_MS = 5000;

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [DatePipe, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './chat-widget.html',
  styleUrl: './chat-widget.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChatWidget {
  private readonly auth = inject(AuthService);
  private readonly contactMessageService = inject(ContactMessageService);

  protected readonly isAdmin = computed(() => this.auth.currentUser()?.isAdmin ?? false);

  protected readonly open = signal(false);
  protected readonly sending = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly conversations = signal<ConversationSummary[]>([]);
  protected readonly selectedConversationUserId = signal<string | null>(null);
  protected readonly messages = signal<ContactMessage[]>([]);
  protected readonly loadingMessages = signal(false);

  protected draft = '';

  private pollHandle: ReturnType<typeof setInterval> | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopPolling());
  }

  protected toggleOpen(): void {
    this.open.update((value) => !value);
    if (this.open()) {
      this.onOpened();
    } else {
      this.stopPolling();
    }
  }

  protected selectConversation(userId: string): void {
    this.selectedConversationUserId.set(userId);
    this.refreshMessages();
  }

  protected backToConversationList(): void {
    this.selectedConversationUserId.set(null);
    this.messages.set([]);
  }

  protected send(text: string): void {
    const trimmed = text.trim();
    if (!trimmed) {
      return;
    }
    const conversationUserId = this.selectedConversationUserId();
    this.sending.set(true);
    this.error.set(null);

    const request =
      this.isAdmin() && conversationUserId
        ? this.contactMessageService.reply(conversationUserId, trimmed)
        : this.contactMessageService.sendMessage(trimmed);

    request.subscribe({
      next: () => {
        this.sending.set(false);
        this.draft = '';
        this.refreshMessages();
        if (this.isAdmin()) {
          this.refreshConversations();
        }
      },
      error: (err: HttpErrorResponse) => {
        this.sending.set(false);
        this.error.set(apiErrorMessage(err, 'Could not send your message.'));
      },
    });
  }

  private onOpened(): void {
    this.error.set(null);
    if (this.isAdmin()) {
      this.refreshConversations();
    } else {
      this.selectedConversationUserId.set(this.auth.currentUser()?.id ?? null);
      this.refreshMessages();
    }
    this.startPolling();
  }

  private startPolling(): void {
    this.stopPolling();
    this.pollHandle = setInterval(() => {
      if (this.isAdmin()) {
        this.refreshConversations();
      }
      this.refreshMessages();
    }, POLL_INTERVAL_MS);
  }

  private stopPolling(): void {
    if (this.pollHandle !== null) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }

  private refreshConversations(): void {
    this.contactMessageService.listAllMessages().subscribe({
      next: (messages) => this.conversations.set(summarize(messages)),
      error: () => {},
    });
  }

  private refreshMessages(): void {
    const userId = this.selectedConversationUserId();
    if (!userId) {
      return;
    }
    this.loadingMessages.set(true);
    this.contactMessageService.getConversation(userId).subscribe({
      next: (messages) => {
        this.messages.set(messages);
        this.loadingMessages.set(false);
      },
      error: () => this.loadingMessages.set(false),
    });
  }
}

function summarize(messages: ContactMessage[]): ConversationSummary[] {
  const byConversation = new Map<string, ContactMessage[]>();
  for (const message of messages) {
    const group = byConversation.get(message.conversationUserId) ?? [];
    group.push(message);
    byConversation.set(message.conversationUserId, group);
  }

  return [...byConversation.entries()]
    .map(([conversationUserId, groupMessages]) => {
      const customerMessage = groupMessages.find((m) => !m.fromAdmin) ?? groupMessages[0];
      const latest = groupMessages.reduce((a, b) => (a.sentAt > b.sentAt ? a : b));
      return {
        conversationUserId,
        customerName: customerMessage.authorName,
        lastMessage: latest.message,
        lastSentAt: latest.sentAt,
      };
    })
    .sort((a, b) => (a.lastSentAt < b.lastSentAt ? 1 : -1));
}
