import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { EMPTY, Observable } from 'rxjs';
import { GroupBookingDetail } from './group-booking-detail';
import { GroupBookingService } from '../core/group-booking.service';
import { API_BASE_URL } from '../core/api-config';
import { GroupBooking } from '../core/models';

/**
 * jsdom (the test DOM) doesn't implement EventSource, so the real service's
 * streamEvents() would throw in this environment. The fallback poll (also driven by
 * GroupBookingDetail) already exercises the HTTP fetch path these tests care about.
 */
class TestGroupBookingService extends GroupBookingService {
  override streamEvents(): Observable<void> {
    return EMPTY;
  }
}

describe('GroupBookingDetail', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupBookingDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: GroupBookingService, useClass: TestGroupBookingService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: 'booking-1' }) } },
        },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('shows the current price, participants and a hint about the next tier', () => {
    const fixture = TestBed.createComponent(GroupBookingDetail);
    fixture.detectChanges();

    httpMock.expectOne(`${API_BASE_URL}/api/group-bookings/booking-1`).flush(sampleBooking());
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('OPEN');
    expect(compiled.textContent).toContain('3 more people needed');
  });

  it('joins the group and updates the displayed booking', () => {
    const fixture = TestBed.createComponent(GroupBookingDetail);
    fixture.detectChanges();
    httpMock.expectOne(`${API_BASE_URL}/api/group-bookings/booking-1`).flush(sampleBooking());
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const input = compiled.querySelector('.join input') as HTMLInputElement;
    input.value = 'Bob';
    input.dispatchEvent(new Event('input'));
    (compiled.querySelector('.join form') as HTMLFormElement).dispatchEvent(new Event('submit'));

    const joinReq = httpMock.expectOne(`${API_BASE_URL}/api/group-bookings/booking-1/participants`);
    expect(joinReq.request.body).toEqual({ customerName: 'Bob' });
    joinReq.flush({ ...sampleBooking(), participantCount: 3 });
    fixture.detectChanges();

    expect(compiled.textContent).toContain('3 / 10');
  });

  function sampleBooking(): GroupBooking {
    return {
      id: 'booking-1',
      tripId: 'trip-1',
      status: 'OPEN',
      participantCount: 2,
      minParticipants: 2,
      maxParticipants: 10,
      currentPricePerSeat: 1000,
      deadline: '2027-05-01T00:00:00Z',
      priceTiers: [{ minParticipants: 5, pricePerSeat: 800 }],
      participants: [
        { id: 'p1', customerName: 'Alice', joinedAt: '2027-04-01T00:00:00Z' },
        { id: 'p2', customerName: 'Bob', joinedAt: '2027-04-01T00:00:00Z' },
      ],
    };
  }
});
