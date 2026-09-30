import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { TripList } from './trip-list';
import { API_BASE_URL } from '../core/api-config';
import { Trip } from '../core/models';

describe('TripList', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripList],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads and displays trips', () => {
    const fixture = TestBed.createComponent(TripList);
    fixture.detectChanges();

    const req = httpMock.expectOne(`${API_BASE_URL}/api/trips`);
    req.flush([sampleTrip()]);
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as { trips: () => Trip[]; loading: () => boolean };
    expect(component.loading()).toBeFalsy();
    expect(component.trips().length).toBe(1);
    expect(component.trips()[0].destination).toBe('Bali');
  });

  it('shows an error message when the request fails', () => {
    const fixture = TestBed.createComponent(TripList);
    fixture.detectChanges();

    const req = httpMock.expectOne(`${API_BASE_URL}/api/trips`);
    req.flush('boom', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Could not load trips');
  });

  function sampleTrip(): Trip {
    return {
      id: 'trip-1',
      destination: 'Bali',
      description: 'desc',
      departureDate: '2027-06-10',
      returnDate: '2027-06-20',
      minParticipants: 2,
      maxParticipants: 10,
      bookingDeadline: '2027-05-01T00:00:00Z',
      basePrice: 1000,
      priceTiers: [{ minParticipants: 5, pricePerSeat: 800 }],
    };
  }
});
