import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TripService } from '../core/trip.service';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { HotelService } from '../core/hotel.service';
import { AuthService } from '../core/auth.service';
import { Hotel, Trip } from '../core/models';
import { tripPhotoUrls } from '../core/photos';
import { ImageCarousel } from '../shared/image-carousel/image-carousel';

@Component({
  selector: 'app-trip-detail',
  standalone: true,
  imports: [
    RouterLink,
    CurrencyPipe,
    DatePipe,
    FormsModule,
    ImageCarousel,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './trip-detail.html',
  styleUrl: './trip-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TripDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly tripService = inject(TripService);
  private readonly groupBookingService = inject(GroupBookingService);
  private readonly hotelService = inject(HotelService);
  protected readonly auth = inject(AuthService);

  private readonly tripId = this.route.snapshot.paramMap.get('id')!;

  protected readonly trip = signal<Trip | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly createError = signal<string | null>(null);
  protected readonly photos = computed(() => {
    const trip = this.trip();
    return trip ? tripPhotoUrls(trip.id, 6) : [];
  });

  protected readonly hotels = signal<Hotel[]>([]);
  protected readonly hotelName = signal('');
  protected readonly hotelDescription = signal('');
  protected readonly hotelPhotoUrls = signal('');
  protected readonly addingHotel = signal(false);
  protected readonly addHotelError = signal<string | null>(null);

  constructor() {
    this.tripService.getTrip(this.tripId).subscribe({
      next: (trip) => {
        this.trip.set(trip);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Trip not found.');
        this.loading.set(false);
      },
    });
    this.refreshHotels();
  }

  protected addHotel(): void {
    const name = this.hotelName().trim();
    const description = this.hotelDescription().trim();
    if (!name || !description) {
      return;
    }
    const photoUrls = this.hotelPhotoUrls()
      .split(/\r?\n/)
      .map((url) => url.trim())
      .filter((url) => url.length > 0);
    this.addingHotel.set(true);
    this.addHotelError.set(null);
    this.hotelService.addHotel(this.tripId, name, description, photoUrls).subscribe({
      next: () => {
        this.addingHotel.set(false);
        this.hotelName.set('');
        this.hotelDescription.set('');
        this.hotelPhotoUrls.set('');
        this.refreshHotels();
      },
      error: (err: HttpErrorResponse) => {
        this.addingHotel.set(false);
        this.addHotelError.set(apiErrorMessage(err, 'Could not add this hotel.'));
      },
    });
  }

  private refreshHotels(): void {
    this.hotelService.listHotels(this.tripId).subscribe({
      next: (hotels) => this.hotels.set(hotels),
      error: () => {
        // Non-critical: the hotel catalog is supplementary, so a failed fetch just
        // leaves the section empty rather than blocking the rest of the page.
      },
    });
  }

  protected startGroup(): void {
    const trip = this.trip();
    if (!trip) {
      return;
    }
    this.creating.set(true);
    this.createError.set(null);
    this.groupBookingService.createGroupBooking(trip.id).subscribe({
      next: (booking) => this.router.navigate(['/group-bookings', booking.id]),
      error: (err: HttpErrorResponse) => {
        this.creating.set(false);
        this.createError.set(apiErrorMessage(err, 'Could not start a group booking.'));
      },
    });
  }
}
