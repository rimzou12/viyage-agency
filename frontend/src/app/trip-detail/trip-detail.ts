import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
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
  imports: [RouterLink, CurrencyPipe, DatePipe, ImageCarousel, MatButtonModule, MatProgressSpinnerModule],
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
