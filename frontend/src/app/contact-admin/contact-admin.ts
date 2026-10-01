import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ContactMessageService } from '../core/contact-message.service';
import { apiErrorMessage } from '../core/group-booking.service';
import { ContactMessage } from '../core/models';

@Component({
  selector: 'app-contact-admin',
  standalone: true,
  imports: [DatePipe, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './contact-admin.html',
  styleUrl: './contact-admin.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactAdmin {
  private readonly contactMessageService = inject(ContactMessageService);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly sent = signal(false);
  protected readonly messages = signal<ContactMessage[]>([]);
  protected readonly loadingMessages = signal(true);

  constructor() {
    this.refreshMessages();
  }

  protected submit(subject: string, message: string): void {
    if (!subject.trim() || !message.trim()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.sent.set(false);
    this.contactMessageService.sendMessage(subject.trim(), message.trim()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.sent.set(true);
        this.refreshMessages();
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err, 'Could not send your message.'));
      },
    });
  }

  private refreshMessages(): void {
    this.loadingMessages.set(true);
    this.contactMessageService.listMessages().subscribe({
      next: (messages) => {
        this.messages.set(messages);
        this.loadingMessages.set(false);
      },
      error: () => this.loadingMessages.set(false),
    });
  }
}
