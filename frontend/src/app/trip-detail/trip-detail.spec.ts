import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TripDetail } from './trip-detail';
import { API_BASE_URL } from '../core/api-config';
import { Trip } from '../core/models';

describe('TripDetail', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TripDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: 'trip-1' }) } },
        },
      ],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads and displays the trip', () => {
    const fixture = TestBed.createComponent(TripDetail);
    fixture.detectChanges();

    const req = httpMock.expectOne(`${API_BASE_URL}/api/trips/trip-1`);
    req.flush(sampleTrip());
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Bali');
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
