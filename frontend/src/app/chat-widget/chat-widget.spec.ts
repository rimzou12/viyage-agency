import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ChatWidget } from './chat-widget';
import { API_BASE_URL } from '../core/api-config';
import { ContactMessage } from '../core/models';

describe('ChatWidget', () => {
  let httpMock: HttpTestingController;

  afterEach(() => localStorage.removeItem('agency-voyage:auth'));

  async function setUp(user: { id: string; email: string; displayName: string; isAdmin: boolean }): Promise<void> {
    localStorage.setItem('agency-voyage:auth', JSON.stringify({ token: 'fake-token', user }));
    await TestBed.configureTestingModule({
      imports: [ChatWidget],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  }

  it('lets a regular user open the widget, see their own thread and send a message', async () => {
    await setUp({ id: 'alice-1', email: 'alice@example.com', displayName: 'Alice', isAdmin: false });

    const fixture = TestBed.createComponent(ChatWidget);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    (compiled.querySelector('.chat-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages/conversations/alice-1`).flush([sampleMessage()]);
    fixture.detectChanges();

    expect(compiled.textContent).toContain('Where is my seat?');

    fixture.componentInstance['draft'] = 'Also, what time is boarding?';
    (compiled.querySelector('.composer') as HTMLFormElement).dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    const sendReq = httpMock.expectOne(`${API_BASE_URL}/api/contact-messages`);
    expect(sendReq.request.method).toBe('POST');
    expect(sendReq.request.body).toEqual({ message: 'Also, what time is boarding?' });
    sendReq.flush({ ...sampleMessage(), message: 'Also, what time is boarding?' });

    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages/conversations/alice-1`).flush([sampleMessage()]);
    fixture.detectChanges();
  });

  it('lets an admin see the conversation list, open a thread and reply', async () => {
    await setUp({ id: 'admin-1', email: 'admin@example.com', displayName: 'Admin', isAdmin: true });

    const fixture = TestBed.createComponent(ChatWidget);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    (compiled.querySelector('.chat-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages`).flush([sampleMessage()]);
    fixture.detectChanges();

    expect(compiled.textContent).toContain('Alice');

    (compiled.querySelector('.conversation-item') as HTMLButtonElement).click();
    fixture.detectChanges();

    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages/conversations/alice-1`).flush([sampleMessage()]);
    fixture.detectChanges();

    fixture.componentInstance['draft'] = 'Seat 12A';
    (compiled.querySelector('.composer') as HTMLFormElement).dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    const replyReq = httpMock.expectOne(`${API_BASE_URL}/api/contact-messages/reply`);
    expect(replyReq.request.method).toBe('POST');
    expect(replyReq.request.body).toEqual({ conversationUserId: 'alice-1', message: 'Seat 12A' });
    replyReq.flush({ ...sampleMessage(), fromAdmin: true, authorName: 'Admin', message: 'Seat 12A' });

    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages/conversations/alice-1`).flush([sampleMessage()]);
    httpMock.expectOne(`${API_BASE_URL}/api/contact-messages`).flush([sampleMessage()]);
    fixture.detectChanges();
  });

  function sampleMessage(): ContactMessage {
    return {
      id: 'msg-1',
      conversationUserId: 'alice-1',
      authorName: 'Alice',
      authorEmail: 'alice@example.com',
      fromAdmin: false,
      message: 'Where is my seat?',
      sentAt: '2027-01-01T00:00:00Z',
    };
  }
});
